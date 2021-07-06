package org.thingsboard.server.dft.entities;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "damtom_user_active")
public class UserActiveEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name  = "user_id")
    private UUID userId;

    @Column(name = "active")
    private boolean active;

    @Column(name = "activate_code")
    private String activeCode;

    @Column(name = "created_time")
    private long createdTime;

    public UserActiveEntity() {
    }

    public UserActiveEntity(UUID id, UUID tenantId, UUID userId, boolean active, String activeCode, long createdTime) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.active = active;
        this.activeCode = activeCode;
        this.createdTime = createdTime;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getActiveCode() {
        return activeCode;
    }

    public void setActiveCode(String activeCode) {
        this.activeCode = activeCode;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
