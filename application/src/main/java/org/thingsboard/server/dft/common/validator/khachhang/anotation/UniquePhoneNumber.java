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
@Constraint(validatedBy = UniquePhoneNumber.UniquePhoneNumberValidator.class)
@Documented
public @interface UniquePhoneNumber {

  String message() default "phone has been used.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class UniquePhoneNumberValidator implements ConstraintValidator<UniquePhoneNumber, String> {

    @Autowired
    DftUserRepository dftUserRepository;

    private String message;

    @Override
    public void initialize(UniquePhoneNumber constraintAnnotation) {
      message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
      phoneNumber = phoneNumber.trim();
      if(phoneNumber.startsWith("+84")) {
        phoneNumber = phoneNumber.replace("+84", "0");
      }
      else if(phoneNumber.startsWith("84")) {
        phoneNumber = phoneNumber.replaceFirst("84", "0");
      }
      if (dftUserRepository.findDistinctFirstByLastName(phoneNumber.trim()) != null) {
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
