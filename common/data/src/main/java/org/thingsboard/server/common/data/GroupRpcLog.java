package org.thingsboard.server.common.data;

import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DamTomLogId;
import org.thingsboard.server.common.data.id.GroupRpcLogId;
import org.thingsboard.server.common.data.id.TenantId;

public class GroupRpcLog extends SearchTextBasedWithAdditionalInfo<GroupRpcLogId> implements HasName, HasTenantId, HasCustomerId {

    private TenantId tenantId;
    private CustomerId customerId;
    private DamTomLogId damTomLogId;
    private String name;
    private String ghiChu;

    public GroupRpcLog(GroupRpcLogId id, TenantId tenantId, CustomerId customerId,
                       DamTomLogId damTomLogId, String name, String ghiChu) {
        this.setId(id);
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.damTomLogId = damTomLogId;
        this.name = name;
        this.ghiChu = ghiChu;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
    }

    public DamTomLogId getDamTomLogId() {
        return damTomLogId;
    }

    public void setDamTomLogId(DamTomLogId damTomLogId) {
        this.damTomLogId = damTomLogId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public CustomerId getCustomerId() {
        return customerId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TenantId getTenantId() {
        return tenantId;
    }

    @Override
    public String getSearchText() {
        return name;
    }
}
