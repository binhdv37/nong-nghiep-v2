package org.thingsboard.server.dft.controllers.web.users.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.thingsboard.server.common.data.SearchTextBasedWithAdditionalInfo;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.entities.RoleEntity;

import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.UUID;

@Data
@NoArgsConstructor
public class UsersDto extends SearchTextBasedWithAdditionalInfo<UserId> {

    private UserId id;

    @NotBlank(message = "Full name is mandatory")
    @Length(max = 255, message = "Full name should not be greater than 255 characters")
    private String firstName;

    @NotBlank(message = "Email is mandatory")
    @Length(max = 320, message = "Email should not be greater than 320 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+([._-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+([.-]?[a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}$", message = "Email should be valid")
    private String email;

    @NotNull(message = "Phone number should not be null")
    @Length(max = 45, message = "Phone number should not be greater than 45 characters")
    @Pattern(regexp = "(^$)|(^\\+?[0-9]{7,44}$)", message = "Phone number should be valid")
    private String lastName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Length(min = 6, max = 255, message = "Password should be from 6 to 255 characters")
    @Pattern(regexp = "^[^\\sáàảãạÁÀẢÃẠăắằẳẵặĂẮẰẲẴẶâấầẩẫậÂẤẦẨẪẬéèẻẽẹÉÈẺẼẸêếềểễệÊẾỀỂỄỆíìỉĩịÍÌỈĨỊýỳỷỹỵÝỲỶỸỴóòỏõọÓÒỎÕỌôồốổỗộÔỒỐỔỖỘơớờởỡợƠỚỜỞỠỢúùủũụÚÙỦŨỤưứừửữựƯỨỪỬỮỰ]{6,}$", message = "Password should be valid")
    private String password;

    private JsonNode additionalInfo;

    private boolean enabled;

    private long createdTime;

    private TenantId tenantId;

    private CustomerId customerId;

    private int responseCode;

    private String responseMessage;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ArrayList<RoleEntity> roleEntity;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private ArrayList<UUID> roleId;

    public UsersDto(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.email = user.getEmail();
        this.lastName = user.getLastName();
        this.additionalInfo = user.getAdditionalInfo();
        this.createdTime = user.getCreatedTime();
        this.tenantId = user.getTenantId();
        this.customerId = user.getCustomerId();
    }

    public UsersDto(UserEntity userEntity) {
        this.id = new UserId(userEntity.getId());
        this.firstName = userEntity.getFirstName();
        this.email = userEntity.getEmail();
        this.lastName = userEntity.getLastName();
        this.additionalInfo = userEntity.getAdditionalInfo();
        this.createdTime = userEntity.getCreatedTime();
        this.tenantId = new TenantId(userEntity.getTenantId());
        this.customerId = new CustomerId(userEntity.getCustomerId());
    }

    @Override
    public String getSearchText() {
        return getEmail();
    }
}
