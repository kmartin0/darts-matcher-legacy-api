package com.dartsmatcher.legacy.validators.nowhitespace;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = NoWhitespaceValidator.class)
@Documented
public @interface NoWhitespace {
	String message() default "{message.no.whitespace.allowed}";
	Class<?>[] groups() default { };
	Class<? extends Payload>[] payload() default { };
}
