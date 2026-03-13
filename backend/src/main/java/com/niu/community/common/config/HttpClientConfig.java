package com.niu.community.common.config;

import com.niu.community.ai.client.AiServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient aiWebClient(AppProperties appProperties) {
        return WebClient.builder().baseUrl(appProperties.getAiServiceUrl()).build();
    }

    @Bean
    public AiServiceClient aiServiceClient(WebClient aiWebClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(aiWebClient)).build();
        return factory.createClient(AiServiceClient.class);
    }
}
