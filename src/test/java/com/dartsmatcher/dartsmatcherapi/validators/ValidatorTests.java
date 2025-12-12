package com.dartsmatcher.dartsmatcherapi.validators;

import com.dartsmatcher.legacy.validators.nowhitespace.NoWhitespaceValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

@ExtendWith(MockitoExtension.class)
public class ValidatorTests {

	private NoWhitespaceValidator noWhitespaceValidator;

	@Mock
	private ConstraintValidatorContext constraintValidatorContext;

	@BeforeEach
	void setup() {
		noWhitespaceValidator = new NoWhitespaceValidator();
	}

	@Test
	void testStringWithWhitespace_returnFalse() {
		Assertions.assertFalse(noWhitespaceValidator.isValid("contains whitespace", constraintValidatorContext));
	}

	@Test
	void testStringWithoutWhitespace_returnTrue() {
		Assertions.assertTrue(noWhitespaceValidator.isValid("word", constraintValidatorContext));
	}

}
