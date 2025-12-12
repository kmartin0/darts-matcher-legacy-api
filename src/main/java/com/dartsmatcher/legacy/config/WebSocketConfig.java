package com.dartsmatcher.legacy.config;

import com.dartsmatcher.legacy.exceptionhandler.CustomStompSubProtocolErrorHandler;
import com.dartsmatcher.legacy.utils.MessageResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


	private final MessageResolver messageResolver;
	private final AuthenticationManager authenticationManager;

	public WebSocketConfig(@Qualifier(BeanIds.AUTHENTICATION_MANAGER) AuthenticationManager authenticationManager, MessageResolver messageResolver) {

		this.authenticationManager = authenticationManager;
		this.messageResolver = messageResolver;
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/darts-matcher-websocket").setAllowedOriginPatterns("*").withSockJS();
		registry.setErrorHandler(new CustomStompSubProtocolErrorHandler(messageResolver));
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app", "/user");
		registry.enableSimpleBroker("/topic", "/queue");
	}


	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.setInterceptors(new ChannelInterceptorAdapter() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor =
						MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if (accessor != null) {
					if (StompCommand.CONNECT.equals(accessor.getCommand())) {
						String bearer = NativeMessageHeaderAccessor.getFirstNativeHeader("Authorization", message.getHeaders());
						if (bearer != null) {
							BearerTokenAuthenticationToken bearerAuth = new BearerTokenAuthenticationToken(bearer.replaceAll("Bearer ", ""));
							Authentication authentication;
							try {
								authentication = getAuthenticationManager().authenticate(bearerAuth);
							} catch (Exception e) {
								e.printStackTrace();
								throw e;
							}

							if (authentication != null) {
								accessor.setUser(authentication);
							}
						}
					}
				}

				return message;
			}
		});
	}
}
