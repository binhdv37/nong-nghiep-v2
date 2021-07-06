package org.thingsboard.server.common.data;

import org.thingsboard.server.common.data.id.*;

public class GatewayLog extends SearchTextBasedWithAdditionalInfo<GatewayLogId> implements HasName, HasTenantId, HasCustomerId{
    private TenantId tenantId;
    private CustomerId customerId;
    private DamTomLogId damTomLogId;
    private Device device;
    private boolean active;

    public GatewayLog() {
        super();
    }

    public GatewayLog(GatewayLogId id) {
        super(id);
    }

    public GatewayLog(GatewayLogId gatewayLogId, TenantId tenantId, CustomerId customerId,
                      DamTomLogId damtomLogId, Device device,
                      boolean active) {
        this.setId(gatewayLogId);
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.damTomLogId = damtomLogId;
        this.device = device;
        this.active = active;
    }

    @Override
    public String getSearchText() {
        return device.getName();
    }

    public GatewayLog(GatewayLog gatewayLog) {
        super(gatewayLog);
        this.tenantId = gatewayLog.getTenantId();
        this.customerId = gatewayLog.getCustomerId();
        this.damTomLogId = gatewayLog.getDamTomLogId();
        this.device = gatewayLog.getDevice();
        this.active = gatewayLog.isActive();
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
        return device.getName();
    }

//    public void setName(String name) {
//        this.name = name;
//    }

    public DamTomLogId getDamTomLogId() {
        return damTomLogId;
    }

    public void setDamTomLogId(DamTomLogId damTomLogId) {
        this.damTomLogId = damTomLogId;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
