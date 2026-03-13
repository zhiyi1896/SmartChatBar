package com.niu.community.es.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class ElasticsearchConfig {

    @Bean(destroyMethod = "close")
    public RestClient restClient(@Value("${spring.elasticsearch.uris}") String uris,
                                 @Value("${spring.elasticsearch.username:}") String username,
                                 @Value("${spring.elasticsearch.password:}") String password) {
        String[] parts = uris.split(",");
        HttpHost[] hosts = new HttpHost[parts.length];
        for (int i = 0; i < parts.length; i++) {
            hosts[i] = HttpHost.create(parts[i].trim());
        }
        if (StringUtils.hasText(username)) {
            BasicCredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            return RestClient.builder(hosts)
                .setHttpClientConfigCallback(builder -> builder.setDefaultCredentialsProvider(provider))
                .build();
        }
        return RestClient.builder(hosts).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
