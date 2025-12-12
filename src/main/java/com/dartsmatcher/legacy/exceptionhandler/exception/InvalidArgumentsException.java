package com.dartsmatcher.legacy.exceptionhandler.exception;

import com.dartsmatcher.legacy.exceptionhandler.response.TargetError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InvalidArgumentsException extends RuntimeException {

	// Errors map where the key is the target and the value is the error message.
	private ArrayList<TargetError> errors;

	public InvalidArgumentsException(TargetError... errors) {
		this.errors = Stream.of(errors).collect(Collectors.toCollection(ArrayList::new));
	}

}
