package org.thingsboard.server.dft.entities;

import java.util.Collection;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "damtom_report_schedule")
public class ReportScheduleEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "damtom_id")
    private UUID damTomId;

    @Column(name = "schedule_name")
    private String scheduleName;

    @Column(name = "report_name")
    private String reportName;

    @Column(name = "cron")
    private String cron;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "scheduleId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<ReportScheduleUserEntity> users;

    @Column(name = "note")
    private String note;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_time")
    private long createdTime;

    @Column(name = "active")
    private boolean active;

    public ReportScheduleEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDamTomId() {
        return damTomId;
    }

    public void setDamTomId(UUID damTomId) {
        this.damTomId = damTomId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Collection<ReportScheduleUserEntity> getUsers() {
        return users;
    }

    public void setUsers(
        Collection<ReportScheduleUserEntity> users) {
        this.users = users;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
