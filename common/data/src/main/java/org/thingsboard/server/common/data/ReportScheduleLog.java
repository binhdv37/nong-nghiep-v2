package org.thingsboard.server.common.data;

import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DamTomLogId;
import org.thingsboard.server.common.data.id.ReportScheduleLogId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.List;
import java.util.UUID;

public class ReportScheduleLog extends SearchTextBasedWithAdditionalInfo<ReportScheduleLogId> implements HasName, HasTenantId, HasCustomerId{
    private TenantId tenantId;
    private CustomerId customerId;
    private DamTomLogId damTomLogId;
    private String scheduleName;
    private String reportName;
    private String cron;
    private String note;
    private List<UUID> users;
    private boolean active;

    public ReportScheduleLog() {
        super();
    }

    public ReportScheduleLog(ReportScheduleLogId id) {
        super(id);
    }

    public ReportScheduleLog(ReportScheduleLogId reportScheduleLogId, TenantId tenantId,
                             CustomerId customerId, DamTomLogId damtomLogId, String scheduleName,
                             String reportName, String cron, String note, List<UUID> users, boolean active) {
        this.setId(reportScheduleLogId);
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.damTomLogId = damtomLogId;
        this.scheduleName = scheduleName;
        this.reportName = reportName;
        this.cron = cron;
        this.note = note;
        this.users = users;
        this.active = active;
    }

    @Override
    public String getSearchText() {
        return scheduleName;
    }

    public ReportScheduleLog(ReportScheduleLog reportScheduleLog) {
        super(reportScheduleLog);
        this.tenantId = reportScheduleLog.getTenantId();
        this.customerId = reportScheduleLog.getCustomerId();
        this.damTomLogId = reportScheduleLog.getDamTomLogId();
        this.scheduleName = reportScheduleLog.getScheduleName();
        this.reportName = reportScheduleLog.getReportName();
        this.cron = reportScheduleLog.getCron();
        this.note = reportScheduleLog.getNote();
        this.users = reportScheduleLog.getUsers();
        this.active = reportScheduleLog.isActive();
    }

    @Override
    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public CustomerId getCustomerId() {
        return customerId;
    }

    public void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
    }

    @Override
    public String getName() {
        return scheduleName;
    }

    public void setName(String name) {
        this.scheduleName = name;
    }

    public DamTomLogId getDamTomLogId() {
        return damTomLogId;
    }

    public void setDamTomLogId(DamTomLogId damTomLogId) {
        this.damTomLogId = damTomLogId;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<UUID> getUsers() {
        return users;
    }

    public void setUsers(List<UUID> users) {
        this.users = users;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
