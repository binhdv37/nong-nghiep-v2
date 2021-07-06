package org.thingsboard.server.dft.controllers.web.Alarm.dtos;

import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;

import java.util.UUID;

public class AlarmDto {
    private String alarmId;
    private UUID tenantId;
    private String profile_Data;
    private UUID damtomId;
    private String name;
    private String note;
    private boolean viaSms;
    private boolean viaEmail;
    private boolean viaNotification;
    private UUID createdBy;
    private long createdTime;
    private String thucHienDieuKhien;

    public AlarmDto() {
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public String getThucHienDieuKhien() {
        return thucHienDieuKhien;
    }

    public void setThucHienDieuKhien(String thucHienDieuKhien) {
        this.thucHienDieuKhien = thucHienDieuKhien;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getProfile_Data() {
        return profile_Data;
    }

    public void setProfile_Data(String profile_Data) {
        this.profile_Data = profile_Data;
    }

    public UUID getDamtomId() {
        return damtomId;
    }

    public void setDamtomId(UUID damtomId) {
        this.damtomId = damtomId;
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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
