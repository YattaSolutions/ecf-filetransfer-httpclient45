package org.eclipse.ecf.provider.filetransfer.httpclient45;

/**
 * @see org.eclipse.ecf.provider.filetransfer.httpclient4.HttpClientOptions
 */
public interface HttpClientOptions
{
   String BROWSE_CONNECTION_TIMEOUT_PROP = "org.eclipse.ecf.provider.filetransfer.httpclient4.browse.connectTimeout"; //$NON-NLS-1$
   String RETRIEVE_READ_TIMEOUT_PROP = "org.eclipse.ecf.provider.filetransfer.httpclient4.retrieve.readTimeout"; //$NON-NLS-1$
   String RETRIEVE_CONNECTION_TIMEOUT_PROP = "org.eclipse.ecf.provider.filetransfer.httpclient4.retrieve.connectTimeout"; //$NON-NLS-1$

   int RETRIEVE_DEFAULT_CONNECTION_TIMEOUT = Integer.parseInt(System.getProperty(RETRIEVE_CONNECTION_TIMEOUT_PROP, "120000")); //$NON-NLS-1$
   int RETRIEVE_DEFAULT_READ_TIMEOUT = Integer.parseInt(System.getProperty(RETRIEVE_READ_TIMEOUT_PROP, "120000")); //$NON-NLS-1$
   int BROWSE_DEFAULT_CONNECTION_TIMEOUT = Integer.parseInt(System.getProperty(BROWSE_CONNECTION_TIMEOUT_PROP, "120000")); //$NON-NLS-1$

   int NTLM_PROXY_RESPONSE_CODE = 477;
   String FORCE_NTLM_PROP = "org.eclipse.ecf.provider.filetransfer.httpclient4.options.ForceNTLMProxy"; //$NON-NLS-1$

}
