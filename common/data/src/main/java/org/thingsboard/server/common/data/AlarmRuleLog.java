package org.thingsboard.server.common.data;

import org.thingsboard.server.common.data.id.*;

import java.util.UUID;

public class AlarmRuleLog extends SearchTextBasedWithAdditionalInfo<AlarmRuleLogId> implements HasName, HasTenantId, HasCustomerId{
    private TenantId tenantId;
    private CustomerId customerId;
    private DamTomLogId damTomLogId;
    private DeviceProfile deviceProfile;
    private String alarmId;
    private String name;
    private String note;
    private boolean viaSms;
    private boolean viaEmail;
    private boolean viaNotification;
    private UUID groupRpcId;

    public AlarmRuleLog() {
        super();
    }

    public AlarmRuleLog(AlarmRuleLogId id) {
        super(id);
    }

    public AlarmRuleLog(AlarmRuleLogId alarmRuleLogId, TenantId tenantId, CustomerId customerId,
                     DamTomLogId damtomLogId, DeviceProfile deviceProfile,
                        String alarmId, String name, String note,
                        boolean viaEmail, boolean viaSms,
                        boolean viaNotification, UUID groupRpcId){
        this.setId(alarmRuleLogId);
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.damTomLogId = damtomLogId;
        this.deviceProfile = deviceProfile;
        this.alarmId = alarmId;
        this.name = name;
        this.note = note;
        this.viaEmail = viaEmail;
        this.viaSms = viaSms;
        this.viaNotification = viaNotification;
        this.groupRpcId = groupRpcId;
    }

    @Override
    public String getSearchText() {
        return name;
    }

    public AlarmRuleLog(AlarmRuleLog alarmRuleLog) {
        super(alarmRuleLog);
        this.tenantId = alarmRuleLog.getTenantId();
        this.customerId = alarmRuleLog.getCustomerId();
        this.damTomLogId = alarmRuleLog.getDamTomLogId();
        this.deviceProfile = alarmRuleLog.getDeviceProfile();
        this.alarmId = alarmRuleLog.getAlarmId();
        this.name = alarmRuleLog.getName();
        this.note = alarmRuleLog.getNote();
        this.viaSms = alarmRuleLog.isViaSms();
        this.viaEmail = alarmRuleLog.isViaEmail();
        this.viaNotification = alarmRuleLog.isViaNotification();
        this.groupRpcId = alarmRuleLog.getGroupRpcId();
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

    public DamTomLogId getDamTomLogId() {
        return damTomLogId;
    }

    public void setDamTomLogId(DamTomLogId damTomLogId) {
        this.damTomLogId = damTomLogId;
    }

    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    public void setDeviceProfile(DeviceProfile deviceProfile) {
        this.deviceProfile = deviceProfile;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isViaSms() {
        return viaSms;
    }

    public void setViaSms(boolean viaSms) {
        this.viaSms = viaSms;
    }

    public boolean isViaEmail() {
        return viaEmail;
    }

    public void setViaEmail(boolean viaEmail) {
        this.viaEmail = viaEmail;
    }

    public boolean isViaNotification() {
        return viaNotification;
    }

    public void setViaNotification(boolean viaNotification) {
        this.viaNotification = viaNotification;
    }

    public UUID getGroupRpcId() {
        return groupRpcId;
    }

    public void setGroupRpcId(UUID groupRpcId) {
        this.groupRpcId = groupRpcId;
    }
}
