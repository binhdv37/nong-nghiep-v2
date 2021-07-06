package org.thingsboard.server.dft.common.validator.khachhang.anotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangEditDto;
import org.thingsboard.server.dft.entities.KhachHangEntity;
import org.thingsboard.server.dft.repositories.DftUserRepository;
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
@Constraint(validatedBy = UniqueEditPhoneNumber.UniqueEditPhoneNumberValidator.class)
@Documented
public @interface UniqueEditPhoneNumber {
  String message() default "ma_khach_hang has been used.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class UniqueEditPhoneNumberValidator
      implements ConstraintValidator<UniqueEditPhoneNumber, Object> {

    @Autowired
    DftUserRepository dftUserRepository;

    @Autowired
    KhachHangRepository khachHangRepository;

    private String message;

    @Override
    public void initialize(final UniqueEditPhoneNumber constraintAnnotation) {
      message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
      try {
        KhachHangEditDto khachHangDto = (KhachHangEditDto) value;
        khachHangDto.setSoDienThoai(khachHangDto.getSoDienThoai().trim());
        if(khachHangDto.getSoDienThoai().startsWith("+84")) {
          khachHangDto.setSoDienThoai(khachHangDto.getSoDienThoai().replace("+84", "0"));
        } else if(khachHangDto.getSoDienThoai().startsWith("84")) {
          khachHangDto.setSoDienThoai(khachHangDto.getSoDienThoai().replaceFirst("84", "0"));
        }
        KhachHangEntity khachHangEntity = khachHangRepository.findById(khachHangDto.getId()).get();
        if (!dftUserRepository.findUserEntitiesByTenantIdNotLikeAndLastName(khachHangEntity.getTenantEntity().getId(), khachHangDto.getSoDienThoai().trim()).isEmpty()) {
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
