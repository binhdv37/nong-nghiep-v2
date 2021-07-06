package org.thingsboard.server.dft.services.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.DeviceSetting;
import org.thingsboard.server.dft.entities.RpcSettingEntity;
import org.thingsboard.server.dft.repositories.RpcSettingRepository;

import java.util.UUID;

@Service
public class RpcSettingService {

  private final RpcSettingRepository rpcSettingRepository;

  @Autowired
  public RpcSettingService(RpcSettingRepository rpcSettingRepository) {
    this.rpcSettingRepository = rpcSettingRepository;
  }

  public DeviceSetting saveOrUpdate(DeviceSetting deviceSetting) {
    RpcSettingEntity rpcSettingEntity = new RpcSettingEntity();
    if (deviceSetting.getId() == null) {
      rpcSettingEntity.setId(UUID.randomUUID());
    } else {
      rpcSettingEntity = rpcSettingRepository.findById(deviceSetting.getId()).get();
    }
    rpcSettingEntity.setDeviceId(deviceSetting.getDeviceId());
    rpcSettingEntity.setDeviceName(deviceSetting.getDeviceName());
    rpcSettingEntity.setValueControl(deviceSetting.getValueControl());
    rpcSettingEntity.setDelayTime(deviceSetting.getDelayTime());
    rpcSettingEntity.setCallbackOption(deviceSetting.isCallbackOption());
    rpcSettingEntity.setTimeCallback(deviceSetting.getTimeCallback());
    rpcSettingEntity.setLoopOption(deviceSetting.isLoopOption());
    rpcSettingEntity.setLoopCount(deviceSetting.getLoopCount());
    rpcSettingEntity.setLoopTimeStep(deviceSetting.getLoopTimeStep());
    rpcSettingEntity.setCommandId(0);
    rpcSettingEntity.setGroupRpcId(null);

    rpcSettingEntity = rpcSettingRepository.save(rpcSettingEntity);
    return new DeviceSetting(rpcSettingEntity);
  }

  public DeviceSetting getById(UUID id) {
    RpcSettingEntity rpcSettingEntity = new RpcSettingEntity();
    rpcSettingEntity = rpcSettingRepository.findById(id).get();
    return new DeviceSetting(rpcSettingEntity);
  }

  public void deleteById(UUID id) {
    rpcSettingRepository.deleteById(id);
  }
}
