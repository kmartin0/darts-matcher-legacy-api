package com.dartsmatcher.legacy.exceptionhandler;

import com.dartsmatcher.legacy.exceptionhandler.response.ApiErrorCode;
import com.dartsmatcher.legacy.exceptionhandler.response.ErrorResponse;
import com.dartsmatcher.legacy.utils.MessageResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

public class CustomStompSubProtocolErrorHandler extends StompSubProtocolErrorHandler {

	private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
	private final MessageResolver messageResolver;

	public CustomStompSubProtocolErrorHandler(MessageResolver messageResolver) {
		this.messageResolver = messageResolver;
	}

	@Override
	protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor, byte[] errorPayload, Throwable cause, StompHeaderAccessor clientHeaderAccessor) {

		if (cause instanceof MessageDeliveryException) {
			Throwable nestedException = cause.getCause();

			ErrorResponse errorResponse = null;

			if (nestedException instanceof InvalidBearerTokenException)
				errorResponse = createInvalidTokenErrorResponse();

			errorPayload = createErrorPayload(errorResponse);
		}

		return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());
	}

	private ErrorResponse createInvalidTokenErrorResponse() {
		return new ErrorResponse(
				ApiErrorCode.UNAUTHENTICATED,
				messageResolver.getMessage("exception.invalid.token")
		);
	}

	private byte[] createErrorPayload(@Nullable ErrorResponse errorResponse) {
		if (errorResponse == null) return null;

		try {
			return ow.writeValueAsString(errorResponse).getBytes();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
}
