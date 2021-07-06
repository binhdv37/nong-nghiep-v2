package org.thingsboard.server.dft.services.DamTomAlarm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dft.entities.DamTomAlarmEntity;
import org.thingsboard.server.dft.entities.DamTomGatewayEntity;
import org.thingsboard.server.dft.repositories.DamTomAlarmRepository;
import org.thingsboard.server.dft.repositories.DftDeviceProfileRepository;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;
import org.thingsboard.server.dft.services.DamTomService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DamTomAlarmService {
    @Autowired
    DamTomAlarmRepository damTomAlarmRepository;
    @Autowired
    DftDeviceProfileRepository dftDeviceProfileRepository;

    @Autowired
    DftDeviceRepository dftDeviceRepository;

    @Autowired
    DamTomService damTomService;

    public DamTomAlarmEntity saveDamTomAlarm(DamTomAlarmEntity damTomAlarmEntity) {
        return this.damTomAlarmRepository.save(damTomAlarmEntity);
    }

    public DeviceProfileEntity saveDeviceProfile(DeviceProfileEntity deviceProfileEntity) {
        return this.dftDeviceProfileRepository.save(deviceProfileEntity);
    }

    public DamTomAlarmEntity findByDamTomAlarmId(UUID tenantId, String damTomAlarmId) {
        return this.damTomAlarmRepository.findByTenantIdAndAlarmId(tenantId, damTomAlarmId);
    }
    public List<DamTomAlarmEntity> findAllByAlarmIdAndTenantId(UUID tenantId, String damTomAlarmId) {
        return  this.damTomAlarmRepository.findAllByAlarmIdAndTenantId(damTomAlarmId,tenantId);
    }

    public List<DamTomAlarmEntity> findDistinctByAlarmIdAndDamtomIdAndTenantId(UUID damTomId, UUID tenantId){
        return this.damTomAlarmRepository.findAllByDamtomIdAndTenantId(damTomId,tenantId);
    }

    public void deleteDamTomAlarm(UUID tenantId, String alarmId) {
        this.damTomAlarmRepository.delete(damTomAlarmRepository.findByTenantIdAndAlarmId(tenantId, alarmId));
    }
    public void deleteDamTomAlarm(DamTomAlarmEntity damTomAlarmEntity){
        this.damTomAlarmRepository.delete(damTomAlarmEntity);
    }

    public DamTomAlarmEntity findByDamTomIdAndName(UUID damTomId, String name) {
       return this.damTomAlarmRepository.findByDamtomIdAndName(damTomId, name);
    }

    public List<DamTomGatewayEntity> getListDamTomGatewayOfDamTom(UUID tenantId,UUID damtomId){
        return damTomService.getListGateway(tenantId,damtomId);
    }
    public List<DeviceEntity> getListDevice(UUID tenantId, UUID damtomId){
        List<UUID> listDeviceId = new ArrayList<>();
        List<DamTomGatewayEntity> damTomGatewayEntityList = this.getListDamTomGatewayOfDamTom(tenantId,damtomId);
        for(DamTomGatewayEntity damTomGateway : damTomGatewayEntityList){
            listDeviceId.add(damTomGateway.getDevice().getId());
        }
       return dftDeviceRepository.findAllByIdIn(listDeviceId);
    }

    public List<DeviceProfileEntity> getDeviceProfileEntityList(UUID tenantId, UUID damTomId){
        List<UUID> listDeviceId = new ArrayList<>();
        List<DeviceEntity> deviceEntityList = this.getListDevice(tenantId,damTomId);
        for (DeviceEntity deviceEntity : deviceEntityList){
            listDeviceId.add(deviceEntity.getDeviceProfileId());
        }
        return dftDeviceProfileRepository.findAllByIdIn(listDeviceId);
    }

    public List<DamTomAlarmEntity> getAllAlarmOfDamTomId(UUID tenantId,UUID damTomId){
        return damTomAlarmRepository.findAllByDamtomIdAndTenantId(damTomId,tenantId);
    }




}
