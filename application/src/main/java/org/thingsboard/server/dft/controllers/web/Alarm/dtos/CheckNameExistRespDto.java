package org.thingsboard.server.dft.controllers.web.Alarm.dtos;

public class CheckNameExistRespDto {
    private boolean exist;
    private String alarmType;

    public CheckNameExistRespDto() {
    }

    public CheckNameExistRespDto(boolean exist, String alarmType) {
        this.exist = exist;
        this.alarmType = alarmType;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }
}
