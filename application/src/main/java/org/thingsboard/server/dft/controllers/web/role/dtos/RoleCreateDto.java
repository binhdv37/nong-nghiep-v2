package org.thingsboard.server.dft.controllers.web.role.dtos;

import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class RoleCreateDto {

    private UUID id;

    @NotBlank(message = "Tên vai trò không được bỏ trống")
    @Length(max = 255, message = "Tên vai trò không được quá 255 kí tự")
    private String name;

    @NotNull(message = "Ghi chú không được null")
    @Length(max = 4000, message = "Ghi chú không được quá 4000 kí tự")
    private String note;

    @Valid()
    @NotNull(message = "Danh sách quyền của vai trò không được null")
    private List<PermissionCreateDto> permissions;

    public RoleCreateDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Collection<PermissionCreateDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionCreateDto> permissions) {
        this.permissions = permissions;
    }
}
