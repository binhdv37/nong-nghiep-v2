package org.thingsboard.server.dft.controllers.web.Alarm.dtos;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class CheckNameExistDto {
    @NotNull
    private UUID damtomId;

    @NotNull
    private String[] alarmTypes;

    public CheckNameExistDto() {
    }

    public UUID getDamtomId() {
        return damtomId;
    }

    public void setDamtomId(UUID damtomId) {
        this.damtomId = damtomId;
    }

    public String[] getAlarmTypes() {
        return alarmTypes;
    }

    public void setAlarmTypes(String[] alarmTypes) {
        this.alarmTypes = alarmTypes;
    }
}
