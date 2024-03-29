package dev.sort.oss.quarkus.opensearch.client.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import java.io.IOException;
import java.io.UncheckedIOException;

@ApplicationScoped
public class OpenSearchClientProducer {
    @Inject
    OpenSearchConfig config;

    private RestClientTransport transport;

    private void ensureTransport() {
        if (this.transport != null) {
            return;
        }

        var credentialsProvider = new BasicCredentialsProvider();
        if (config.username.isPresent()) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(config.username.get(), config.password.orElse("")));
        }

        var restClient = RestClient.builder(HttpHost.create(config.host))
                .setHttpClientConfigCallback(clientBuilder ->
                        clientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();

        var mapper = new ObjectMapper()
                .registerModule(new KotlinModule())
                .registerModule(new JavaTimeModule())
                .registerModule(new JSONPModule());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.transport = new RestClientTransport(restClient, new JacksonJsonpMapper(mapper));
    }

    @Produces
    @Singleton
    public OpenSearchClient openSearchJavaClient() {
        ensureTransport();
        return new OpenSearchClient(this.transport);
    }

    @Produces
    @Singleton
    public OpenSearchAsyncClient openSearchJavaAsyncClient() {
        ensureTransport();
        return new OpenSearchAsyncClient(this.transport);
    }

    @PreDestroy
    void destroy() {
        try {
            if (this.transport != null) {
                this.transport.close();
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
