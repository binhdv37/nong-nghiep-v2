package org.thingsboard.server.dft.common.validator.reportSchedule;

import org.springframework.stereotype.Component;
import org.thingsboard.server.dft.repositories.ReportScheduleRepository;

import java.util.UUID;

@Component
public class ReportScheduleValidator {
  public static final String TYPE_ADD = "ADD_ENTITY";
  public static final String TYPE_EDIT = "EDIT_ENTITY";

  private final ReportScheduleRepository reportScheduleRepository;

  public ReportScheduleValidator(ReportScheduleRepository reportScheduleRepository) {
    this.reportScheduleRepository = reportScheduleRepository;
  }

  public boolean validateReportScheduleName(UUID tenantId, String type, String value, UUID id) {
    String scheduleName = value.trim();
    switch (type) {
      case TYPE_ADD:
        return reportScheduleRepository.existsByTenantIdAndScheduleName(tenantId, scheduleName);
      case TYPE_EDIT:
        return reportScheduleRepository.existsByTenantIdAndIdNotAndScheduleName(
                tenantId, id, scheduleName);
      default:
        return false;
    }
  }
}
