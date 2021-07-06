package org.thingsboard.server.common.data;

import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;
import org.thingsboard.server.common.data.id.AlarmRuleLogIdV2;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DamTomLogId;
import org.thingsboard.server.common.data.id.TenantId;

public class AlarmRuleLogV2 extends SearchTextBasedWithAdditionalInfo<AlarmRuleLogIdV2> implements HasName, HasTenantId, HasCustomerId{
    private TenantId tenantId;
    private CustomerId customerId;
    private DamTomLogId damTomLogId;
    private DeviceProfileAlarm deviceProfileAlarm;

    public AlarmRuleLogV2() {
        super();
    }

    public AlarmRuleLogV2(AlarmRuleLogIdV2 id) {
        super(id);
    }

    public AlarmRuleLogV2(AlarmRuleLogIdV2 alarmRuleLogIdV2, TenantId tenantId, CustomerId customerId,
                          DamTomLogId damtomLogId, DeviceProfileAlarm deviceProfileAlarm){
        super(alarmRuleLogIdV2);
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.damTomLogId = damtomLogId;
        this.deviceProfileAlarm = deviceProfileAlarm;
    }

    @Override
    public String getSearchText() {
        return deviceProfileAlarm.getAlarmType();
    }

    @Override
    public String getName() {
        return deviceProfileAlarm.getAlarmType();
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

    public DamTomLogId getDamTomLogId() {
        return damTomLogId;
    }

    public void setDamTomLogId(DamTomLogId damTomLogId) {
        this.damTomLogId = damTomLogId;
    }

    public DeviceProfileAlarm getDeviceProfileAlarm() {
        return deviceProfileAlarm;
    }

    public void setDeviceProfileAlarm(DeviceProfileAlarm deviceProfileAlarm) {
        this.deviceProfileAlarm = deviceProfileAlarm;
    }
}
