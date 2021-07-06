package org.thingsboard.server.dft.controllers.web.bothietbi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.GatewayLog;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.device.DeviceCredentialsService;
import org.thingsboard.server.dao.device.DeviceProfileService;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.RelationEntity;
import org.thingsboard.server.dao.relation.RelationService;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.damtom.dtos.DamTomReturnDto;
import org.thingsboard.server.dft.controllers.web.damtom.dtos.DeviceEditDto;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomGatewayEntity;
import org.thingsboard.server.dft.repositories.DftCustomerRepository;
import org.thingsboard.server.dft.repositories.DftRelationRepository;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.device.DftDeviceService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class BoThietBiController extends BaseController {
  @Autowired protected DamTomService damTomService;
  @Autowired private RelationService relationService;
  @Autowired private UserService userService;
  @Autowired private DeviceProfileService deviceProfileService;
  @Autowired private DeviceCredentialsService deviceCredentialsService;
  @Autowired private DftDeviceService dftDeviceService;
  @Autowired private RelationService relationServices;
  @Autowired private DftCustomerRepository customerRepository;
  @Autowired private DftRelationRepository dftRelationRepository;

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_QLTHIETBI + "\")")
  @RequestMapping(value = "/dam-tom/device", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getDeviceByTenantAndDamTom(
      @RequestParam(required = false, defaultValue = "") String damtomId,
      @RequestParam int pageSize,
      @RequestParam int page,
      @RequestParam(required = false, defaultValue = "") String textSearch,
      @RequestParam(required = false, defaultValue = "id") String sortProperty,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder)
      throws ThingsboardException {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      UUID userId = getCurrentUser().getId().getId();
      Pageable pageable =
          PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);

      Page<DamTomGatewayEntity> dtos =
          damTomService.getGatewayByDamTomId(textSearch, UUID.fromString(damtomId), pageable);

      PageData<DamTomGatewayEntity> pageData =
          new PageData<>(
              dtos.getContent(), dtos.getTotalPages(), dtos.getTotalElements(), dtos.hasNext());

      return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);

    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
          + PermissionConstants.PAGES_QLTHIETBI_DELETE
          + "\")")
  @RequestMapping(value = "/dam-tom/device/{id}", method = RequestMethod.DELETE)
  @ResponseBody
  public ResponseEntity<Integer> deleteDamTomDevice(@PathVariable("id") UUID id)
      throws ThingsboardException {
    try {
      TenantId tenantId = getCurrentUser().getTenantId();
      DamTomGatewayEntity damTomGatewayEntity =
          damTomService.getDamTomGatewayEntity(tenantId.getId(), id);
      if (damTomGatewayEntity.isActive()) {
        return ResponseEntity.ok(1);
      } else {
        EntityId entityId =
            EntityIdFactory.getByTypeAndId(
                "DEVICE", damTomGatewayEntity.getDevice().getId().toString());
        List<EntityRelation> list =
            relationService.findByFrom(
                getCurrentUser().getTenantId(), entityId, RelationTypeGroup.COMMON);
        for (EntityRelation entityRelation : list) {
          deviceService.deleteDevice(tenantId, new DeviceId(entityRelation.getTo().getId()));
        }
        damTomService.deleteDevice(tenantId.getId(), id);

        // binhdv - ghi log
        GatewayLog gatewayLog =
            new GatewayLog(
                new GatewayLogId(damTomGatewayEntity.getId()),
                getTenantId(),
                getCurrentUser().getCustomerId(),
                new DamTomLogId(damTomGatewayEntity.getDamtomId()),
                damTomGatewayEntity.getDevice().toData(),
                damTomGatewayEntity.isActive());
        logEntityAction(
            gatewayLog.getId(),
            gatewayLog,
            getCurrentUser().getCustomerId(),
            ActionType.DELETED,
            null,
            id.toString());

        return ResponseEntity.ok(0);
      }
    } catch (Exception e) {
      // binhdv - ghi log
      DamTomGatewayEntity damTomGatewayEntity =
          damTomService.getDamTomGatewayEntity(getTenantId().getId(), id);
      if (damTomGatewayEntity != null) {
        GatewayLog gatewayLog =
            new GatewayLog(
                new GatewayLogId(damTomGatewayEntity.getId()),
                getTenantId(),
                getCurrentUser().getCustomerId(),
                new DamTomLogId(damTomGatewayEntity.getDamtomId()),
                damTomGatewayEntity.getDevice().toData(),
                damTomGatewayEntity.isActive());
        logEntityAction(
            emptyId(EntityType.GATEWAY), gatewayLog, null, ActionType.DELETED, e, id.toString());
      }
      throw handleException(e);
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_QLTHIETBI + "\")")
  @RequestMapping(value = "/dam-tom/device/{id}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getDamTomGateWay(@PathVariable("id") UUID id)
      throws ThingsboardException {
    UUID tenantId = getCurrentUser().getTenantId().getId();
    DamTomGatewayEntity damTomGatewayEntity = damTomService.getDamTomGatewayEntity(tenantId, id);
    EntityId entityId =
        EntityIdFactory.getByTypeAndId(
            "DEVICE", damTomGatewayEntity.getDevice().getId().toString());
    List<EntityRelation> list =
        relationService.findByFrom(
            getCurrentUser().getTenantId(), entityId, RelationTypeGroup.COMMON);
    List<UUID> listIdDevice = new ArrayList<>();
    for (EntityRelation entityRelation : list) {
      listIdDevice.add(entityRelation.getTo().getId());
    }
    List<DeviceEntity> listDevices = damTomService.getDevices(listIdDevice);
    DeviceId deviceId = new DeviceId(damTomGatewayEntity.getDevice().getId());
    DeviceCredentials deviceCredentials =
        deviceCredentialsService.findDeviceCredentialsByDeviceId(
            getCurrentUser().getTenantId(), deviceId);

    DamTomReturnDto damTomReturnDto = new DamTomReturnDto();
    damTomReturnDto.setListDevices(listDevices);
    damTomReturnDto.setGateway(damTomGatewayEntity);
    damTomReturnDto.setCredentialsId(deviceCredentials.getCredentialsId());
    return new ResponseEntity<>(damTomReturnDto, HttpStatus.OK);
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
          + PermissionConstants.PAGES_QLTHIETBI_EDIT
          + "\")")
  @RequestMapping(value = "/dam-tom/device/edit", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> editDevice(@RequestBody DeviceEditDto deviceEditDto)
      throws ThingsboardException, IOException {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      DamTomEntity damTomEntity =
          damTomService.getDamTomById(tenantId, UUID.fromString(deviceEditDto.getDamtomId()));
      Collection<DamTomGatewayEntity> list = damTomEntity.getGateways();

      // binhdv - object de ghi log
      GatewayLog gatewayLog = null;

      for (DamTomGatewayEntity damTomGatewayEntity : list) {
        if (damTomGatewayEntity.getId().toString().equals(deviceEditDto.getId())) {
          damTomGatewayEntity.getDevice().setName(deviceEditDto.getName());
          damTomGatewayEntity.setActive(deviceEditDto.isActive());
          ObjectMapper mapper = new ObjectMapper();
          String Salt = "$<SALT001";
          String description = "Day la ghi chu";
          String additionalInfo =
              "{\"gateway\":true,\"overwriteActivityTime\":false,\"description\":\"$<SALT001\"}";
          String finalAdditionalInfo = additionalInfo.replace(Salt, deviceEditDto.getNote());
          JsonNode actualObj = mapper.readTree(finalAdditionalInfo);
          damTomGatewayEntity.getDevice().setAdditionalInfo(actualObj);
          RelationEntity relationEntity =
              dftRelationRepository.findRelationEntityByToId(
                  damTomGatewayEntity.getDevice().getId(), "ASSET", "DEVICE");
          if (damTomGatewayEntity.isActive()) {
            if (relationEntity == null) {
              EntityRelation entityRelation = new EntityRelation();
              AssetId from = new AssetId(damTomEntity.getAsset().getId());
              EntityId to =
                  new EntityId() {
                    @Override
                    public UUID getId() {
                      return damTomGatewayEntity.getDevice().getId();
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
          } else if (!damTomGatewayEntity.isActive()) {
            if (relationEntity != null) {
              dftRelationRepository.delete(relationEntity);
            }
          }

          // binhdv - tao object de ghi log
          gatewayLog =
              new GatewayLog(
                  new GatewayLogId(damTomGatewayEntity.getId()),
                  getTenantId(),
                  getCurrentUser().getCustomerId(),
                  new DamTomLogId(damTomGatewayEntity.getDamtomId()),
                  damTomGatewayEntity.getDevice().toData(),
                  damTomGatewayEntity.isActive());
        }
      }
      damTomService.saveDevice(damTomEntity);
      for (DamTomGatewayEntity damTomGatewayEntity : list) {
        if (damTomGatewayEntity.getId().toString().equals(deviceEditDto.getId())) {
          tbClusterService.onDeviceChange(damTomGatewayEntity.getDevice().toData(), null);
          tbClusterService.pushMsgToCore(
              new DeviceNameOrTypeUpdateMsg(
                  damTomGatewayEntity.getDevice().toData().getTenantId(),
                  damTomGatewayEntity.getDevice().toData().getId(),
                  damTomGatewayEntity.getDevice().toData().getName(),
                  damTomGatewayEntity.getDevice().toData().getType()),
              null);
          tbClusterService.onEntityStateChange(
              damTomGatewayEntity.getDevice().toData().getTenantId(),
              damTomGatewayEntity.getDevice().toData().getId(),
              ComponentLifecycleEvent.UPDATED);
          deviceStateService.onDeviceUpdated(damTomGatewayEntity.getDevice().toData());
        }
      }

      // binhdv - ghi log
      if (gatewayLog != null) {
        logEntityAction(
            gatewayLog.getId(),
            gatewayLog,
            getCurrentUser().getCustomerId(),
            ActionType.UPDATED,
            null);
      }

      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      // binhdv - update that bai
      Device device = new Device();
      device.setName(deviceEditDto.getName());
      GatewayLog gatewayLog =
          new GatewayLog(
              null,
              getTenantId(),
              getCurrentUser().getCustomerId(),
              new DamTomLogId(UUID.fromString(deviceEditDto.getDamtomId())),
              device,
              deviceEditDto.isActive());
      logEntityAction(emptyId(EntityType.GATEWAY), gatewayLog, null, ActionType.UPDATED, e);

      return new ResponseEntity<>(1, HttpStatus.OK);
    }
  }
}
