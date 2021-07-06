package org.thingsboard.server.dft.controllers.web.Alarm.dtos;

public class AlarmDeleteDto {
    private String alarmId;
    private String profile_Data;

    public AlarmDeleteDto() {
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public String getProfile_Data() {
        return profile_Data;
    }

    public void setProfile_Data(String profile_Data) {
        this.profile_Data = profile_Data;
    }
}
