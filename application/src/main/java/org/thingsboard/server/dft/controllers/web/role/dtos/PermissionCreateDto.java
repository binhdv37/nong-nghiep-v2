package org.thingsboard.server.dft.controllers.web.role.dtos;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public class PermissionCreateDto {

    @NotBlank
    @Length(max = 255, message = "Tên quyền không được quá 255 kí tự")
    private String name;

    public PermissionCreateDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
