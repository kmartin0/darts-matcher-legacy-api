package com.dartsmatcher.legacy.exceptionhandler;

import com.dartsmatcher.legacy.exceptionhandler.exception.ForbiddenException;
import com.dartsmatcher.legacy.exceptionhandler.exception.InvalidArgumentsException;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceAlreadyExistsException;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.legacy.exceptionhandler.response.ApiErrorCode;
import com.dartsmatcher.legacy.exceptionhandler.response.ErrorResponse;
import com.dartsmatcher.legacy.exceptionhandler.response.TargetError;
import com.dartsmatcher.legacy.utils.MessageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.ArrayList;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private final MessageResolver messageResolver;


	@Autowired
	public GlobalExceptionHandler(MessageResolver messageResolver) {
		this.messageResolver = messageResolver;
	}

	// Handler for all unhandled exceptions.
	@ExceptionHandler({Exception.class})
	public ResponseEntity<ErrorResponse> handleRunTimeException(Exception e) {
		e.printStackTrace();
		ApiErrorCode apiErrorCode = ApiErrorCode.INTERNAL;
		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				messageResolver.getMessage("exception.internal")
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for access denied exceptions.
	@ExceptionHandler({AccessDeniedException.class})
	public void handleAccessDeniedException(AccessDeniedException e) {
		// Let an AccessDeniedException fall back to the Spring Security Handler.

		throw e;
	}

	// Handler for invalid credentials.
	@ExceptionHandler({InvalidGrantException.class})
	public ResponseEntity<InvalidGrantException> handleInvalidGrantExceptionException(InvalidGrantException e) {

		return new ResponseEntity<>(e, HttpStatus.UNAUTHORIZED);
	}

	// Typically thrown when no Authentication object is present in SecurityContext
	@ExceptionHandler({AuthenticationCredentialsNotFoundException.class})
	public void handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException e) {

		// Let an AuthenticationCredentialsNotFoundException fall back to the Spring Security Handler.
		throw e;
	}

	// Handler for custom forbidden exception.
	@ExceptionHandler({ForbiddenException.class})
	public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e) {
		ApiErrorCode apiErrorCode = ApiErrorCode.PERMISSION_DENIED;
		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				e.getDescription()
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for bean validation errors thrown in controllers.
	@ExceptionHandler({MethodArgumentNotValidException.class})
	public ResponseEntity<ErrorResponse> handleMethodArgumentsInvalidException(MethodArgumentNotValidException e) {
		ArrayList<TargetError> errors = new ArrayList<>();
		ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

		for (ObjectError error : e.getBindingResult().getAllErrors()) {
			if (error instanceof FieldError) {
				errors.add(new TargetError(((FieldError) error).getField(), error.getDefaultMessage()));
			} else {
				errors.add(new TargetError(error.getCode(), error.getDefaultMessage()));
			}
		}

		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				messageResolver.getMessage("exception.invalid.arguments"),
				errors.toArray(new TargetError[0])
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for bean validation errors in services.
	@SuppressWarnings("DuplicatedCode")
	@ExceptionHandler({ConstraintViolationException.class})
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
		ArrayList<TargetError> errors = new ArrayList<>();
		ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

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

		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				messageResolver.getMessage("exception.invalid.arguments"),
				errors.toArray(new TargetError[0])
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	@ExceptionHandler({MissingServletRequestParameterException.class})
	public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {

		ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				messageResolver.getMessage("exception.invalid.arguments"),
				new TargetError(e.getParameterName(), messageResolver.getMessage("javax.validation.constraints.NotNull.message"))
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for custom invalid arguments.
	@ExceptionHandler({InvalidArgumentsException.class})
	public ResponseEntity<ErrorResponse> handleInvalidArgumentException(InvalidArgumentsException e) {
		ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				messageResolver.getMessage("exception.invalid.arguments"),
				e.getErrors().toArray(new TargetError[0])
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for accessing url that don't support the Http media type (e.g. using form url encoded where only application/json is supported).
	@ExceptionHandler({HttpMediaTypeException.class})
	public ResponseEntity<ErrorResponse> handleHttpMediaTypeException(HttpMediaTypeException e) {
		ApiErrorCode apiErrorCode = ApiErrorCode.UNSUPPORTED_MEDIA_TYPE;
		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				e.getMessage()
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for accessing url that don't support the Http method (e.g. using HTTP POST where only HTTP GET is supported).
	@ExceptionHandler({HttpRequestMethodNotSupportedException.class})
	public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
		ApiErrorCode apiErrorCode = ApiErrorCode.METHOD_NOT_ALLOWED;
		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				e.getMessage()
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for accessing url that doesn't exist
	@ExceptionHandler({NoHandlerFoundException.class})
	public ResponseEntity<ErrorResponse> handleNoHandlerFoundExceptionException(NoHandlerFoundException e) {
		ApiErrorCode apiErrorCode = ApiErrorCode.URI_NOT_FOUND;
		ErrorResponse responseBody = new ErrorResponse(
				ApiErrorCode.URI_NOT_FOUND,
				messageResolver.getMessage("exception.uri.not.found", e.getRequestURL())
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for resources that are not found.
	@ExceptionHandler({ResourceNotFoundException.class})
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
		ApiErrorCode apiErrorCode = ApiErrorCode.RESOURCE_NOT_FOUND;
		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				messageResolver.getMessage("exception.resource.not.found", e.getResourceType(), e.getIdentifier())
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for sending malformed data or invalid data types (e.g. invalid json, using array instead of string).
	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, ConversionFailedException.class})
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(Exception e) {
		e.printStackTrace();

		ApiErrorCode apiErrorCode = ApiErrorCode.MESSAGE_NOT_READABLE;
		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				messageResolver.getMessage("exception.body.not.readable")
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for trying to create a resource when it already exists.
	@ExceptionHandler({ResourceAlreadyExistsException.class})
	public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
		ApiErrorCode apiErrorCode = ApiErrorCode.ALREADY_EXISTS;
		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				messageResolver.getMessage("exception.resource.already.exists", e.getResourceType()),
				new TargetError(e.getTarget(), messageResolver.getMessage("message.resource.already.exists", e.getValue()))
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

	// Handler for when the database is down.
	@ExceptionHandler({DataAccessResourceFailureException.class})
	public ResponseEntity<ErrorResponse> handleDataAccessResourceFailureException(DataAccessResourceFailureException e) {
		ApiErrorCode apiErrorCode = ApiErrorCode.UNAVAILABLE;

		ErrorResponse responseBody = new ErrorResponse(
				apiErrorCode,
				messageResolver.getMessage("exception.service.unavailable")
		);

		return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
	}

}