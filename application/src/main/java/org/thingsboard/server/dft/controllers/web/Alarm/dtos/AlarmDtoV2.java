package org.thingsboard.server.dft.controllers.web.Alarm.dtos;

import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class AlarmDtoV2 {
    /*
        Qui tắc lưu tên alarm : (deviceProfileAlarm.alarmType)
        - Luật cảnh báo : Thêm prefix "ALARM-"
        - Luật điều khiển : Thêm prefix "RPC-"
     */
    @NotNull
    private UUID damtomId;

    @NotNull
    private DeviceProfileAlarm deviceProfileAlarm;

    public AlarmDtoV2() {
    }

    public UUID getDamtomId() {
        return damtomId;
    }

    public void setDamtomId(UUID damtomId) {
        this.damtomId = damtomId;
    }

    public DeviceProfileAlarm getDeviceProfileAlarm() {
        return deviceProfileAlarm;
    }

    public void setDeviceProfileAlarm(DeviceProfileAlarm deviceProfileAlarm) {
        this.deviceProfileAlarm = deviceProfileAlarm;
    }
}
