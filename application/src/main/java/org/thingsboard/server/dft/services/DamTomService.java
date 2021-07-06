package org.thingsboard.server.dft.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.DeviceProfileProvisionType;
import org.thingsboard.server.common.data.DeviceProfileType;
import org.thingsboard.server.common.data.DeviceTransportType;
import org.thingsboard.server.dao.model.sql.AssetEntity;
import org.thingsboard.server.dao.model.sql.CustomerEntity;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.sql.asset.AssetRepository;
import org.thingsboard.server.dft.common.constants.EntityNameConstant;
import org.thingsboard.server.dft.controllers.web.damtom.dtos.DamTomCreateDto;
import org.thingsboard.server.dft.controllers.web.damtom.dtos.DamTomDto;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomGatewayEntity;
import org.thingsboard.server.dft.entities.DamTomStaffEntity;
import org.thingsboard.server.dft.repositories.*;

import java.util.*;

@Service
@Slf4j
public class DamTomService {

    @Autowired
    private DamTomRepository damTomRepository;
    @Autowired
    private DamTomStaffRepository damTomStaffRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private GateWayRepository gateWayRepository;
    @Autowired
    private DftDeviceRepository dftDeviceRepository;
    @Autowired
    DftDeviceProfileRepository dftDeviceProfileRepository;
    @Autowired
    DftCustomerRepository dftCustomerRepository;

    public Page<DamTomEntity> getDamTomByTenantIdAndSearchText(UUID tenantId, String searchText, Pageable pageable) {
        return damTomRepository.findAllByTenantId(searchText, tenantId, pageable);
    }

    public List<DamTomEntity> getDamTomByTenantIdAndSearchTextMobi(UUID tenantId, String searchText) {
        return damTomRepository.findAllByTenantIdMobi(searchText, tenantId);
    }

    public List<DamTomEntity> getDamTomActiveByTenantIdAndSearchTextMobi(UUID tenantId, String searchText) {
        return damTomRepository.findAllByTenantIdActiveMobi(searchText, tenantId);
    }

    public Page<DamTomEntity> getDamTomByListID(String searchText, List<UUID> list, UUID idCreatedBy, Pageable pageable) {
        return damTomRepository.findByIdInOrCreatedBy( searchText,list, idCreatedBy, pageable);
    }

    public List<DamTomEntity> getDamTomByListIDMobi(List<UUID> list, UUID idCreatedBy) {
        return damTomRepository.findByIdInOrCreatedByOrderByCreatedTimeDesc(list, idCreatedBy);
    }
    public List<DamTomEntity> getDamTomActiveByListIDMobi(List<UUID> list, UUID idCreatedBy) {
        return damTomRepository.findAllByActiveIsTrueAndIdInOrCreatedByAndActiveOrderByCreatedTimeDesc(list, idCreatedBy, true);
    }

    public List<DamTomStaffEntity> getListOfDamTomStaff(UUID staffId) {
        return damTomStaffRepository.findAllByStaffId(staffId);
    }


    public DamTomDto save(UUID tenantId, DamTomCreateDto damTomCreateDto, UUID userId) throws JsonProcessingException {

        if (damTomCreateDto.getId() == null) {
            //create
            return this.CreateDamTom(tenantId, damTomCreateDto, userId);
        } else {
            //edit
            return this.EditDamTom(tenantId, damTomCreateDto, userId);
        }


    }

    public List<DamTomEntity> findAllByTenantIdAndName(UUID tenantId, String name) {
        return damTomRepository.findAllByTenantIdAndName(tenantId, name);
    }

    public DamTomEntity getDamTomById(UUID tenantId, UUID id) {
        return damTomRepository.findDamTomEntityByTenantIdAndId(tenantId, id);
    }

    public void deleteDamtom(UUID tenantId, UUID id) {
        DamTomEntity damTomEntity = damTomRepository.findDamTomEntityByTenantIdAndId(tenantId, id);
        UUID assetId = damTomEntity.getAsset().getId();
        UUID deviceProfileId = damTomEntity.getDeviceProfile().getId();
        damTomRepository.delete(damTomEntity);
        assetRepository.deleteById(assetId);
        dftDeviceProfileRepository.deleteById(deviceProfileId);

    }

    public DamTomDto CreateDamTom(UUID tenantId, DamTomCreateDto damTomCreateDto, UUID userId) throws JsonProcessingException {
        DamTomEntity damTomEntity = damTomCreateDto.toDamTomEntity();
        damTomEntity.setSearchText(damTomEntity.getName().toLowerCase());
        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setId(UUID.randomUUID());
        assetEntity.setName(damTomEntity.getName().toLowerCase());
        assetEntity.setCreatedTime(damTomEntity.getCreatedTime());
        assetEntity.setType("dam_tom");
        assetEntity.setSearchText(damTomEntity.getName().toLowerCase());
        assetEntity.setTenantId(damTomEntity.getTenantId());
        CustomerEntity customerEntity = dftCustomerRepository.findDistinctFirstByTenantId(tenantId);
        assetEntity.setCustomerId(customerEntity.getTenantId());
        assetEntity.setTenantId(tenantId);
        ObjectMapper objectMapper = new ObjectMapper();
        assetRepository.save(assetEntity);
        damTomEntity.setAsset(assetEntity);
        damTomEntity.setTenantId(tenantId);
        List<DamTomStaffEntity> listdt = new ArrayList<>();
        for (int i = 0; i < damTomCreateDto.getStaffs().size(); i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUuid(UUID.fromString(damTomCreateDto.getStaffs().get(i)));
            DamTomStaffEntity damTomStaffEntity = new DamTomStaffEntity();
            damTomStaffEntity.setDamtomId(damTomEntity.getId());
            damTomStaffEntity.setStaff(userEntity);
            damTomStaffEntity.setCreatedTime(damTomEntity.getCreatedTime());
            damTomStaffEntity.setCreatedBy(UUID.fromString(userId.toString()));
            listdt.add(damTomStaffEntity);
        }
        damTomEntity.setStaffs(listdt);


        DeviceProfileEntity deviceProfileEntity = new DeviceProfileEntity();
        deviceProfileEntity.setId(UUID.randomUUID());
        deviceProfileEntity.setTenantId(tenantId);
        deviceProfileEntity.setCreatedTime(System.currentTimeMillis());
        deviceProfileEntity.setName(damTomCreateDto.getName());
        deviceProfileEntity.setType(DeviceProfileType.DEFAULT);
        deviceProfileEntity.setTransportType(DeviceTransportType.DEFAULT);
        deviceProfileEntity.setDescription(damTomCreateDto.getName() + "Gateway Dam tom profile");
        deviceProfileEntity.setDefault(false);
        deviceProfileEntity.setProvisionType(DeviceProfileProvisionType.DISABLED);
        JsonNode jsonNode =
                objectMapper.readTree("{\"alarms\": null, \"configuration\": {\"type\": \"DEFAULT\"}, \"provisionConfiguration\": {\"type\": \"DISABLED\", \"provisionDeviceSecret\": null}, \"transportConfiguration\": {\"type\": \"DEFAULT\"}}");
        deviceProfileEntity.setProfileData(jsonNode);
        deviceProfileEntity.setSearchText(EntityNameConstant.DEVICE_PROFILE_GATEWAY.toLowerCase(Locale.ROOT));
        dftDeviceProfileRepository.save(deviceProfileEntity);
        damTomEntity.setDeviceProfile(deviceProfileEntity);
        DamTomEntity a = damTomRepository.save(damTomEntity);
        return a.toDto();
    }

    public DamTomDto EditDamTom(UUID tenantId, DamTomCreateDto damTomCreateDto, UUID userId) {
        DamTomEntity damTomEntity = damTomRepository.findDamTomEntityByTenantIdAndId(tenantId, UUID.fromString(damTomCreateDto.getId()));
        damTomEntity.toEntityCreate(damTomCreateDto);
        Collection<DamTomStaffEntity> listStaff = new ArrayList<>();
        for (int i = 0; i < damTomCreateDto.getStaffs().size(); i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUuid(UUID.fromString(damTomCreateDto.getStaffs().get(i)));
            DamTomStaffEntity damTomStaffEntity = new DamTomStaffEntity();
            damTomStaffEntity.setDamtomId(damTomEntity.getId());
            damTomStaffEntity.setStaff(userEntity);
            damTomStaffEntity.setCreatedTime(damTomEntity.getCreatedTime());
            damTomStaffEntity.setCreatedBy(UUID.fromString(userId.toString()));
            listStaff.add(damTomStaffEntity);
        }
        damTomEntity.setStaffs(listStaff);
        damTomEntity.getAsset().setSearchText(damTomEntity.getName().toLowerCase());
        damTomEntity.setTenantId(tenantId);
        damTomEntity.getDeviceProfile().setName(damTomCreateDto.getName());
        damTomEntity.getDeviceProfile().setDescription(damTomCreateDto.getName() + "Gateway Dam tom profile");
        dftDeviceProfileRepository.save(damTomEntity.getDeviceProfile());
        return damTomRepository.save(damTomEntity).toDto();
    }

    public DamTomEntity saveDevice(DamTomEntity damTomEntity) {
        return damTomRepository.save(damTomEntity);
    }

    public Page<DamTomGatewayEntity> getGatewayByDamTomId(String searchText, UUID damTomId, Pageable pageable) {
        return gateWayRepository.findByDamtomIdAndDevice_NameContaining(damTomId, searchText, pageable);
    }

    public DamTomGatewayEntity getDamTomGatewayEntity(UUID tenantId, UUID id) {
        return gateWayRepository.findByTenantIdAndId(tenantId, id);
    }

    public void deleteDevice(UUID tenantId, UUID id) {
        DamTomGatewayEntity damTomGatewayEntity = gateWayRepository.findByTenantIdAndId(tenantId, id);
//        UUID idDevice = damTomGatewayEntity.getDevice().getId();
        UUID idGateway = damTomGatewayEntity.getId();
        gateWayRepository.deleteById(idGateway);
//        dftDeviceProfileRepository.deleteById(idDevice);
    }

    public List<DeviceEntity> getDevices(List<UUID> list) {
        return dftDeviceRepository.findAllByIdIn(list);
    }

    public List<DamTomGatewayEntity> getListGateway(UUID tenantId, UUID damtomId) {
        return gateWayRepository.findByTenantIdAndDamtomId(tenantId, damtomId);
    }

    public List<DamTomEntity> getListDamTomByTenantId(UUID tenantId){
        return damTomRepository.findAllByTenantId(tenantId);
    }
}
