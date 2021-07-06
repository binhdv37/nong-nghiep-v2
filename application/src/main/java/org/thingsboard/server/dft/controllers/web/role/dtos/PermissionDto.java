package org.thingsboard.server.dft.controllers.web.role.dtos;

import org.thingsboard.server.dft.entities.PermissionEntity;

import java.util.UUID;

public class PermissionDto {

    private String id;

    private String tenantId;

    private String roleId;

    private String name;

    private String createdBy;

    private long createdTime;

    public PermissionDto() {
    }

    public PermissionDto(PermissionEntity entity) {
        this.id = entity.getId().toString();
        this.tenantId = entity.getTenantId().toString();
        this.name = entity.getName();
        this.createdBy = entity.getCreatedBy().toString();
        this.createdTime = entity.getCreatedTime();
        this.roleId = entity.getRoleId().toString();
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

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
