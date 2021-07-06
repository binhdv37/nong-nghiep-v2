package org.thingsboard.server.dft.controllers.web.damtom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.rule.engine.api.msg.DeviceNameOrTypeUpdateMsg;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.data.security.DeviceCredentialsType;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.device.DeviceCredentialsService;
import org.thingsboard.server.dao.device.DeviceProfileService;
import org.thingsboard.server.dao.model.sql.CustomerEntity;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dao.relation.RelationService;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.dft.common.constants.EntityNameConstant;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.damtom.dtos.DamTomCreateDto;
import org.thingsboard.server.dft.controllers.web.damtom.dtos.DamTomDto;
import org.thingsboard.server.dft.controllers.web.damtom.dtos.DeviceCreateDto;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomGatewayEntity;
import org.thingsboard.server.dft.entities.DamTomStaffEntity;
import org.thingsboard.server.dft.repositories.DftCustomerRepository;
import org.thingsboard.server.dft.repositories.DftDeviceProfileRepository;
import org.thingsboard.server.dft.repositories.DftRelationRepository;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.device.DftDeviceService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.io.IOException;
import java.util.*;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class DamTomController extends BaseController {

  @Autowired protected DamTomService damTomService;
  @Autowired private RelationService relationService;
  @Autowired private UserService userService;
  @Autowired protected DeviceProfileService deviceProfileService;
  @Autowired protected DeviceCredentialsService deviceCredentialsService;
  @Autowired protected DftDeviceService dftDeviceService;
  @Autowired protected RelationService relationServices;
  @Autowired private DftCustomerRepository customerRepository;
  @Autowired private DftRelationRepository dftRelationRepository;
  @Autowired private  ObjectMapper objectMapper;
  @Autowired private DftDeviceProfileRepository dftDeviceProfileRepository;


  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DAMTOM + "\")")
  @RequestMapping(value = "/dam-tom", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getTenantDamTom(
      @RequestParam int pageSize,
      @RequestParam int page,
      @RequestParam(required = false, defaultValue = "") String textSearch,
      @RequestParam(required = false, defaultValue = "id") String sortProperty,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder)
      throws ThingsboardException {
    try {
      textSearch = textSearch.trim();
      UUID tenantId = getCurrentUser().getTenantId().getId();
      UUID userId = getCurrentUser().getId().getId();
      User currentUser =
          userService.findUserById(getCurrentUser().getTenantId(), getCurrentUser().getId());
      if (currentUser.getAuthority().name().equals("TENANT_ADMIN")) {
        Pageable pageable =
            PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);

        Page<DamTomDto> dtos =
            damTomService
                .getDamTomByTenantIdAndSearchText(tenantId, textSearch, pageable)
                .map(DamTomDto::new);

        PageData<DamTomDto> pageData =
            new PageData<>(
                dtos.getContent(), dtos.getTotalPages(), dtos.getTotalElements(), dtos.hasNext());

        return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
      } else {
        Pageable pageable =
            PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);

        List<DamTomStaffEntity> list = damTomService.getListOfDamTomStaff(userId);
        List<UUID> listIds = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
          listIds.add(list.get(i).getDamtomId());
        }
        Page<DamTomDto> dtos =
            damTomService
                .getDamTomByListID(textSearch, listIds, userId, pageable)
                .map(DamTomDto::new);

        PageData<DamTomDto> pageData =
            new PageData<>(
                dtos.getContent(), dtos.getTotalPages(), dtos.getTotalElements(), dtos.hasNext());

        return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
      }
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
          + PermissionConstants.PAGES_DAMTOM_CREATE
          + "\")")
  @RequestMapping(value = "/dam-tom", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> saveDamTom(@RequestBody DamTomCreateDto damTomCreateDto)
      throws ThingsboardException, JsonProcessingException {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      UUID userId = getCurrentUser().getId().getId();
      damTomCreateDto.setCreatedBy(userId.toString());
      damTomCreateDto.setName(damTomCreateDto.getName().trim());
      damTomCreateDto.setAddress(damTomCreateDto.getAddress().trim());
      damTomCreateDto.setNote(damTomCreateDto.getNote().trim());
      List<DamTomEntity> list =
          damTomService.findAllByTenantIdAndName(tenantId, damTomCreateDto.getName());
      if (list.size() == 0 || list == null) {
        DamTomDto damTomDto = damTomService.save(tenantId, damTomCreateDto, userId);
        damTomDto.setMessage(0);

        // binhdv - log
        DamTomLog damTomLog =
            new DamTomLog(
                new DamTomLogId(damTomDto.getId().getId()),
                getTenantId(),
                getCurrentUser().getCustomerId(),
                damTomDto.getName(),
                damTomDto.getAddress(),
                damTomDto.getNote(),
                damTomDto.getImages());
        // create success
        logEntityAction(
            damTomLog.getId(), damTomLog, getCurrentUser().getCustomerId(), ActionType.ADDED, null);

        return new ResponseEntity<>(damTomDto, HttpStatus.OK);
      } else {
        if (list.size() == 1 && list.get(0).getId().toString().equals(damTomCreateDto.getId())) {
          DamTomDto damTomDto = damTomService.save(tenantId, damTomCreateDto, userId);
          damTomDto.setMessage(1);

          // binhdv - log
          DamTomLog damTomLog =
              new DamTomLog(
                  new DamTomLogId(damTomDto.getId().getId()),
                  getTenantId(),
                  getCurrentUser().getCustomerId(),
                  damTomDto.getName(),
                  damTomDto.getAddress(),
                  damTomDto.getNote(),
                  damTomDto.getImages());
          // update success
          logEntityAction(
              damTomLog.getId(),
              damTomLog,
              getCurrentUser().getCustomerId(),
              ActionType.UPDATED,
              null);

          return new ResponseEntity<>(damTomDto, HttpStatus.OK);
        }
      }
      return ResponseEntity.ok(2);
    } catch (Exception e) {

      // binhdv - log
      DamTomLog damTomLog =
          new DamTomLog(
              null,
              getTenantId(),
              getCurrentUser().getCustomerId(),
              damTomCreateDto.getName(),
              damTomCreateDto.getAddress(),
              damTomCreateDto.getNote(),
              damTomCreateDto.getImages());
      // create or update failure
      logEntityAction(
          emptyId(EntityType.DAM_TOM),
          damTomLog,
          null,
          damTomCreateDto.getId() == null ? ActionType.ADDED : ActionType.UPDATED,
          e);

      throw handleException(e);
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
          + PermissionConstants.PAGES_DAMTOM_DELETE
          + "\")")
  @RequestMapping(value = "/dam-tom/{id}", method = RequestMethod.DELETE)
  @ResponseBody
  public ResponseEntity<Integer> deleteDamTom(@PathVariable("id") UUID id)
      throws ThingsboardException {
    try {
      TenantId tenantId = getCurrentUser().getTenantId();
      DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId.getId(), id);
      UUID assetId = damTomEntity.getAsset().getId();
      EntityId entityId = EntityIdFactory.getByTypeAndId("ASSET", assetId.toString());
      List<DamTomGatewayEntity> list = damTomService.getListGateway(tenantId.getId(), id);
      if (list.size() == 0) {
        damTomService.deleteDamtom(tenantId.getId(), id);

        // binhdv - log
        DamTomLog damTomLog =
            new DamTomLog(
                new DamTomLogId(damTomEntity.getId()),
                getTenantId(),
                getCurrentUser().getCustomerId(),
                damTomEntity.getName(),
                damTomEntity.getAddress(),
                damTomEntity.getNote(),
                damTomEntity.getImages());
        // delete thanh cong
        logEntityAction(
            damTomLog.getId(),
            damTomLog,
            getCurrentUser().getCustomerId(),
            ActionType.DELETED,
            null,
            id.toString());

        return ResponseEntity.ok(0);
      } else {
        // k thanh cong - vi con gateway
        return ResponseEntity.ok(1);
      }
    } catch (Exception e) {
      // binhdv - log
      // find object de ghi log:
      DamTomEntity damTomEntity = damTomService.getDamTomById(getTenantId().getId(), id);

      if (damTomEntity != null) {
        // tao object de ghi log
        DamTomLog damTomLog =
            new DamTomLog(
                new DamTomLogId(damTomEntity.getId()),
                getTenantId(),
                getCurrentUser().getCustomerId(),
                damTomEntity.getName(),
                damTomEntity.getAddress(),
                damTomEntity.getNote(),
                damTomEntity.getImages());
        // ghi log
        logEntityAction(
            emptyId(EntityType.DAM_TOM), damTomLog, null, ActionType.DELETED, e, id.toString());
      }

      throw handleException(e);
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DAMTOM + "\")")
  @RequestMapping(value = "/dam-tom/{id}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getDamTomById(@PathVariable("id") UUID id) throws ThingsboardException {
    UUID tenantId = getCurrentUser().getTenantId().getId();
    return new ResponseEntity<>(damTomService.getDamTomById(tenantId, id), HttpStatus.OK);
  }

//  @PreAuthorize(
//      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
//          + PermissionConstants.PAGES_QLTHIETBI_CREATE
//          + "\")")
//  @RequestMapping(value = "/dam-tom/device", method = RequestMethod.POST)
//  @ResponseBody
//  public ResponseEntity<?> createDevice(@RequestBody DeviceCreateDto deviceCreateDto)
//      throws ThingsboardException, IOException {
//    UUID tenantId = getCurrentUser().getTenantId().getId();
//    DamTomEntity damTomEntity =
//        damTomService.getDamTomById(tenantId, UUID.fromString(deviceCreateDto.getDamtomId()));
//    Collection<DamTomGatewayEntity> gateways = damTomEntity.getGateways();
//
//    DamTomGatewayEntity damTomGatewayEntity = new DamTomGatewayEntity();
//    damTomGatewayEntity.setId(UUID.randomUUID());
//    damTomGatewayEntity.setTenantId(tenantId);
//    damTomGatewayEntity.setDamtomId(UUID.fromString(deviceCreateDto.getDamtomId()));
//    damTomGatewayEntity.setCreatedBy(getCurrentUser().getId().getId());
//    damTomGatewayEntity.setCreatedTime(System.currentTimeMillis());
//    damTomGatewayEntity.setActive(deviceCreateDto.isActive());
//
//    DeviceEntity deviceEntity = new DeviceEntity();
//    deviceEntity.setId(UUID.randomUUID());
//    deviceEntity.setTenantId(tenantId);
//    deviceEntity.setCreatedTime(System.currentTimeMillis());
//    CustomerEntity customerEntity = customerRepository.findDistinctFirstByTenantId(tenantId);
//    deviceEntity.setCustomerId(customerEntity.getId());
//    ObjectMapper mapper = new ObjectMapper();
//
//    String Salt = "$<SALT001";
//    String description = "Day la ghi chu";
//    String additionalInfo =
//        "{\"gateway\":true,\"overwriteActivityTime\":false,\"description\":\"$<SALT001\"}";
//    String finalAdditionalInfo = additionalInfo.replace(Salt, deviceCreateDto.getNote());
//    JsonNode actualObj = mapper.readTree(finalAdditionalInfo);
//    deviceEntity.setAdditionalInfo(actualObj);
//
//    JsonNode actualObj2 =
//        mapper.readTree(
//            "{\"configuration\": {\"type\": \"DEFAULT\"}, \"transportConfiguration\": {\"type\": \"DEFAULT\"}}");
//    deviceEntity.setDeviceData(actualObj2);
//
//    DeviceProfileEntity deviceProfileEntity =
//            dftDeviceService.getDeviceProfileById(damTomEntity.getDeviceProfile().getId());
//    deviceEntity.setDeviceProfileId(deviceProfileEntity.getId());
//    deviceEntity.setType("default");
//    deviceEntity.setName(deviceCreateDto.getName());
//    deviceEntity.setSearchText(deviceCreateDto.getName().toLowerCase(Locale.ROOT));
//
//
//    damTomGatewayEntity.setDevice(deviceEntity);
//    gateways.add(damTomGatewayEntity);
//    damTomEntity.setGateways(gateways);
//
//    DeviceCredentials deviceCredentials = new DeviceCredentials();
//    deviceCredentials.setCredentialsId(RandomStringUtils.randomAlphanumeric(20));
//    deviceCredentials.setCredentialsType(DeviceCredentialsType.ACCESS_TOKEN);
//    deviceCredentials.setDeviceId(new DeviceId(deviceEntity.getId()));
//    try {
//      DamTomEntity damTomEntity1 = damTomService.saveDevice(damTomEntity);
//      if (damTomEntity1 != null) {
//        tbClusterService.onDeviceChange(deviceEntity.toData(), null);
//        tbClusterService.pushMsgToCore(
//            new DeviceNameOrTypeUpdateMsg(
//                deviceEntity.toData().getTenantId(),
//                deviceEntity.toData().getId(),
//                deviceEntity.toData().getName(),
//                deviceEntity.toData().getType()),
//            null);
//        tbClusterService.onEntityStateChange(
//            deviceEntity.toData().getTenantId(),
//            deviceEntity.toData().getId(),
//            ComponentLifecycleEvent.CREATED);
//
//        deviceStateService.onDeviceAdded(deviceEntity.toData());
//
//        DeviceCredentials deviceCredentials1 =
//            deviceCredentialsService.createDeviceCredentials(
//                getCurrentUser().getTenantId(), deviceCredentials);
//        if (deviceCredentials1 != null) {
//          if (damTomGatewayEntity.isActive()) {
//            EntityRelation entityRelation = new EntityRelation();
//            EntityId from =
//                new EntityId() {
//                  @Override
//                  public UUID getId() {
//                    return damTomEntity1.getAsset().getId();
//                  }
//
//                  @Override
//                  public EntityType getEntityType() {
//                    return EntityType.ASSET;
//                  }
//                };
//            EntityId to =
//                new EntityId() {
//                  @Override
//                  public UUID getId() {
//                    return deviceEntity.getId();
//                  }
//
//                  @Override
//                  public EntityType getEntityType() {
//                    return EntityType.DEVICE;
//                  }
//                };
//            entityRelation.setFrom(from);
//            entityRelation.setTo(to);
//            entityRelation.setType("Contains");
//            entityRelation.setTypeGroup(RelationTypeGroup.COMMON);
//            relationServices.saveRelation(getTenantId(), entityRelation);
//          }
//        }
//
//        // binhdv - ghi log
//        GatewayLog gatewayLog =
//            new GatewayLog(
//                new GatewayLogId(damTomGatewayEntity.getId()),
//                getTenantId(),
//                getCurrentUser().getCustomerId(),
//                new DamTomLogId(damTomGatewayEntity.getDamtomId()),
//                deviceEntity.toData(),
//                damTomGatewayEntity.isActive());
//        logEntityAction(
//            gatewayLog.getId(),
//            gatewayLog,
//            getCurrentUser().getCustomerId(),
//            ActionType.ADDED,
//            null);
//      }
//      return new ResponseEntity<>(damTomEntity1.getGateways(), HttpStatus.OK);
//    } catch (Exception e) {
//      // binhdv - ghi log khi that bai
//      Device device = new Device();
//      device.setName(deviceCreateDto.getName());
//      GatewayLog gatewayLog =
//          new GatewayLog(
//              null,
//              getTenantId(),
//              getCurrentUser().getCustomerId(),
//              new DamTomLogId(UUID.fromString(deviceCreateDto.getDamtomId())),
//              device,
//              deviceCreateDto.isActive());
//      logEntityAction(emptyId(EntityType.GATEWAY), gatewayLog, null, ActionType.ADDED, e);
//
//      return new ResponseEntity<>(1, HttpStatus.OK);
//    }
//  }

  /**
   *  revert
   * @param deviceCreateDto
   * @return
   * @throws ThingsboardException
   * @throws IOException
   */
  @PreAuthorize(
          "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                  + PermissionConstants.PAGES_QLTHIETBI_CREATE
                  + "\")")
  @RequestMapping(value = "/dam-tom/device", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> createDevice(@RequestBody DeviceCreateDto deviceCreateDto)
          throws ThingsboardException, IOException {
    UUID tenantId = getCurrentUser().getTenantId().getId();
    DamTomEntity damTomEntity =
            damTomService.getDamTomById(tenantId, UUID.fromString(deviceCreateDto.getDamtomId()));
    Collection<DamTomGatewayEntity> gateways = damTomEntity.getGateways();

    DamTomGatewayEntity damTomGatewayEntity = new DamTomGatewayEntity();
    damTomGatewayEntity.setId(UUID.randomUUID());
    damTomGatewayEntity.setTenantId(tenantId);
    damTomGatewayEntity.setDamtomId(UUID.fromString(deviceCreateDto.getDamtomId()));
    damTomGatewayEntity.setCreatedBy(getCurrentUser().getId().getId());
    damTomGatewayEntity.setCreatedTime(System.currentTimeMillis());
    damTomGatewayEntity.setActive(deviceCreateDto.isActive());

    DeviceEntity deviceEntity = new DeviceEntity();
    deviceEntity.setId(UUID.randomUUID());
    deviceEntity.setTenantId(tenantId);
    deviceEntity.setCreatedTime(System.currentTimeMillis());
    CustomerEntity customerEntity = customerRepository.findDistinctFirstByTenantId(tenantId);
    deviceEntity.setCustomerId(customerEntity.getId());
    ObjectMapper mapper = new ObjectMapper();

    String Salt = "$<SALT001";
    String description = "Day la ghi chu";
    String additionalInfo =
            "{\"gateway\":true,\"overwriteActivityTime\":false,\"description\":\"$<SALT001\"}";
    String finalAdditionalInfo = additionalInfo.replace(Salt, deviceCreateDto.getNote());
    JsonNode actualObj = mapper.readTree(finalAdditionalInfo);
    deviceEntity.setAdditionalInfo(actualObj);

    JsonNode actualObj2 =
            mapper.readTree(
                    "{\"configuration\": {\"type\": \"DEFAULT\"}, \"transportConfiguration\": {\"type\": \"DEFAULT\"}}");
    deviceEntity.setDeviceData(actualObj2);

    DeviceProfileEntity deviceProfileEntity =
            dftDeviceService.getDeviceProfileById(damTomEntity.getDeviceProfile().getId());
    deviceEntity.setDeviceProfileId(deviceProfileEntity.getId());
    deviceEntity.setType("default");
    deviceEntity.setName(deviceCreateDto.getName());
    deviceEntity.setSearchText(deviceCreateDto.getName().toLowerCase(Locale.ROOT));

    damTomGatewayEntity.setDevice(deviceEntity);
    gateways.add(damTomGatewayEntity);
    damTomEntity.setGateways(gateways);

    DeviceCredentials deviceCredentials = new DeviceCredentials();
    deviceCredentials.setCredentialsId(RandomStringUtils.randomAlphanumeric(20));
    deviceCredentials.setCredentialsType(DeviceCredentialsType.ACCESS_TOKEN);
    deviceCredentials.setDeviceId(new DeviceId(deviceEntity.getId()));
    try {
      DamTomEntity damTomEntity1 = damTomService.saveDevice(damTomEntity);
      if (damTomEntity1 != null) {
        tbClusterService.onDeviceChange(deviceEntity.toData(), null);
        tbClusterService.pushMsgToCore(
                new DeviceNameOrTypeUpdateMsg(
                        deviceEntity.toData().getTenantId(),
                        deviceEntity.toData().getId(),
                        deviceEntity.toData().getName(),
                        deviceEntity.toData().getType()),
                null);
        tbClusterService.onEntityStateChange(
                deviceEntity.toData().getTenantId(),
                deviceEntity.toData().getId(),
                ComponentLifecycleEvent.CREATED);

        deviceStateService.onDeviceAdded(deviceEntity.toData());

        DeviceCredentials deviceCredentials1 =
                deviceCredentialsService.createDeviceCredentials(
                        getCurrentUser().getTenantId(), deviceCredentials);
        if (deviceCredentials1 != null) {
          if (damTomGatewayEntity.isActive()) {
            EntityRelation entityRelation = new EntityRelation();
            EntityId from =
                    new EntityId() {
                      @Override
                      public UUID getId() {
                        return damTomEntity1.getAsset().getId();
                      }

                      @Override
                      public EntityType getEntityType() {
                        return EntityType.ASSET;
                      }
                    };
            EntityId to =
                    new EntityId() {
                      @Override
                      public UUID getId() {
                        return deviceEntity.getId();
                      }

                      @Override
                      public EntityType getEntityType() {
                        return EntityType.DEVICE;
                      }
                    };
            entityRelation.setFrom(from);
            entityRelation.setTo(to);
            entityRelation.setType("Contains");
            entityRelation.setTypeGroup(RelationTypeGroup.COMMON);
            relationServices.saveRelation(getTenantId(), entityRelation);
          }
        }

        // binhdv - ghi log
        GatewayLog gatewayLog =
                new GatewayLog(
                        new GatewayLogId(damTomGatewayEntity.getId()),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        new DamTomLogId(damTomGatewayEntity.getDamtomId()),
                        deviceEntity.toData(),
                        damTomGatewayEntity.isActive());
        logEntityAction(
                gatewayLog.getId(),
                gatewayLog,
                getCurrentUser().getCustomerId(),
                ActionType.ADDED,
                null);
      }
      return new ResponseEntity<>(damTomEntity1.getGateways(), HttpStatus.OK);
    } catch (Exception e) {
      // binhdv - ghi log khi that bai
      Device device = new Device();
      device.setName(deviceCreateDto.getName());
      GatewayLog gatewayLog =
              new GatewayLog(
                      null,
                      getTenantId(),
                      getCurrentUser().getCustomerId(),
                      new DamTomLogId(UUID.fromString(deviceCreateDto.getDamtomId())),
                      device,
                      deviceCreateDto.isActive());
      logEntityAction(emptyId(EntityType.GATEWAY), gatewayLog, null, ActionType.ADDED, e);

      return new ResponseEntity<>(1, HttpStatus.OK);
    }
  }



  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DAMTOM + "\")")
  @RequestMapping(value = "/dam-tom/mobi", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getTenantDamTomMobi(
      @RequestParam(required = false, defaultValue = "") String textSearch)
      throws ThingsboardException {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      UUID userId = getCurrentUser().getId().getId();
      User currentUser =
          userService.findUserById(getCurrentUser().getTenantId(), getCurrentUser().getId());
      if (currentUser.getAuthority().name().equals("TENANT_ADMIN")) {
        List<DamTomEntity> list =
            damTomService.getDamTomByTenantIdAndSearchTextMobi(tenantId, textSearch);
        return new ResponseEntity<>(checkNotNull(list), HttpStatus.OK);
      } else {
        List<DamTomStaffEntity> list = damTomService.getListOfDamTomStaff(userId);
        List<UUID> listIds = new ArrayList<>();
        for (DamTomStaffEntity damTomStaffEntity : list) {
          listIds.add(damTomStaffEntity.getDamtomId());
        }
        List<DamTomEntity> damTomEntities = damTomService.getDamTomByListIDMobi(listIds, userId);
        return new ResponseEntity<>(checkNotNull(damTomEntities), HttpStatus.OK);
      }
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DAMTOM + "\")")
  @RequestMapping(value = "/dam-tom/active/mobi", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getTenantDamTomActiveMobi(
      @RequestParam(required = false, defaultValue = "") String textSearch)
      throws ThingsboardException {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      UUID userId = getCurrentUser().getId().getId();
      User currentUser =
          userService.findUserById(getCurrentUser().getTenantId(), getCurrentUser().getId());
      if (currentUser.getAuthority().name().equals("TENANT_ADMIN")) {
        List<DamTomEntity> list =
            damTomService.getDamTomActiveByTenantIdAndSearchTextMobi(tenantId, textSearch);
        return new ResponseEntity<>(checkNotNull(list), HttpStatus.OK);
      } else {
        List<DamTomStaffEntity> list = damTomService.getListOfDamTomStaff(userId);
        List<UUID> listIds = new ArrayList<>();
        for (DamTomStaffEntity damTomStaffEntity : list) {
          listIds.add(damTomStaffEntity.getDamtomId());
        }
        List<DamTomEntity> damTomEntities =
            damTomService.getDamTomActiveByListIDMobi(listIds, userId);
        return new ResponseEntity<>(checkNotNull(damTomEntities), HttpStatus.OK);
      }
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  // binhdv - get all active damtom of tenant for bao cao
  // cứ login là call dc api này -> user chỉ có quyền xem báo cáo cũng call đc.
  @GetMapping("/dam-tom/get-all/active")
  public ResponseEntity<List<DamTomEntity>> getALlActiveDamtomForBaoCao()
      throws ThingsboardException {
    try {
      List<DamTomEntity> damTomEntities =
          damTomService.getDamTomActiveByTenantIdAndSearchTextMobi(getTenantId().getId(), "");
      return ResponseEntity.ok(damTomEntities);
    } catch (Exception e) {
      throw handleException(e);
    }
  }
}
