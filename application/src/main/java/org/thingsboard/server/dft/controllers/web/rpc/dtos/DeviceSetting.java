package org.thingsboard.server.dft.controllers.web.rpc.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.entities.RpcSettingEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
public class DeviceSetting {
  private UUID id;
  private UUID deviceId;
  private String deviceName;
  private String label;
  private double valueControl;
  private long delayTime;
  private boolean callbackOption;
  private long timeCallback;
  private boolean loopOption;
  private int loopCount;
  private long loopTimeStep;
  private int commandId;
  private UUID groupRpcId;

  public DeviceSetting(RpcSettingEntity rpcSettingEntity) {
    this.id = rpcSettingEntity.getId();
    this.deviceId = rpcSettingEntity.getDeviceId();
    this.deviceName = rpcSettingEntity.getDeviceName();
    this.valueControl = rpcSettingEntity.getValueControl();
    this.delayTime = rpcSettingEntity.getDelayTime();
    this.callbackOption = rpcSettingEntity.isCallbackOption();
    this.timeCallback = rpcSettingEntity.getTimeCallback();
    this.loopOption = rpcSettingEntity.isLoopOption();
    this.loopCount = rpcSettingEntity.getLoopCount();
    this.loopTimeStep = rpcSettingEntity.getLoopTimeStep();
    this.commandId = rpcSettingEntity.getCommandId();
    this.groupRpcId = rpcSettingEntity.getGroupRpcId();
  }
}
