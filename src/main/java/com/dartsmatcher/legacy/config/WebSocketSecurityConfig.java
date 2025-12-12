package com.dartsmatcher.legacy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig
		extends AbstractSecurityWebSocketMessageBrokerConfigurer {

	@Override
	protected boolean sameOriginDisabled() {
		return true;
	}




	protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//		messages.matchers("/topic/friends").
		messages.anyMessage().permitAll();

		messages.simpTypeMatchers(
				SimpMessageType.CONNECT,
				SimpMessageType.DISCONNECT,
				SimpMessageType.UNSUBSCRIBE,
				SimpMessageType.SUBSCRIBE,
				SimpMessageType.OTHER).permitAll();



//		messages.anyMessage().authenticated();
//		messages.simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.UNSUBSCRIBE, SimpMessageType.DISCONNECT).permitAll();

//		messages
//				.simpTypeMatchers(SimpMessageType.CONNECT,
//						SimpMessageType.DISCONNECT, SimpMessageType.OTHER).permitAll()
//				.anyMessage().authenticated();
	}
}
