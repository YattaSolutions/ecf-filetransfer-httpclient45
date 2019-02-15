/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thomas Joiner - HttpClient 4 implementation
 *******************************************************************************/
package de.yatta.ecf.internal.provider.filetransfer.httpclient45;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.core.util.ProxyAddress;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.filetransfer.DebugOptions;
import org.osgi.framework.FrameworkUtil;

import de.yatta.ecf.provider.filetransfer.httpclient45.HttpClientRetrieveFileTransfer;

public class HttpClientProxyCredentialProvider extends BasicCredentialsProvider
{
   private static final String COMPUTERNAME_ENV = "COMPUTERNAME"; //$NON-NLS-1$
   private static final String HOSTNAME_ENV = "HOSTNAME"; //$NON-NLS-1$
   private static final String LOGONSERVER_ENV = "LOGONSERVER"; //$NON-NLS-1$
   private static final String USER_DOMAIN_ENV = "USERDOMAIN"; //$NON-NLS-1$
   private static final String USER_DNS_DOMAIN_ENV = "USERDNSDOMAIN"; //$NON-NLS-1$

   private static final char BACKSLASH = '\\';
   private static final char SLASH = '/';

   private static final String OSGI_OS = "osgi.os"; //$NON-NLS-1$
   private static final String OSGI_OS_WIN32 = "win32"; //$NON-NLS-1$

   private static final String NTLM_DOMAIN_PROPERTY = "http.auth.ntlm.domain"; //$NON-NLS-1$

   protected Proxy getECFProxy()
   {
      return null;
   }

   protected boolean allowNTLMAuthentication()
   {
      DefaultNTLMProxyHandler.setSeenNTLM();
      return Activator.getDefault().getNTLMProxyHandler().allowNTLMAuthentication(null);
   }

   protected Credentials getNTLMCredentials(Credentials credentials)
   {
      DefaultNTLMProxyHandler.setSeenNTLM();
      Credentials fixed = fixNTCredentials(credentials);
      if (fixed == credentials || allowNTLMAuthentication())
      {
         return fixed;
      }
      return null;
   }

   protected NTCredentials getNTLMCredentials(Proxy proxy)
   {
      if (allowNTLMAuthentication())
      {
         return createNTLMCredentials(proxy);
      }
      return null;
   }

   @Override
   public Credentials getCredentials(AuthScope authscope)
   {
      Trace.entering(Activator.PLUGIN_ID, DebugOptions.METHODS_ENTERING, HttpClientProxyCredentialProvider.class, "getCredentials " + authscope); //$NON-NLS-1$

      // First check to see whether given authscope matches any authscope
      // already cached.
      Credentials result = super.getCredentials(authscope);

      // If we have a match, return credentials
      if (result != null)
      {
         if ("ntlm".equalsIgnoreCase(authscope.getScheme())) //$NON-NLS-1$
         {
            //We might have gotten these from a password prompt, making them UsernamePasswordCredentials...
            Credentials fixed = fixNTCredentials(result);
            if (fixed != result)
            {
               result = fixed;
               setCredentials(authscope, fixed);
            }
         }
         return result;
      }

      // If we don't have a match, first get ECF proxy, if any
      Proxy proxy = getECFProxy();
      if (proxy == null)
      {
         return null;
      }

      // Make sure that authscope and proxy host and port match
      if (!matchAuthScopeAndProxy(authscope, proxy))
      {
         return null;
      }

      // Then match scheme, and get credentials from proxy (if it's scheme we know about)
      Credentials credentials = null;
      if ("ntlm".equalsIgnoreCase(authscope.getScheme())) //$NON-NLS-1$
      {
         credentials = getNTLMCredentials(proxy);
      }
      else if ("basic".equalsIgnoreCase(authscope.getScheme()) || //$NON-NLS-1$
            "digest".equalsIgnoreCase(authscope.getScheme())) //$NON-NLS-1$
      {
         final String proxyUsername = proxy.getUsername();
         final String proxyPassword = proxy.getPassword();
         // If credentials present for proxy then we're done
         if (proxyUsername != null)
         {
            credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
         }
      }
      else if ("negotiate".equalsIgnoreCase(authscope.getScheme())) //$NON-NLS-1$
      {
         Trace.trace(Activator.PLUGIN_ID, "SPNEGO is not supported, if you can contribute support, please do so."); //$NON-NLS-1$
      }
      else
      {
         Trace.trace(Activator.PLUGIN_ID, "Unrecognized authentication scheme."); //$NON-NLS-1$
      }

      // Put found credentials in cache for next time
      if (credentials != null)
      {
         setCredentials(authscope, credentials);
      }

      return credentials;
   }

   private static boolean matchAuthScopeAndProxy(AuthScope authscope, Proxy proxy)
   {
      ProxyAddress proxyAddress = proxy.getAddress();
      return (authscope.getHost().equals(proxyAddress.getHostName()) && (authscope.getPort() == proxyAddress.getPort()));
   }

   /**
    * @param p proxy to create NTCredentials for
    * @return NTCredentials new ntlm credentials given proxy
    * @since 5.0
    */
   public static NTCredentials createNTLMCredentials(Proxy p)
   {
      if (p == null)
      {
         return null;
      }
      return createNTLMCredentials(p.getUsername(), p.getPassword());
   }

   /**
    * @param p proxy to create NTCredentials for
    * @return NTCredentials new ntlm credentials given proxy
    * @since 5.0
    */
   protected static NTCredentials createNTLMCredentials(String username, String password)
   {
      String un = getNTLMUserName(username);
      String domain = getNTLMDomainName(username);
      if (un == null || domain == null || un.isEmpty() || domain.isEmpty())
      {
         return null;
      }

      String workstation = getNTLMWorkstation();

      return new NTCredentials(un, password, workstation, domain);
   }

   public static Credentials fixNTCredentials(Credentials credentials)
   {
      if (credentials == null)
      {
         return null;
      }
      if (credentials instanceof NTCredentials)
      {
         NTCredentials ntCreds = (NTCredentials)credentials;
         String userName = ntCreds.getUserName();
         String domainUser = getNTLMUserName(userName);
         if (ntCreds.getDomain() == null || domainUser != userName)
         {
            String domain = getNTLMDomainName(userName);
            String workstation = getNTLMWorkstation();
            return new NTCredentials(domainUser, ntCreds.getPassword(), workstation, domain);
         }
      }
      else if (credentials instanceof UsernamePasswordCredentials)
      {
         UsernamePasswordCredentials basicCredentials = (UsernamePasswordCredentials)credentials;
         return createNTLMCredentials(basicCredentials.getUserName(), basicCredentials.getPassword());
      }
      return credentials;
   }

   public static String getNTLMWorkstation()
   {
      String dnsName = getDnsHostName();
      if (dnsName != null)
      {
         return dnsName;
      }
      String hostName = getEnvHostName();
      if (hostName != null)
      {
         return hostName;
      }
      return null;
   }

   private static boolean isWindows()
   {
      String os = FrameworkUtil.getBundle(HttpClientProxyCredentialProvider.class).getBundleContext().getProperty(OSGI_OS);
      return OSGI_OS_WIN32.equalsIgnoreCase(os);
   }

   private static String getEnvHostName()
   {
      boolean isWindows = isWindows();
      String envKey = isWindows ? COMPUTERNAME_ENV : HOSTNAME_ENV;
      String hostName = System.getenv(envKey);
      if (hostName == null && isWindows)
      {
         hostName = System.getenv(HOSTNAME_ENV);
      }
      return hostName;
   }

   private static String getDnsHostName()
   {
      try
      {
         InetAddress localHost = InetAddress.getLocalHost();
         if (!localHost.isLoopbackAddress())
         {
            String hostName = localHost.getHostName();
            if (hostName != null && !"".equals(hostName) && !"localhost".equals(hostName) && !hostName.equals(localHost.getHostAddress())) //$NON-NLS-1$ //$NON-NLS-2$
            {
               return hostName;
            }
         }
      }
      catch (UnknownHostException e)
      {
         Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING, HttpClientRetrieveFileTransfer.class, "getDnsHostName", e); //$NON-NLS-1$
      }
      return null;
   }

   public static String getNTLMDomainName(String userName)
   {
      String domain = getDomainFromUserName(userName);
      if (domain != null)
      {
         return domain;
      }
      domain = getDomainFromSystemProperties();
      if (domain != null)
      {
         return domain;
      }
      return getDomainFromEnv();
   }

   private static String getDomainFromEnv()
   {
      //FIXME some systems seem to need the DNS domain name instead of the NetBIOS name (from USERDNSDOMAIN env variable)
      String domain = System.getenv(USER_DOMAIN_ENV);
      if (domain != null)
      {
         if (isRealDomain(domain))
         {
            return domain;
         }
         return null;
      }
      domain = System.getenv(USER_DNS_DOMAIN_ENV);
      if (domain != null)
      {
         return domain;
      }
      return null;
   }

   private static String getDomainFromSystemProperties()
   {
      String domain;
      domain = System.getProperty(NTLM_DOMAIN_PROPERTY);
      return domain;
   }

   private static boolean isRealDomain(String domain)
   {
      String hostName = getEnvHostName();
      if (!domain.equalsIgnoreCase(hostName))
      {
         return true;
      }
      if (isWindows())
      {
         String logonHost = System.getenv(LOGONSERVER_ENV);
         if (logonHost != null && !logonHost.equalsIgnoreCase(Character.toString(BACKSLASH) + BACKSLASH + domain))
         {
            return true;
         }
      }
      return false;
   }

   private static String getDomainFromUserName(String userName)
   {
      if (userName == null)
      {
         return null;
      }
      int pos = getNTLMDomainUserSeparatorPos(userName);
      if (pos != -1)
      {
         return userName.substring(0, pos);
      }
      return null;
   }

   private static int getNTLMDomainUserSeparatorPos(String userName)
   {
      int pos = userName.indexOf(BACKSLASH);
      if (pos == -1)
      {
         pos = userName.indexOf(SLASH);
      }
      return pos;
   }

   public static String getNTLMUserName(String userName)
   {
      if (userName == null)
      {
         return null;
      }
      int pos = getNTLMDomainUserSeparatorPos(userName);
      if (pos == -1)
      {
         return userName;
      }
      if (userName.length() > pos + 1 && userName.charAt(pos + 1) == SLASH)
      {
         pos++;
      }
      if (userName.length() >= pos + 1)
      {
         return userName.substring(pos + 1);
      }
      return userName;
   }

}
