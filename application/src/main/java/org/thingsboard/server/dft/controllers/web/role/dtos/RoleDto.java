package org.thingsboard.server.dft.controllers.web.role.dtos;

import java.util.Collection;
import java.util.stream.Collectors;
import org.thingsboard.server.dft.entities.RoleEntity;

public class RoleDto {

    private String id;

    private String tenantId;

    private String name;

    private String note;

    private Collection<PermissionDto> permissions;

    private String createdBy;

    private long createdTime;

    private ErrorInfoDto errorInfo;

    public RoleDto() {
    }

    public RoleDto(RoleEntity entity) {
        this.id = entity.getId().toString();
        this.tenantId = entity.getTenantId().toString();
        this.name = entity.getName();
        this.note = entity.getNote();
        this.createdBy = entity.getCreatedBy().toString();
        this.createdTime = entity.getCreatedTime();
        this.permissions = entity.getPermissions()
            .stream()
            .map(PermissionDto::new)
            .collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public Collection<PermissionDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(
        Collection<PermissionDto> permissions) {
        this.permissions = permissions;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public ErrorInfoDto getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(ErrorInfoDto error) {
        this.errorInfo = error;
    }
}
