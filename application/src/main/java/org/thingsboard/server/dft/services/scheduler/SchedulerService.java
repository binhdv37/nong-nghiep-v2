package org.thingsboard.server.dft.services.scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.common.constants.ReportScheduleConstant;
import org.thingsboard.server.dft.common.service.TimeQueryService;
import org.thingsboard.server.dft.common.service.TimestampService;
import org.thingsboard.server.dft.entities.ReportScheduleEntity;
import org.thingsboard.server.dft.entities.ReportScheduleUserEntity;
import org.thingsboard.server.dft.repositories.ReportScheduleRepository;
import org.thingsboard.server.dft.services.baoCaoDLGiamSat.BcDlGiamSatService;
import org.thingsboard.server.dft.services.baocaoCanhBao.BcCanhBaoService;
import org.thingsboard.server.dft.services.baocaoThongBao.BcThongBaoService;
import org.thingsboard.server.dft.services.baocaoTongHop.BcTongHopService;
import org.thingsboard.server.dft.services.baocaoketnoi.BaoCaoKetNoiService;
import org.thingsboard.server.service.mail.DefaultMailService;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SchedulerService {

  // trungdt - mảng danh sách Task runnner xuất lịch báo cáo
  private Map<String, ScheduledFuture<?>> schedules;

  private final ThreadPoolTaskScheduler taskScheduler;

  private final ReportScheduleRepository reportScheduleRepository;

  private final TimestampService timestampService;
  private final TimeQueryService timeQueryService;

  private final DefaultMailService defaultMailService;

  private final BaoCaoKetNoiService baoCaoKetNoiService;
  private final BcDlGiamSatService bcDlGiamSatService;
  private final BcThongBaoService bcThongBaoService;
  private final BcCanhBaoService bcCanhBaoService;
  private final BcTongHopService bcTongHopService;

  @Autowired
  public SchedulerService(
      ThreadPoolTaskScheduler taskScheduler,
      ReportScheduleRepository reportScheduleRepository,
      TimestampService timestampService,
      TimeQueryService timeQueryService,
      DefaultMailService defaultMailService,
      BaoCaoKetNoiService baoCaoKetNoiService,
      BcDlGiamSatService bcDlGiamSatService,
      BcThongBaoService bcThongBaoService,
      BcCanhBaoService bcCanhBaoService,
      BcTongHopService bcTongHopService) {
    this.taskScheduler = taskScheduler;
    this.reportScheduleRepository = reportScheduleRepository;
    this.timestampService = timestampService;
    this.timeQueryService = timeQueryService;
    this.defaultMailService = defaultMailService;
    this.baoCaoKetNoiService = baoCaoKetNoiService;
    this.bcDlGiamSatService = bcDlGiamSatService;
    this.bcThongBaoService = bcThongBaoService;
    this.bcCanhBaoService = bcCanhBaoService;
    this.bcTongHopService = bcTongHopService;
  }

  @PostConstruct
  public void scheduleRunnableWithCronTrigger() throws ThingsboardException {
    this.schedules = new HashMap<>();

    List<ReportScheduleEntity> schedules = this.reportScheduleRepository.getAllByActive(true);
    for (ReportScheduleEntity schedule : schedules) {
      try {
        this.addTask(schedule.getId().toString(), schedule.getCron(), createScheduleTask(schedule));
      } catch (Exception e) {
        throw new ThingsboardException(e.getMessage(), ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }
    }
  }

  private RunnableTask createScheduleTask(ReportScheduleEntity schedule) {
    return new RunnableTask(
        schedule.getDamTomId(),
        schedule.getReportName(),
        schedule.getCron(),
        schedule.getUsers().stream()
            .map(ReportScheduleUserEntity::getUser)
            .collect(Collectors.toList()));
  }

  private void addTask(String scheduleId, String cron, RunnableTask task) {
    ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(cron));
    this.schedules.put(scheduleId, future);
  }

  private void removeTask(String scheduleId) {
    ScheduledFuture<?> future = this.schedules.get(scheduleId);
    if (future == null) {
      return;
    }
    future.cancel(true);
    this.schedules.remove(scheduleId);
  }

  private void updateTask(String scheduleId, String cron, RunnableTask task) {
    this.removeTask(scheduleId);
    this.addTask(scheduleId, cron, task);
  }

  public ReportScheduleEntity createSchedule(ReportScheduleEntity entity)
      throws ThingsboardException {
    ReportScheduleEntity result = this.reportScheduleRepository.save(entity);

    if (result.isActive()) {
      try {
        this.addTask(result.getId().toString(), result.getCron(), createScheduleTask(entity));
      } catch (Exception e) {
        throw new ThingsboardException(e.getMessage(), ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }
    }

    return result;
  }

  public ReportScheduleEntity updateSchedule(ReportScheduleEntity entity)
      throws ThingsboardException {
    if (entity.getId() == null) {
      throw new ThingsboardException(
          "Schedule ID is NULL", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }

    ReportScheduleEntity result = this.reportScheduleRepository.save(entity);

    if (result.isActive()) {
      try {
        this.updateTask(result.getId().toString(), result.getCron(), createScheduleTask(result));
      } catch (Exception e) {
        throw new ThingsboardException(e.getMessage(), ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }
    } else {
      this.removeTask(result.getId().toString());
    }

    return result;
  }

  public void deleteSchedule(UUID id) {
    this.reportScheduleRepository.deleteById(id);
    this.removeTask(id.toString());
  }

  class RunnableTask implements Runnable {

    private final UUID damTomId;
    private final String reportName;
    private final String cron;
    private final Collection<UserEntity> users;

    public RunnableTask(
        UUID damTomId, String reportName, String cron, Collection<UserEntity> users) {
      this.damTomId = damTomId;
      this.reportName = reportName;
      this.cron = cron;
      this.users = users;
    }

    private boolean isDailyCron(String str) {
      return Pattern.compile("^(\\d{1,2}|\\*) (\\d{1,2}|\\*) (\\d{1,2}|\\*) (\\*) (\\*) (\\*)$")
          .matcher(str)
          .matches();
    }

    private boolean isWeeklyCron(String str) {
      return Pattern.compile(
              "^(\\d{1,2}|\\*) (\\d{1,2}|\\*) (\\d{1,2}|\\*) (\\*) (\\*) (\\d{1,2})$")
          .matcher(str)
          .matches();
    }

    private boolean isMonthlyCron(String str) {
      return Pattern.compile(
              "^(\\d{1,2}|\\*) (\\d{1,2}|\\*) (\\d{1,2}|\\*) (\\d{1,2}) (\\*) (\\*)$")
          .matcher(str)
          .matches();
    }

    @SneakyThrows
    @Override
    public void run() {
      Date date = new Date();
      long endTs = date.getTime();
      long startTs = 0;
      if (isDailyCron(this.cron)) {
        // Lặp lại - Ngày
        startTs = timestampService.getStartDayTs();
      }

      if (isWeeklyCron(this.cron)) {
        // Lặp lại - Tuần
        startTs = timestampService.getStartDayOfWeekTs();
      }

      if (isMonthlyCron(this.cron)) {
        // Lặp lại - Tháng
        startTs = timestampService.getStartDayOfMonthTs();
      }

      log.info(
          "Thoi gian truy van bao cao: "
              + timeQueryService.getDateByTimestamp(startTs)
              + "- "
              + timeQueryService.getDateByTimestamp(endTs));
      UUID tenantId = users.stream().findFirst().get().getTenantId();
      String subject = "";
      String msgData = "";
      if (startTs > 0) {
        if (reportName.equalsIgnoreCase(ReportScheduleConstant.MONITORING_DATA_REPORT)) {
          if (isDailyCron(this.cron)) {
            subject =
                "Báo cáo dữ liệu giám sát ngày " + timeQueryService.getDateByTimestamp(startTs);
          } else {
            subject =
                "Báo cáo dữ liệu giám sát từ ngày "
                    + timeQueryService.getDateByTimestamp(startTs)
                    + " đến ngày "
                    + timeQueryService.getDateByTimestamp(endTs);
          }
          msgData = bcDlGiamSatService.getMailContentData(tenantId, startTs, endTs);
        } else if (reportName.equalsIgnoreCase(ReportScheduleConstant.SENSOR_CONNECTION_REPORT)) {
          if (isDailyCron(this.cron)) {
            subject =
                "Báo cáo dữ kết nối cảm biến ngày " + timeQueryService.getDateByTimestamp(startTs);
          } else {
            subject =
                "Báo cáo dữ kết nối cảm biến từ ngày "
                    + timeQueryService.getDateByTimestamp(startTs)
                    + " đến ngày "
                    + timeQueryService.getDateByTimestamp(endTs);
          }
          if (damTomId != null) {
            msgData = baoCaoKetNoiService.getMailContentData(tenantId, damTomId, startTs, endTs);
          } else {
            msgData = baoCaoKetNoiService.getMailContentData(tenantId, null, startTs, endTs);
          }
        } else if (reportName.equalsIgnoreCase(ReportScheduleConstant.NOTIFICATION_DATA_REPORT)) {
          if (isDailyCron(this.cron)) {
            subject = "Báo cáo thông báo biến ngày " + timeQueryService.getDateByTimestamp(startTs);
          } else {
            subject =
                "Báo cáo thông báo biến từ ngày "
                    + timeQueryService.getDateByTimestamp(startTs)
                    + " đến ngày "
                    + timeQueryService.getDateByTimestamp(endTs);
          }
          msgData = bcThongBaoService.getMailContentData(tenantId, startTs, endTs);
        } else if (reportName.equalsIgnoreCase(ReportScheduleConstant.WARNING_REPORT)) {
          if (isDailyCron(this.cron)) {
            subject = "Báo cáo cảnh báo ngày " + timeQueryService.getDateByTimestamp(startTs);
          } else {
            subject =
                "Báo cáo cảnh báo từ ngày "
                    + timeQueryService.getDateByTimestamp(startTs)
                    + " đến ngày "
                    + timeQueryService.getDateByTimestamp(endTs);
          }

          msgData = bcCanhBaoService.getMailContentData(tenantId, damTomId, startTs, endTs);
        } else if (reportName.equalsIgnoreCase(ReportScheduleConstant.SYNTHESIS_REPORT)) {
          if (isDailyCron(this.cron)) {
            subject = "Báo cáo tổng hợp ngày " + timeQueryService.getDateByTimestamp(startTs);
          } else {
            subject =
                "Báo cáo tổng hợp từ ngày "
                    + timeQueryService.getDateByTimestamp(startTs)
                    + " đến ngày "
                    + timeQueryService.getDateByTimestamp(endTs);
          }
          if (damTomId != null) {
            msgData = bcTongHopService.getMailContentData(tenantId, damTomId, startTs, endTs);
          } else {
            msgData = bcTongHopService.getMailContentData(tenantId, null, startTs, endTs);
          }
        }
      }

      if (!subject.equals("") && !msgData.equals("")) {
        for (UserEntity userEntity : users) {
          defaultMailService.sendEmail(
              new TenantId(tenantId), userEntity.getEmail(), subject, msgData);
        }
      }
      System.out.println(
          new Date()
              + "Bao cao - "
              + reportName
              + " - on thread "
              + Thread.currentThread().getName());
    }
  }

  /** @author viettd */
  public Page<ReportScheduleEntity> findByTenantId(
      UUID tenantId, String textSearch, Pageable pageable) {
    return reportScheduleRepository.findByTenantId(tenantId, textSearch, pageable);
  }

  public ReportScheduleEntity findById(UUID id) {
    return reportScheduleRepository.findById(id).orElse(null);
  }

  public boolean checkCreateExistName(UUID tenantId, String scheduleName) {
    return reportScheduleRepository.existsByTenantIdAndScheduleName(tenantId, scheduleName);
  }

  public boolean checkUpdateExistName(UUID tenantId, UUID scheduleId, String scheduleName) {
    return reportScheduleRepository.existsByTenantIdAndIdNotAndScheduleName(
        tenantId, scheduleId, scheduleName);
  }
}
