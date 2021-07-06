package org.thingsboard.server.dft.controllers.web.rpc.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.entities.RpcScheduleEntity;
import org.thingsboard.server.dft.entities.RpcSettingEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
public class RpcScheduleDto {
    private UUID id;
    private UUID damTomId;
    private String name;

    private UUID rpcSettingId;
    private UUID deviceId;

    private UUID groupRpcId;

    private double valueControl;
    private String cron;
    private boolean active;
    private boolean callbackOption;
    private long timeCallback;
    private boolean loopOption;
    private int loopCount;
    private long loopTimeStep;

    private long createdTime;

    public RpcScheduleDto(RpcScheduleEntity rpcScheduleEntity) {
        this.id = rpcScheduleEntity.getId();
        this.damTomId = rpcScheduleEntity.getDamTomId();
        this.name = rpcScheduleEntity.getName();
        this.rpcSettingId = rpcScheduleEntity.getRpcSettingId();
        this.groupRpcId = rpcScheduleEntity.getRpcGroupId();
        this.cron = rpcScheduleEntity.getCron();
        this.active = rpcScheduleEntity.isActive();
        this.createdTime = rpcScheduleEntity.getCreatedTime();
    }

    public RpcScheduleDto(RpcScheduleEntity rpcScheduleEntity, RpcSettingEntity rpcSettingEntity) {
        this.id = rpcScheduleEntity.getId();
        this.damTomId = rpcScheduleEntity.getDamTomId();
        this.name = rpcScheduleEntity.getName();
        this.active = rpcScheduleEntity.isActive();
        this.cron = rpcScheduleEntity.getCron();

        this.rpcSettingId = rpcSettingEntity.getId();
        this.deviceId = rpcSettingEntity.getDeviceId();
        this.valueControl = rpcSettingEntity.getValueControl();
        this.callbackOption = rpcSettingEntity.isCallbackOption();
        this.timeCallback = rpcSettingEntity.getTimeCallback();
        this.loopOption = rpcSettingEntity.isLoopOption();
        this.loopCount = rpcSettingEntity.getLoopCount();
        this.loopTimeStep = rpcSettingEntity.getLoopTimeStep();
        this.createdTime = rpcScheduleEntity.getCreatedTime();
    }
}
