package com.niu.community.common.config;

import com.niu.community.message.websocket.MessageWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageWebSocketHandler messageWebSocketHandler;
    private final AppProperties appProperties;

    public WebSocketConfig(MessageWebSocketHandler messageWebSocketHandler, AppProperties appProperties) {
        this.messageWebSocketHandler = messageWebSocketHandler;
        this.appProperties = appProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageWebSocketHandler, appProperties.getWebsocketPath()).setAllowedOrigins("*");
    }
}
