package org.thingsboard.server.dft.common.validator.khachhang.anotation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = NoSpace.NoSpaceValidator.class)
@Documented
public @interface NoSpace {

    String message() default "Field not allow space.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class NoSpaceValidator implements ConstraintValidator<NoSpace, String> {

        private String message;

        @Override
        public void initialize(NoSpace constraintAnnotation) {
            message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            Pattern pattern = Pattern.compile("\\s\\s+");
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                context
                        .buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
            return true;
        }
    }
}
