package org.thingsboard.server.common.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.thingsboard.server.common.data.id.*;

public class Role extends SearchTextBasedWithAdditionalInfo<RoleId> implements HasName, HasTenantId, HasCustomerId{

    private TenantId tenantId;
    private CustomerId customerId;
    private String name;
    private String note;

    public Role() {
        super();
    }

    public Role(RoleId id) {
        super(id);
    }

    public Role(RoleId roleId, TenantId tenantId, CustomerId customerId, String name, String note) {
        this.setId(roleId);
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.name = name;
        this.note = note;
    }

    @Override
    public String getSearchText() {
        return name;
    }

    public Role(Role role) {
        super(role);
        this.tenantId = role.getTenantId();
        this.customerId = role.getCustomerId();
        this.name = role.getName();
        this.note = role.getNote();
    }

    @Override
    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public CustomerId getCustomerId() {
        return customerId;
    }

    public void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
    }

    @Override
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


//    @JsonIgnore
//    public boolean isSystemAdmin() {
//        return tenantId == null || EntityId.NULL_UUID.equals(tenantId.getId());
//    }
//
//    @JsonIgnore
//    public boolean isTenantAdmin() {
//        return !isSystemAdmin() && (customerId == null || EntityId.NULL_UUID.equals(customerId.getId()));
//    }
//
//    @JsonIgnore
//    public boolean isCustomerUser() {
//        return !isSystemAdmin() && !isTenantAdmin();
//    }

}
