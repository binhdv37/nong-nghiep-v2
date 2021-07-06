package org.thingsboard.server.dft.controllers.web.lscanhbao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.alarm.AlarmStatus;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.AlarmId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.lscanhbao.dtos.DamTomSnapshot;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomSnapshotEntity;
import org.thingsboard.server.dft.entities.DamTomStaffEntity;
import org.thingsboard.server.dft.repositories.DamTomSnapshotRepository;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.lscanhbao.LichSuCanhBaoService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.security.permission.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.thingsboard.server.dao.service.Validator.validateId;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class LichSuCanhBaoController extends BaseController {

  private final LichSuCanhBaoService lichSuCanhBaoService;
  private final DamTomSnapshotRepository damTomSnapshotRepository;
  private final DamTomService damTomService;

  @Autowired
  public LichSuCanhBaoController(
      LichSuCanhBaoService lichSuCanhBaoService,
      DamTomSnapshotRepository damTomSnapshotRepository,
      DamTomService damTomService) {
    this.lichSuCanhBaoService = lichSuCanhBaoService;
    this.damTomSnapshotRepository = damTomSnapshotRepository;
    this.damTomService = damTomService;
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_GIAMSAT + "\")")
  @RequestMapping(value = "/alarm-history", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getAllDamTomAlarm(
      @RequestParam(name = "damtomId", required = false) UUID damtomId,
      @RequestParam(name = "pageSize") int pageSize,
      @RequestParam(name = "page") int page,
      @RequestParam(name = "sortProperty", required = false, defaultValue = "timeSnapshot")
          String sortProperty,
      @RequestParam(name = "sortOrder", required = false, defaultValue = "desc") String sortOrder,
      @RequestParam(name = "startTime", required = false) Long startTime,
      @RequestParam(name = "endTime", required = false) Long endTime,
      @RequestParam(name = "textSearch", required = false, defaultValue = "") String textSearch) {
    try {
      TenantId tenantId = getCurrentUser().getTenantId();
      List<Sort.Order> orders = new ArrayList<>();

      Sort.Order clearOrder = new Sort.Order(Sort.Direction.ASC, "clear");
      orders.add(clearOrder);
      Sort.Order timeOrder = new Sort.Order(Sort.Direction.fromString(sortOrder), sortProperty);
      orders.add(timeOrder);

      Pageable pageable = PageRequest.of(page, pageSize, Sort.by(orders));
      PageData<DamTomSnapshot> pageData =
          lichSuCanhBaoService.getAllDamTomSnap(
              tenantId.getId(), damtomId, pageable, startTime, endTime, textSearch);
      return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
          "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_GIAMSAT + "\")")
  @RequestMapping(value = "/mobile/alarm-history", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getAllDamTomAlarmMobile(
          @RequestParam(name = "damtomId", required = false) UUID damtomId,
          @RequestParam(name = "pageSize") int pageSize,
          @RequestParam(name = "page") int page,
          @RequestParam(name = "sortProperty", required = false, defaultValue = "timeSnapshot")
                  String sortProperty,
          @RequestParam(name = "sortOrder", required = false, defaultValue = "desc") String sortOrder,
          @RequestParam(name = "startTime", required = false) Long startTime,
          @RequestParam(name = "endTime", required = false) Long endTime,
          @RequestParam(name = "textSearch", required = false, defaultValue = "") String textSearch) {
    try {
      TenantId tenantId = getCurrentUser().getTenantId();
      List<Sort.Order> orders = new ArrayList<>();

      Sort.Order clearOrder = new Sort.Order(Sort.Direction.ASC, "clear");
      orders.add(clearOrder);
      Sort.Order timeOrder = new Sort.Order(Sort.Direction.fromString(sortOrder), sortProperty);
      orders.add(timeOrder);

      Pageable pageable = PageRequest.of(page, pageSize, Sort.by(orders));
      PageData<DamTomSnapshot> pageData =
              lichSuCanhBaoService.getAllDamTomSnapForMobile(
                      tenantId.getId(), damtomId, pageable, startTime, endTime, textSearch);
      return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_GIAMSAT + "\")")
  @RequestMapping(value = "/alarm-history/{snapShotId}", method = RequestMethod.PUT)
  @ResponseBody
  public ResponseEntity<?> clearAlarm(@PathVariable("snapShotId") UUID snapShotId) {
    try {
      DamTomSnapshotEntity damTomSnapshotEntity =
          damTomSnapshotRepository.findById(snapShotId).get();
      AlarmId alarmId = new AlarmId(damTomSnapshotEntity.getAlarmId());
      Alarm alarm = checkAlarmId(alarmId, Operation.WRITE);
      if(alarm != null) {
        long clearTs = System.currentTimeMillis();
        alarmService.clearAlarm(getCurrentUser().getTenantId(), alarmId, null, clearTs).get();
        alarm.setClearTs(clearTs);
        alarm.setStatus(
                alarm.getStatus().isAck() ? AlarmStatus.CLEARED_ACK : AlarmStatus.CLEARED_UNACK);
        logEntityAction(
                alarm.getId(), alarm, getCurrentUser().getCustomerId(), ActionType.ALARM_CLEAR, null);
      }
      lichSuCanhBaoService.clearAlarm(snapShotId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_GIAMSAT + "\")")
  @RequestMapping(value = "/lastest-alarm", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getLastestAlarmSnapshot() {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      UUID userId = getCurrentUser().getId().getId();
      User currentUser =
          userService.findUserById(getCurrentUser().getTenantId(), getCurrentUser().getId());
      if (currentUser.getAuthority().name().equals("TENANT_ADMIN")) {
        DamTomSnapshot damTomSnapshot =
            lichSuCanhBaoService.getLastestAlarmSnapshot(null, tenantId);
        return new ResponseEntity<>(checkNotNull(damTomSnapshot), HttpStatus.OK);
      } else {
        List<DamTomStaffEntity> list = damTomService.getListOfDamTomStaff(userId);
        List<UUID> listIds = new ArrayList<>();
        for (DamTomStaffEntity damTomStaffEntity : list) {
          listIds.add(damTomStaffEntity.getDamtomId());
        }
        List<DamTomEntity> damTomEntities =
            damTomService.getDamTomActiveByListIDMobi(listIds, userId);
        List<UUID> damTomIds = new ArrayList<>();
        for (DamTomEntity damTomEntity : damTomEntities) {
          damTomIds.add(damTomEntity.getId());
        }
        DamTomSnapshot damTomSnapshot =
            lichSuCanhBaoService.getLastestAlarmSnapshot(damTomIds, tenantId);
        return new ResponseEntity<>(checkNotNull(damTomSnapshot), HttpStatus.OK);
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_GIAMSAT + "\")")
  @RequestMapping(value = "/active-alarm/{damTomId}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getAllActiveAlarm(@PathVariable("damTomId") UUID damTomId) {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      List<DamTomSnapshot> damTomSnapshotList =
          lichSuCanhBaoService.getAllActiveAlarm(damTomId, tenantId);
      return new ResponseEntity<>(checkNotNull(damTomSnapshotList), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_GIAMSAT + "\")")
  @RequestMapping(value = "/active-alarm/count", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> countAlarmByDamTom(@RequestParam(value = "damTomId", required = false) UUID damTomId) {
    try {
      UUID tenantId = getCurrentUser().getTenantId().getId();
      UUID userId = getCurrentUser().getId().getId();
      User currentUser =
          userService.findUserById(getCurrentUser().getTenantId(), getCurrentUser().getId());
      if (currentUser.getAuthority().name().equals("TENANT_ADMIN")) {
        List<DamTomEntity> damTomEntities =
            damTomService.getDamTomActiveByTenantIdAndSearchTextMobi(tenantId, "");
        List<UUID> listIds = new ArrayList<>();
        for (DamTomEntity damTomEntity : damTomEntities) {
          listIds.add(damTomEntity.getId());
        }
        int count = lichSuCanhBaoService.getTotalActiveAlarm(damTomId, listIds, tenantId);
        return new ResponseEntity<>(count, HttpStatus.OK);
      } else {
        List<DamTomStaffEntity> list = damTomService.getListOfDamTomStaff(userId);
        List<UUID> listIds = new ArrayList<>();
        for (DamTomStaffEntity damTomStaffEntity : list) {
          listIds.add(damTomStaffEntity.getDamtomId());
        }
        List<DamTomEntity> damTomEntities =
            damTomService.getDamTomActiveByListIDMobi(listIds, userId);
        List<UUID> damTomIds = new ArrayList<>();
        for (DamTomEntity damTomEntity : damTomEntities) {
          damTomIds.add(damTomEntity.getId());
        }
        int count = lichSuCanhBaoService.getTotalActiveAlarm(damTomId, listIds, tenantId);
        return new ResponseEntity<>(count, HttpStatus.OK);
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  Alarm checkAlarmId(AlarmId alarmId, Operation operation) {
    try {
      validateId(alarmId, "Incorrect alarmId " + alarmId);
      Alarm alarm = alarmService.findAlarmByIdAsync(getCurrentUser().getTenantId(), alarmId).get();
      checkNotNull(alarm);
      accessControlService.checkPermission(
          getCurrentUser(), Resource.ALARM, operation, alarmId, alarm);
      return alarm;
    } catch (Exception e) {
      return null;
    }
  }
}
