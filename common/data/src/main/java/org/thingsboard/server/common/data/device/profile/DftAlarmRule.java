package org.thingsboard.server.common.data.device.profile;

import java.util.List;
import java.util.UUID;

public class DftAlarmRule {
    private List<UUID> gatewayIds;
    private boolean viaSms;
    private boolean viaEmail;
    private boolean viaNotification;
    private List<UUID> groupRpcIds;
    private List<UUID> rpcSettingIds;
    private boolean rpcAlarm; // true -> luat dieu khien, false -> luat canh bao
    private boolean active;
    private Long createdTime;
//    private String checkChenhLech;
//    private long thoiGianChenhLech;
//    private int nguongChenhLech;

    public DftAlarmRule() {
    }

    public List<UUID> getGatewayIds() {
        return gatewayIds;
    }

    public void setGatewayIds(List<UUID> gatewayIds) {
        this.gatewayIds = gatewayIds;
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

    public List<UUID> getGroupRpcIds() {
        return groupRpcIds;
    }

    public void setGroupRpcIds(List<UUID> groupRpcIds) {
        this.groupRpcIds = groupRpcIds;
    }

    public List<UUID> getRpcSettingIds() {
        return rpcSettingIds;
    }

    public void setRpcSettingIds(List<UUID> rpcSettingIds) {
        this.rpcSettingIds = rpcSettingIds;
    }

    public boolean isRpcAlarm() {
        return rpcAlarm;
    }

    public void setRpcAlarm(boolean rpcAlarm) {
        this.rpcAlarm = rpcAlarm;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
}
