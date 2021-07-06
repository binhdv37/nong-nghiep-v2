package org.thingsboard.server.dft.controllers.web.report.schedule.dtos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.entities.ReportScheduleEntity;
import org.thingsboard.server.dft.entities.ReportScheduleUserEntity;

@Getter
@Setter
public class ReportScheduleCreateOrUpdateDto {

    private UUID tenantId;

    @NotNull(message = "Nhà vườn không được null")
    private UUID damTomId;

    @NotBlank(message = "Tên lịch xuất báo cáo không được để trống")
    private String scheduleName;

    @NotBlank(message = "Tên báo cáo không được để trống")
    private String reportName;

    @NotBlank(message = "Cấu hình tự động không được để trống")
    @Pattern(regexp = "(\\*|[0-5]?[0-9]) (\\*|[0-5]?[0-9]) (\\*|[0-9]|1[0-9]|2[0-3]) (\\*|[0-9]|1[0-9]|2[0-9]|30|31) (\\*|[0-9]|1[0-2]) (\\*|[0-7])",message = "Cron should be valid")
    private String cron;

    private String note;

    private UUID createdBy;

    private long createdTime;

    private boolean active;

    @Valid()
    @NotNull(message = "Người nhận báo cáo không được để trống")
    private Collection<UUID> users;

    public ReportScheduleCreateOrUpdateDto() {
    }

    public ReportScheduleEntity updateEntity(UUID entityId, UUID updateBy) {
        long createTime = System.currentTimeMillis();

        ReportScheduleEntity entity = new ReportScheduleEntity();
        entity.setId(entityId);
        entity.setTenantId(this.tenantId);
        entity.setDamTomId(this.damTomId);
        entity.setScheduleName(this.scheduleName);
        entity.setReportName(this.reportName);
        entity.setCron(this.cron);
        entity.setNote(this.note);
        entity.setCreatedBy(updateBy);
        entity.setCreatedTime(createTime);
        entity.setActive(this.active);
        Collection<ReportScheduleUserEntity> userEntities = new ArrayList<>();
        for (UUID userId : this.users) {
            UserEntity user = new UserEntity();
            user.setId(userId);
            ReportScheduleUserEntity reportScheduleUserEntity = new ReportScheduleUserEntity();

            reportScheduleUserEntity.setScheduleId(entity.getId());
            reportScheduleUserEntity.setUser(user);
            reportScheduleUserEntity.setCreatedTime(createTime);
            reportScheduleUserEntity.setCreatedBy(updateBy);

            userEntities.add(reportScheduleUserEntity);
        }
        entity.setUsers(userEntities);

        return entity;
    }

    public ReportScheduleEntity createEntity(UUID createdBy) {
        long createTime = System.currentTimeMillis();

        ReportScheduleEntity entity = new ReportScheduleEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(this.tenantId);
        entity.setDamTomId(this.damTomId);
        entity.setScheduleName(this.scheduleName);
        entity.setReportName(this.reportName);
        entity.setCron(this.cron);
        entity.setNote(this.note);
        entity.setCreatedBy(createdBy);
        entity.setCreatedTime(createTime);
        entity.setActive(this.active);
        Collection<ReportScheduleUserEntity> userEntities = new ArrayList<>();
        for (UUID userId : this.users) {
            UserEntity user = new UserEntity();
            user.setId(userId);
            ReportScheduleUserEntity reportScheduleUserEntity = new ReportScheduleUserEntity();

            reportScheduleUserEntity.setScheduleId(entity.getId());
            reportScheduleUserEntity.setUser(user);
            reportScheduleUserEntity.setCreatedTime(createTime);
            reportScheduleUserEntity.setCreatedBy(createdBy);

            userEntities.add(reportScheduleUserEntity);
        }
        entity.setUsers(userEntities);

        return entity;
    }
}
