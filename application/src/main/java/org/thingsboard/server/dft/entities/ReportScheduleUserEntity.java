package org.thingsboard.server.dft.entities;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.thingsboard.server.dao.model.sql.UserEntity;

@Entity @IdClass(ReportScheduleUserEntityKey.class)
@Table(name = "damtom_report_schedule_user")
public class ReportScheduleUserEntity {
    @Id
    @Column(name = "schedule_id", columnDefinition = "uuid")
    private UUID scheduleId;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_time")
    private long createdTime;

    public ReportScheduleUserEntity() {
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
