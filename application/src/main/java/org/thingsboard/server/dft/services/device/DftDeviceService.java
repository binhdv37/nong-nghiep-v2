package org.thingsboard.server.dft.services.device;

import org.springframework.stereotype.Service;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dft.repositories.DftDeviceProfileRepository;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;

import java.util.UUID;

@Service
public class DftDeviceService {

  private final DftDeviceProfileRepository dftDeviceProfileRepository;
  private final DftDeviceRepository dftDeviceRepository;

  public DftDeviceService(
      DftDeviceProfileRepository dftDeviceProfileRepository,
      DftDeviceRepository dftDeviceRepository) {
    this.dftDeviceProfileRepository = dftDeviceProfileRepository;
    this.dftDeviceRepository = dftDeviceRepository;
  }

  public DeviceProfileEntity getDeviceProfileById(UUID id) {
    return dftDeviceProfileRepository.findById(id).get();
  }

  public DeviceEntity changeDeviceLabel(UUID tenantId, UUID deviceId, String label) {
    DeviceEntity deviceEntity =
        dftDeviceRepository.findDeviceEntityByIdAndTenantId(deviceId, tenantId);
    deviceEntity.setLabel(label);
    return dftDeviceRepository.save(deviceEntity);
  }
}
