package org.thingsboard.server.dft.common.validator.khachhang.anotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.thingsboard.server.dft.repositories.KhachHangRepository;

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
@Constraint(validatedBy = UniqueMaKhachHang.UniqueMaKhachHangValidator.class)
@Documented
public @interface UniqueMaKhachHang {

  String message() default "ma_khach_hang has been used.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class UniqueMaKhachHangValidator implements ConstraintValidator<UniqueMaKhachHang, String> {

    @Autowired KhachHangRepository khachHangRepository;

    private String message;

    @Override
    public void initialize(UniqueMaKhachHang constraintAnnotation) {
      message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String maKhachHang, ConstraintValidatorContext context) {
      if (khachHangRepository.findAllByMaKhachHang(maKhachHang.trim()) != null) {
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
