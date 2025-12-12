package com.dartsmatcher.legacy.exceptionhandler;

import com.dartsmatcher.legacy.exceptionhandler.exception.ForbiddenException;
import com.dartsmatcher.legacy.exceptionhandler.exception.InvalidArgumentsException;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceAlreadyExistsException;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.legacy.exceptionhandler.response.ApiErrorCode;
import com.dartsmatcher.legacy.exceptionhandler.response.ErrorResponse;
import com.dartsmatcher.legacy.exceptionhandler.response.TargetError;
import com.dartsmatcher.legacy.exceptionhandler.response.WebSocketErrorResponse;
import com.dartsmatcher.legacy.utils.MessageResolver;
import com.dartsmatcher.legacy.utils.Websockets;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.ArrayList;

@ControllerAdvice
public class GlobalWebsocketExceptionHandler {

    private final MessageResolver messageResolver;


    public GlobalWebsocketExceptionHandler(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    //	 Handler for all unhandled exceptions.
    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleRunTimeException(Exception e, StompHeaderAccessor stompHeaderAccessor) {
        e.printStackTrace();

        return new WebSocketErrorResponse(
                ApiErrorCode.INTERNAL,
                stompHeaderAccessor.getDestination(),
                messageResolver.getMessage("exception.internal")
        );
    }

    // Handler for access denied exceptions.
    @MessageExceptionHandler(AccessDeniedException.class)
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleAccessDeniedException(AccessDeniedException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.UNAUTHENTICATED,
                stompHeaderAccessor.getDestination(),
                messageResolver.getMessage("exception.authentication.credentials.not.found")
        );
    }

    // Handler for custom forbidden exception.
    @MessageExceptionHandler(ForbiddenException.class)
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleForbiddenException(ForbiddenException e, StompHeaderAccessor stompHeaderAccessor) {
        return new WebSocketErrorResponse(
                ApiErrorCode.PERMISSION_DENIED,
                stompHeaderAccessor.getDestination(),
                e.getDescription()
        );
    }

    // Handler for bean validation errors thrown in controllers.
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e, StompHeaderAccessor stompHeaderAccessor) {

        ArrayList<TargetError> errors = new ArrayList<>();

        if (e.getBindingResult() != null) {
            for (ObjectError error : e.getBindingResult().getAllErrors()) {
                if (error instanceof FieldError) {
                    errors.add(new TargetError(((FieldError) error).getField(), error.getDefaultMessage()));
                } else {
                    errors.add(new TargetError(error.getCode(), error.getDefaultMessage()));
                }
            }
        }

        return new WebSocketErrorResponse(
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage("exception.invalid.arguments"),
                stompHeaderAccessor.getDestination(),
                errors.toArray(new TargetError[0])
        );
    }

    // Handler for bean validation errors in services.
    @SuppressWarnings("DuplicatedCode")
    @MessageExceptionHandler(ConstraintViolationException.class)
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleConstraintViolationException(ConstraintViolationException e, StompHeaderAccessor stompHeaderAccessor) {

        ArrayList<TargetError> errors = new ArrayList<>();

        // Constructs the path disregarding the first two path nodes.
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            // List of property paths.
            ArrayList<Path.Node> violationPropertyPaths = new ArrayList<>();
            violation.getPropertyPath().iterator().forEachRemaining(violationPropertyPaths::add);

            // The response error path.
            StringBuilder errorPath = new StringBuilder();

            // Ignore method and object path if the property path is nested.
            int startIndex = 0;
            if (violationPropertyPaths.size() > 1) startIndex = 1;
            if (violationPropertyPaths.size() > 2) startIndex = 2;

            for (int i = startIndex; i < violationPropertyPaths.size(); i++) {
                // Append the path.
                errorPath.append(violationPropertyPaths.get(i));

                // Append a dot between paths.
                if (i < violationPropertyPaths.size() - 1) errorPath.append(".");
            }

            errors.add(new TargetError(errorPath.toString(), violation.getMessage()));
        }

        return new WebSocketErrorResponse(
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage("exception.invalid.arguments"),
                stompHeaderAccessor.getDestination(),
                errors.toArray(new TargetError[0])
        );
    }

    // Handler for custom invalid arguments.
    @MessageExceptionHandler(InvalidArgumentsException.class)
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleInvalidArgumentException(InvalidArgumentsException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage("exception.invalid.arguments"),
                stompHeaderAccessor.getDestination(),
                e.getErrors().toArray(new TargetError[0])
        );
    }

    // Handler for resources that are not found.
    @MessageExceptionHandler(ResourceNotFoundException.class)
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleResourceNotFoundException(ResourceNotFoundException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.RESOURCE_NOT_FOUND,
                stompHeaderAccessor.getDestination(),
                messageResolver.getMessage("exception.resource.not.found", e.getResourceType(), e.getIdentifier())
        );
    }

    // Handler for trying to create a resource when it already exists.
    @MessageExceptionHandler(ResourceAlreadyExistsException.class)
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleResourceAlreadyExistsException(ResourceAlreadyExistsException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.ALREADY_EXISTS,
                messageResolver.getMessage("exception.resource.already.exists", e.getResourceType()),
                stompHeaderAccessor.getDestination(),
                new TargetError(e.getTarget(), messageResolver.getMessage("message.resource.already.exists", e.getValue()))
        );
    }

    // Handler for sending malformed data or invalid data types (e.g. invalid json, using array instead of string).
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(MethodArgumentTypeMismatchException e) {
        e.printStackTrace();

        ApiErrorCode apiErrorCode = ApiErrorCode.MESSAGE_NOT_READABLE;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.body.not.readable")
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }


    //	 Handler for when a Message can't be deserialized to the corresponding object (e.g. object requires int but gets an array).
    @MessageExceptionHandler({MessageConversionException.class, ConversionFailedException.class})
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleMessageConversionException(Exception e, StompHeaderAccessor stompHeaderAccessor) {
        e.printStackTrace();

        return new WebSocketErrorResponse(
                ApiErrorCode.MESSAGE_NOT_READABLE,
                stompHeaderAccessor.getDestination(),
                messageResolver.getMessage("exception.body.not.readable")
        );
    }

    // Handler for when the database is down.
    @MessageExceptionHandler(DataAccessResourceFailureException.class)
    @SendToUser(destinations = Websockets.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleDataAccessResourceFailureException(DataAccessResourceFailureException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.UNAVAILABLE,
                stompHeaderAccessor.getDestination(),
                messageResolver.getMessage("exception.service.unavailable")
        );
    }

}
