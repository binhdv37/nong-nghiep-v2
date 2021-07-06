package org.thingsboard.server.dft.services.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;
import org.thingsboard.server.common.data.device.profile.DeviceProfileData;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dft.repositories.DftDeviceProfileRepository;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;

import java.util.List;
import java.util.UUID;

@Service
public class RpcAlarmService {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final DftDeviceProfileRepository deviceProfileRepository;
  private final DftDeviceRepository dftDeviceRepository;

  @Autowired
  public RpcAlarmService(
      DftDeviceProfileRepository deviceProfileRepository, DftDeviceRepository dftDeviceRepository) {
    this.deviceProfileRepository = deviceProfileRepository;
    this.dftDeviceRepository = dftDeviceRepository;
  }

  public DeviceProfileEntity findDeviceProfileByDeviceId(UUID deviceId) {
    DeviceEntity deviceEntity = dftDeviceRepository.findDeviceEntityById(deviceId);
    DeviceProfileEntity deviceProfile =
        deviceProfileRepository.findById(deviceEntity.getDeviceProfileId()).get();
    return deviceProfile;
  }

  public DeviceProfileData getDeviceProfileDataByIdGateway(UUID deviceId) {
    DeviceProfileEntity deviceProfile = findDeviceProfileByDeviceId(deviceId);
    DeviceProfileData deviceProfileData = new DeviceProfileData();
    try {
      deviceProfileData =
          objectMapper.treeToValue(deviceProfile.getProfileData(), DeviceProfileData.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return deviceProfileData;
  }

  public DeviceProfileAlarm getAlarmRuleByAlarmType(UUID gatewayId, String alarmType) {
    DeviceProfileData deviceProfileData = getDeviceProfileDataByIdGateway(gatewayId);
    List<DeviceProfileAlarm> deviceProfileAlarmList = deviceProfileData.getAlarms();
    for (DeviceProfileAlarm deviceProfileAlarm : deviceProfileAlarmList) {
      if (deviceProfileAlarm.getAlarmType().equalsIgnoreCase(alarmType)) {
        return deviceProfileAlarm;
      }
    }
    return null;
  }
}
