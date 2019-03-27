package org.eclipse.ecf.internal.provider.filetransfer.httpclient45;

import java.util.Map;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;

public interface IHttpClientFactory
{

   HttpClientBuilder newClient();

   HttpClientContext newClientContext();

   RequestConfig.Builder newRequestConfig(HttpClientContext context, Map<?, ?> localOptions);

}