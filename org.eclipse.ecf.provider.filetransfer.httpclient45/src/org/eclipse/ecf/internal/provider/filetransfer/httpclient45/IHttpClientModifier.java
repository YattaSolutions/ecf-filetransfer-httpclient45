package org.eclipse.ecf.internal.provider.filetransfer.httpclient45;

import java.util.Map;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;

public interface IHttpClientModifier {
	HttpClientBuilder modifyClient(HttpClientBuilder builder);

	CredentialsProvider modifyCredentialsProvider(CredentialsProvider credentialsProvider);

	HttpClientContext modifyContext(HttpClientContext context);

	RequestConfig.Builder modifyRequestConfig(RequestConfig.Builder config, HttpClientContext context,
			Map<?, ?> options);
}
