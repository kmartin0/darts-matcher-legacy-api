package com.dartsmatcher.legacy.validators.nowhitespace;

import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NoWhitespaceValidator implements ConstraintValidator<NoWhitespace, String> {

	@Override
	public void initialize(NoWhitespace constraintAnnotation) {
	}

	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		return !StringUtils.containsWhitespace(s);
	}
}
