package de.yatta.ecf.internal.provider.filetransfer.httpclient45;

import java.util.Map;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Proxy;

public interface INTLMProxyHandler
{
   void handleNTLMProxy(Proxy proxy, int code) throws ECFException;

   void handleSPNEGOProxy(Proxy proxy, int code) throws ECFException;

   boolean allowNTLMAuthentication(Map<?, ?> connectOptions);
}
