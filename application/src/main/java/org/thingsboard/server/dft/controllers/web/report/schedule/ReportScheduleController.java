package org.thingsboard.server.dft.controllers.web.report.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.ReportScheduleLog;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DamTomLogId;
import org.thingsboard.server.common.data.id.ReportScheduleLogId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.common.validator.reportSchedule.ReportScheduleValidator;
import org.thingsboard.server.dft.controllers.web.report.schedule.dtos.ReportScheduleCreateOrUpdateDto;
import org.thingsboard.server.dft.controllers.web.report.schedule.dtos.ReportScheduleDto;
import org.thingsboard.server.dft.entities.ReportScheduleEntity;
import org.thingsboard.server.dft.services.scheduler.SchedulerService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@TbCoreComponent
@RequestMapping("/api/report-schedule")
public class ReportScheduleController extends BaseController {

  @Autowired private SchedulerService schedulerService;

  private final ReportScheduleValidator reportsValidator;

  @Autowired
  public ReportScheduleController(ReportScheduleValidator reportsValidator) {
    this.reportsValidator = reportsValidator;
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', \""
          + PermissionConstants.PAGES_REPORT_SCHEDULE_CREATE
          + "\")")
  @PostMapping
  @ResponseBody
  public ReportScheduleDto create(@Valid @RequestBody ReportScheduleCreateOrUpdateDto input)
      throws ThingsboardException {

    input.setTenantId(getCurrentUser().getTenantId().getId());

    try {
      ReportScheduleEntity entity = input.createEntity(getCurrentUser().getId().getId());
      // huydv check lap lich
      boolean checkReportName =
          reportsValidator.validateReportScheduleName(
              entity.getTenantId(),
              ReportScheduleValidator.TYPE_ADD,
              entity.getScheduleName(),
              entity.getId());
      if (checkReportName) {
        throw new IllegalArgumentException("Report schedule name existed!");
      }
      ReportScheduleEntity savedScheduleEntity = this.schedulerService.createSchedule(entity);

      // binhdv - ghi log
      List<UUID> users =
          savedScheduleEntity.getUsers() == null
              ? null
              : savedScheduleEntity.getUsers().stream()
                  .map(x -> x.getUser().getId())
                  .collect(Collectors.toList());
      ReportScheduleLog reportScheduleLog =
          new ReportScheduleLog(
              new ReportScheduleLogId(savedScheduleEntity.getId()),
              getTenantId(),
              getCurrentUser().getCustomerId(),
              new DamTomLogId(savedScheduleEntity.getDamTomId()),
              savedScheduleEntity.getScheduleName(),
              savedScheduleEntity.getReportName(),
              savedScheduleEntity.getCron(),
              savedScheduleEntity.getNote(),
              users,
              savedScheduleEntity.isActive());
      logEntityAction(
          reportScheduleLog.getId(),
          reportScheduleLog,
          getCurrentUser().getCustomerId(),
          ActionType.ADDED,
          null);

      return new ReportScheduleDto(savedScheduleEntity);
    } catch (Exception e) {
      // binhdv - ghi log
      List<UUID> users = (List<UUID>) input.getUsers();
      ReportScheduleLog reportScheduleLog =
          new ReportScheduleLog(
              null,
              getTenantId(),
              getCurrentUser().getCustomerId(),
              new DamTomLogId(input.getDamTomId()),
              input.getScheduleName(),
              input.getReportName(),
              input.getCron(),
              input.getNote(),
              users,
              input.isActive());
      logEntityAction(
          emptyId(EntityType.REPORT_SCHEDULE), reportScheduleLog, null, ActionType.ADDED, e);

      throw handleException(e);
    }
  }

  /** @author viettd Get report Schedules */
  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', \""
          + PermissionConstants.PAGES_REPORT_SCHEDULE
          + "\")")
  @GetMapping
  public PageData<ReportScheduleDto> getReportSchedule(
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "") String textSearch,
      @RequestParam(required = false, defaultValue = "reportName") String sortProperty,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder)
      throws ThingsboardException {
    try {
      UUID tenantID = getCurrentUser().getTenantId().getId();

      Pageable pageable =
          PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
      textSearch = textSearch.trim();

      Page<ReportScheduleDto> pageReportSchedule =
          schedulerService
              .findByTenantId(tenantID, textSearch, pageable)
              .map(ReportScheduleDto::new);

      PageData<ReportScheduleDto> pageDataReportSchedule =
          new PageData<>(
              pageReportSchedule.getContent(),
              pageReportSchedule.getTotalPages(),
              pageReportSchedule.getTotalElements(),
              pageReportSchedule.hasNext());
      System.out.println(pageDataReportSchedule.getTotalElements());
      return pageDataReportSchedule;

    } catch (Exception e) {
      throw handleException(e);
    }
  }

  /** @author viettd Get report Schedule */
  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', \""
          + PermissionConstants.PAGES_REPORT_SCHEDULE
          + "\")")
  @GetMapping("/{id}")
  public ReportScheduleDto getReportSchedule(@PathVariable("id") UUID id)
      throws ThingsboardException {
    try {
      ReportScheduleEntity reportScheduleEntity = schedulerService.findById(id);
      return checkNotNull(new ReportScheduleDto(reportScheduleEntity));
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @GetMapping("/check-name-exist")
  public ResponseEntity<?> mobiScheduleNameExist(
      @RequestParam(name = "scheduleId", required = false) UUID scheduleId,
      @RequestParam(name = "name") String scheduleName)
      throws ThingsboardException {
    try {

      scheduleName = scheduleName.trim();

      // scheduleId = null -> create schedule
      if (scheduleId == null) {
        boolean isScheduleExist =
            schedulerService.checkCreateExistName(getTenantId().getId(), scheduleName);
        return ResponseEntity.ok(isScheduleExist);
      }

      // scheduleId != null -> update schedule
      boolean isScheduleExist =
          schedulerService.checkUpdateExistName(getTenantId().getId(), scheduleId, scheduleName);
      return ResponseEntity.ok(isScheduleExist);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', \""
          + PermissionConstants.PAGES_REPORT_SCHEDULE_EDIT
          + "\")")
  @PutMapping("/{id}")
  @ResponseBody
  public ReportScheduleDto edit(
      @Valid @RequestBody ReportScheduleCreateOrUpdateDto input, @PathVariable UUID id)
      throws ThingsboardException {

    if (id == null) {
      throw new ThingsboardException(
          "Schedule ID is NULL", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }

    input.setTenantId(getCurrentUser().getTenantId().getId());

    try {
      ReportScheduleEntity entity = input.updateEntity(id, getCurrentUser().getId().getId());

      // huydv check lap lich
      boolean checkReportName =
          reportsValidator.validateReportScheduleName(
              entity.getTenantId(),
              ReportScheduleValidator.TYPE_EDIT,
              entity.getScheduleName(),
              entity.getId());
      if (checkReportName) {
        throw new IllegalArgumentException("Report schedule name existed!");
      }

      ReportScheduleEntity savedScheduleEntity = this.schedulerService.updateSchedule(entity);

      // binhdv - ghi log
      List<UUID> users =
          savedScheduleEntity.getUsers() == null
              ? null
              : savedScheduleEntity.getUsers().stream()
                  .map(x -> x.getUser().getId())
                  .collect(Collectors.toList());
      ReportScheduleLog reportScheduleLog =
          new ReportScheduleLog(
              new ReportScheduleLogId(savedScheduleEntity.getId()),
              getTenantId(),
              getCurrentUser().getCustomerId(),
              new DamTomLogId(savedScheduleEntity.getDamTomId()),
              savedScheduleEntity.getScheduleName(),
              savedScheduleEntity.getReportName(),
              savedScheduleEntity.getCron(),
              savedScheduleEntity.getNote(),
              users,
              savedScheduleEntity.isActive());
      logEntityAction(
          reportScheduleLog.getId(),
          reportScheduleLog,
          getCurrentUser().getCustomerId(),
          ActionType.UPDATED,
          null);

      return new ReportScheduleDto(savedScheduleEntity);
    } catch (Exception e) {
      // binhdv - ghi log
      List<UUID> users = (List<UUID>) input.getUsers();
      ReportScheduleLog reportScheduleLog =
          new ReportScheduleLog(
              null,
              getTenantId(),
              getCurrentUser().getCustomerId(),
              new DamTomLogId(input.getDamTomId()),
              input.getScheduleName(),
              input.getReportName(),
              input.getCron(),
              input.getNote(),
              users,
              input.isActive());
      logEntityAction(
          emptyId(EntityType.REPORT_SCHEDULE), reportScheduleLog, null, ActionType.UPDATED, e);

      throw handleException(e);
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', \""
          + PermissionConstants.PAGES_REPORT_SCHEDULE_DELETE
          + "\")")
  @DeleteMapping("/{id}")
  void delete(@PathVariable UUID id) throws ThingsboardException {
    // binhdv - ghi log
    ReportScheduleEntity entity = this.schedulerService.findById(id);

    this.schedulerService.deleteSchedule(id);

    if (entity != null) {
      // xoa thanh cong
      List<UUID> users =
          entity.getUsers() == null
              ? null
              : entity.getUsers().stream()
                  .map(x -> x.getUser().getId())
                  .collect(Collectors.toList());
      ReportScheduleLog reportScheduleLog =
          new ReportScheduleLog(
              new ReportScheduleLogId(entity.getId()),
              getTenantId(),
              getCurrentUser().getCustomerId(),
              new DamTomLogId(entity.getDamTomId()),
              entity.getScheduleName(),
              entity.getReportName(),
              entity.getCron(),
              entity.getNote(),
              users,
              entity.isActive());
      logEntityAction(
          reportScheduleLog.getId(),
          reportScheduleLog,
          getCurrentUser().getCustomerId(),
          ActionType.DELETED,
          null,
          id.toString());
    }
  }
}
