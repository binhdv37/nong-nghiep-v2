package org.thingsboard.server.dft.controllers.web.rpc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.GroupRpcLog;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DamTomLogId;
import org.thingsboard.server.common.data.id.GroupRpcLogId;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.common.validator.bodieukhien.GroupRpcValidator;
import org.thingsboard.server.dft.common.validator.khachhang.ValidateKhachHangService;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.DeviceSetting;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.GroupRpcDto;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.GroupRpcStatus;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;
import org.thingsboard.server.dft.services.rpc.BoDieuKhienService;
import org.thingsboard.server.dft.services.rpc.ThietBiDieuKhienService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class GroupRpcController extends BaseController {

  private final ThietBiDieuKhienService thietBiDieuKhienService;

  private final BoDieuKhienService boDieuKhienService;
  private final GroupRpcValidator groupRpcValidator;
  private final DftDeviceRepository dftDeviceRepository;

  public GroupRpcController(
          ThietBiDieuKhienService thietBiDieuKhienService,
          BoDieuKhienService boDieuKhienService,
          GroupRpcValidator groupRpcValidator, DftDeviceRepository dftDeviceRepository) {
    this.thietBiDieuKhienService = thietBiDieuKhienService;
    this.boDieuKhienService = boDieuKhienService;
    this.groupRpcValidator = groupRpcValidator;
    this.dftDeviceRepository = dftDeviceRepository;
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping(value = "/rpc-device")
  @ResponseBody
  public ResponseEntity<?> getAllThietBiDieuKhien(@RequestParam("damTomId") UUID damtomId) {
    try {
      SecurityUser securityUser = getCurrentUser();
      return new ResponseEntity<>(
          thietBiDieuKhienService.getAllThietBiDieuKhien(securityUser, damtomId), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping(value = "/group-rpc")
  @ResponseBody
  public ResponseEntity<?> getAllBoDieuKhien(@RequestParam("damTomId") UUID damtomId) {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      List<GroupRpcDto> groupRpcDtoList = boDieuKhienService.findAllByTenantId(tenantId, damtomId);
      return new ResponseEntity<>(checkNotNull(groupRpcDtoList), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping(value = "/group-rpc/{id}")
  @ResponseBody
  public ResponseEntity<?> getBoDieuKhienById(@PathVariable("id") UUID id) {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      GroupRpcDto groupRpcDto = boDieuKhienService.findByTenantIdAndId(tenantId, id);
      for (DeviceSetting deviceSetting : groupRpcDto.getRpcSettingList()) {
        DeviceEntity deviceEntity = dftDeviceRepository.findById(deviceSetting.getDeviceId()).get();
        if(deviceEntity.getLabel() == null) {
          deviceSetting.setLabel(deviceEntity.getName());
        } else {
          deviceSetting.setLabel(deviceEntity.getLabel());
        }
      }
      return new ResponseEntity<>(groupRpcDto, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @PostMapping(value = "/group-rpc")
  @ResponseBody
  public ResponseEntity<?> saveBoDieuKhien(@RequestBody GroupRpcDto groupRpcDto)
      throws ThingsboardException {
    try {
      groupRpcDto.setName(groupRpcDto.getName().trim());
      boolean existName;
      if (groupRpcDto.getGroupRpcId() != null) {
        existName =
            groupRpcValidator.validateGroupRpc(
                groupRpcDto.getDamTomId(),
                ValidateKhachHangService.TYPE_EDIT,
                groupRpcDto.getName(),
                groupRpcDto.getGroupRpcId());
      } else {
        existName =
            groupRpcValidator.validateGroupRpc(
                groupRpcDto.getDamTomId(),
                ValidateKhachHangService.TYPE_ADD,
                groupRpcDto.getName(),
                null);
      }
      if (existName) {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
            .body("Tên bộ điều khiển đã tồn tại");
      }
      SecurityUser securityUser = getCurrentUser();
      GroupRpcLog groupRpcLog;

      GroupRpcDto groupRpcDtoSaved = boDieuKhienService.saveGroupRpc(securityUser, groupRpcDto);

      // Bo xung cho ghi log
      groupRpcLog =
          new GroupRpcLog(
              new GroupRpcLogId(groupRpcDtoSaved.getGroupRpcId()),
              getCurrentUser().getTenantId(),
              getCurrentUser().getCustomerId(),
              new DamTomLogId(groupRpcDto.getDamTomId()),
              groupRpcDtoSaved.getName(),
              null);

      if (groupRpcDto.getGroupRpcId() == null) {
        // Ghi log them moi o day
        logEntityAction(
            groupRpcLog.getId(),
            groupRpcLog,
            getCurrentUser().getCustomerId(),
            ActionType.ADDED,
            null);
      } else {
        // Ghi log cap nhat o day
        logEntityAction(
            groupRpcLog.getId(),
            groupRpcLog,
            getCurrentUser().getCustomerId(),
            ActionType.UPDATED,
            null);
      }
      return new ResponseEntity<>(groupRpcDtoSaved, HttpStatus.CREATED);
    } catch (Exception e) {
      // ghi log khi that bai
      GroupRpcLog groupRpcLog =
          new GroupRpcLog(
              null,
              getTenantId(),
              getCurrentUser().getCustomerId(),
              new DamTomLogId(groupRpcDto.getDamTomId()),
              groupRpcDto.getName(),
              null);
      logEntityAction(
          emptyId(EntityType.GROUP_RPC),
          groupRpcLog,
          null,
          groupRpcDto.getGroupRpcId() == null ? ActionType.ADDED : ActionType.UPDATED,
          e);

      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @DeleteMapping(value = "/group-rpc/{id}")
  @ResponseBody
  public ResponseEntity<?> delete(@PathVariable("id") UUID id) throws ThingsboardException {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      // Phuc vu ghi log
      GroupRpcDto groupRpcDto = boDieuKhienService.findByTenantIdAndId(tenantId, id);

      boDieuKhienService.delete(tenantId, id);

      // ghi log sau khi xoa thanh cong
      if (groupRpcDto != null) {
        GroupRpcLog groupRpcLog =
            new GroupRpcLog(
                new GroupRpcLogId(id),
                getCurrentUser().getTenantId(),
                getCurrentUser().getCustomerId(),
                new DamTomLogId(groupRpcDto.getDamTomId()),
                groupRpcDto.getName(),
                null);
        logEntityAction(
            groupRpcLog.getId(),
            groupRpcLog,
            getCurrentUser().getCustomerId(),
            ActionType.DELETED,
            null,
            id.toString());
      }

      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      // Ghi log khi xoa that bai
      GroupRpcDto groupRpcDto =
          boDieuKhienService.findByTenantIdAndId(getCurrentUser().getTenantId().getId(), id);

      if (groupRpcDto != null) {
        GroupRpcLog groupRpcLog =
            new GroupRpcLog(
                new GroupRpcLogId(id),
                getCurrentUser().getTenantId(),
                getCurrentUser().getCustomerId(),
                new DamTomLogId(groupRpcDto.getDamTomId()),
                groupRpcDto.getName(),
                null);
        logEntityAction(
            emptyId(EntityType.GROUP_RPC), groupRpcLog, null, ActionType.DELETED, e, id.toString());
      }

      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping(value = "/group-rpc/validate")
  public ResponseEntity<?> ValidateGroupRpcName(
      @RequestParam(name = "damTomId") UUID damTomId,
      @RequestParam(name = "type") String type,
      @RequestParam(name = "groupRpcName") String groupRpcName,
      @RequestParam(name = "id", required = false) UUID id) {
    try {
      boolean check;
      if (type.equals(ValidateKhachHangService.TYPE_EDIT)) {
        if (id == null) {
          return new ResponseEntity<>(false, HttpStatus.OK);
        } else {
          check = groupRpcValidator.validateGroupRpc(damTomId, type, groupRpcName, id);
          return new ResponseEntity<>(check, HttpStatus.OK);
        }
      } else {
        id = UUID.randomUUID();
        check = groupRpcValidator.validateGroupRpc(damTomId, type, groupRpcName, id);
        return new ResponseEntity<>(check, HttpStatus.OK);
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @PostMapping(value = "/group-rpc/start/{groupRpcId}")
  @ResponseBody
  public ResponseEntity<?> startGroupRpc(@PathVariable("groupRpcId") UUID groupRpcId) {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      boDieuKhienService.startGroupRpc(tenantId, groupRpcId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @PostMapping(value = "/group-rpc/stop/{groupRpcId}")
  @ResponseBody
  public ResponseEntity<?> stopGroupRpc(@PathVariable("groupRpcId") UUID groupRpcId) {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      boDieuKhienService.stopGroupRpc(tenantId, groupRpcId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping(value = "/group-rpc/status/{groupRpcId}")
  @ResponseBody
  public ResponseEntity<?> getStatusGroupRpc(@PathVariable("groupRpcId") UUID groupRpcId) {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      GroupRpcStatus groupRpcStatus =
          boDieuKhienService.checkStatusGroupRpcById(tenantId, groupRpcId);
      return new ResponseEntity<>(groupRpcStatus, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }
}
