package org.thingsboard.server.dft.controllers.web.report.schedule.dtos;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.thingsboard.server.dft.controllers.web.users.dtos.UsersDto;
import org.thingsboard.server.dft.entities.ReportScheduleUserEntity;

@Getter
@Setter
public class ReportScheduleUserDto {

    private UUID scheduleId;

    private UsersDto user;

    private UUID createdBy;

    private long createdTime;

    public ReportScheduleUserDto() {
    }

    public ReportScheduleUserDto(ReportScheduleUserEntity entity) {
        if (entity == null) {
            return;
        }
        this.scheduleId = entity.getScheduleId();
        this.user = new UsersDto(entity.getUser());
        this.createdBy = entity.getCreatedBy();
        this.createdTime = entity.getCreatedTime();
    }
}
