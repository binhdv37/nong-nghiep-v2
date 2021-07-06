package org.thingsboard.server.dft.controllers.web.report.schedule.dtos;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.thingsboard.server.dft.controllers.web.role.dtos.PermissionDto;
import org.thingsboard.server.dft.entities.ReportScheduleEntity;

@Getter
@Setter
public class ReportScheduleDto {

    private UUID id;

    private UUID tenantId;

    private UUID damTomId;

    private String scheduleName;

    private String reportName;

    private String cron;

    private Collection<ReportScheduleUserDto> users;

    private String note;

    private UUID createdBy;

    private long createdTime;

    private boolean active;

    public ReportScheduleDto() {
    }

    public ReportScheduleDto(ReportScheduleEntity entity) {
        if (entity == null) {
            return;
        }
        this.id = entity.getId();
        this.tenantId = entity.getTenantId();
        this.damTomId = entity.getDamTomId();
        this.scheduleName = entity.getScheduleName();
        this.reportName = entity.getReportName();
        this.cron = entity.getCron();
        this.users = entity.getUsers()
            .stream()
            .map(ReportScheduleUserDto::new)
            .collect(Collectors.toList());
        this.note = entity.getNote();
        this.createdBy = entity.getCreatedBy();
        this.createdTime = entity.getCreatedTime();
        this.active = entity.isActive();
    }
}
