package com.ftn.uns.ac.rs.smarthomesockets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
// STOMP
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/realtime").setAllowedOriginPatterns("*").withSockJS();   // CORS
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/thermometer","/ac", "/consumption", "/production", "/wm");  // địa chỉ mà client sẽ SUBSCRIBE để nhận dữ liệu real-time.
        registry.setApplicationDestinationPrefixes("/app");
    }
}