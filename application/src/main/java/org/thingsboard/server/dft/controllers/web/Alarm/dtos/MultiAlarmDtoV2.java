package org.thingsboard.server.dft.controllers.web.Alarm.dtos;

import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class MultiAlarmDtoV2 {
    @NotNull
    private UUID damtomId;

    @NotNull
    private List<DeviceProfileAlarm> alarmList;

    public MultiAlarmDtoV2() {
    }

    public UUID getDamtomId() {
        return damtomId;
    }

    public void setDamtomId(UUID damtomId) {
        this.damtomId = damtomId;
    }

    public List<DeviceProfileAlarm> getAlarmList() {
        return alarmList;
    }

    public void setAlarmList(List<DeviceProfileAlarm> alarmList) {
        this.alarmList = alarmList;
    }
}
