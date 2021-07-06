package org.thingsboard.server.dft.common.validator.bodieukhien;

import org.springframework.beans.factory.annotation.Autowired;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.GroupRpcDto;
import org.thingsboard.server.dft.repositories.GroupRpcRepository;

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
@Constraint(validatedBy = UniqueGroupRpcName.UniqueGroupRpcNameValidator.class)
@Documented
public @interface UniqueGroupRpcName {
    String message() default "group rpc name has been used.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    class UniqueGroupRpcNameValidator
            implements ConstraintValidator<UniqueGroupRpcName, Object> {

        @Autowired
        GroupRpcRepository groupRpcRepository;

        private String message;

        @Override
        public void initialize(final UniqueGroupRpcName constraintAnnotation) {
            message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(final Object value, final ConstraintValidatorContext context) {
            try {
                GroupRpcDto groupRpcDto = (GroupRpcDto) value;
                if(groupRpcDto.getGroupRpcId() == null) {
                    if (groupRpcRepository.existsGroupRpcEntitiesByDamTomIdAndTen(groupRpcDto.getDamTomId(), groupRpcDto.getName().trim())) {
                        context
                                .buildConstraintViolationWithTemplate(message)
                                .addConstraintViolation()
                                .disableDefaultConstraintViolation();
                        return false;
                    }
                } else {
                    if (groupRpcRepository.existsGroupRpcEntitiesByDamTomIdAndTenAndIdNot(groupRpcDto.getDamTomId(), groupRpcDto.getName().trim(), groupRpcDto.getGroupRpcId())) {
                        context
                                .buildConstraintViolationWithTemplate(message)
                                .addConstraintViolation()
                                .disableDefaultConstraintViolation();
                        return false;
                    }
                }
            } catch (final Exception ignore) {
                // ignore
            }
            return true;
        }
    }
}
