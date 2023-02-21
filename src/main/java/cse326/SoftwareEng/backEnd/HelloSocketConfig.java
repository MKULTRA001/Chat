package cse326.SoftwareEng.backEnd;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Test Socket Config
 * <p>Default application endpoint prefix: /app</p>
 * <p>Default client message broker endpoint: /test</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class HelloSocketConfig implements WebSocketMessageBrokerConfigurer {
    //I hate how many words there are in these spring framework class frames it's awful

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //where the broker operates (outgoing)
        config.enableSimpleBroker("/test");
        //where the application operates (incoming)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //This enables various things but the important part is just make sure the socket endpoint lines up in the JS
        registry.addEndpoint("/test-websocket").withSockJS();
    }

}
