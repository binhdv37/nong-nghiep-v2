package org.thingsboard.server.dft.common.validator.khachhang.anotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangEditDto;
import org.thingsboard.server.dft.repositories.KhachHangRepository;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueEditMaKhachHang.UniqueEditMaKhachHangValidator.class)
@Documented
public @interface UniqueEditMaKhachHang {
  String message() default "ma_khach_hang has been used.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};


  class UniqueEditMaKhachHangValidator
      implements ConstraintValidator<UniqueEditMaKhachHang, Object> {

    @Autowired KhachHangRepository khachHangRepository;

    private String message;

    @Override
    public void initialize(final UniqueEditMaKhachHang constraintAnnotation) {
      message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
      try {
        KhachHangEditDto khachHangEditDto = (KhachHangEditDto) value;
        if (khachHangRepository.findAllByMaKhachHangAndIdNotEqual(khachHangEditDto.getMaKhachHang().trim(), khachHangEditDto.getId())
            != null) {
          context
              .buildConstraintViolationWithTemplate(message)
              .addConstraintViolation()
              .disableDefaultConstraintViolation();
          return false;
        }
      } catch (final Exception ignore) {
        // ignore
      }
      return true;
    }
  }
}
