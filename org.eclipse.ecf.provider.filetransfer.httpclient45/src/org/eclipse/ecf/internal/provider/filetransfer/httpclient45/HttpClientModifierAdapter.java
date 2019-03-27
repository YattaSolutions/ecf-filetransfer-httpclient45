package de.yatta.ecf.internal.provider.filetransfer.httpclient45;

import java.util.Map;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientModifierAdapter implements IHttpClientModifier
{

   @Override
   public HttpClientBuilder modifyClient(HttpClientBuilder builder)
   {
      return builder;
   }

   @Override
   public CredentialsProvider modifyCredentialsProvider(CredentialsProvider credentialsProvider)
   {
      return credentialsProvider;
   }

   @Override
   public HttpClientContext modifyContext(HttpClientContext context)
   {
      return context;
   }

   @Override
   public Builder modifyRequestConfig(Builder config, HttpClientContext context, Map<?, ?> options)
   {
      return config;
   }

}
