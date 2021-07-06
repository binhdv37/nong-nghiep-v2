package org.thingsboard.server.dft.common.validator.khachhang.anotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.thingsboard.server.dft.repositories.DftUserRepository;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueEmail.UniqueEmailValidator.class)
@Documented
public @interface UniqueEmail {

  String message() default "email has been used.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    DftUserRepository dftUserRepository;

    private String message;

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
      message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
      if (dftUserRepository.findDistinctFirstByEmail(email) != null) {
        context
            .buildConstraintViolationWithTemplate(message.trim())
            .addConstraintViolation()
            .disableDefaultConstraintViolation();
        return false;
      }
      return true;
    }
  }
}
