package org.thingsboard.server.common.data;

import org.thingsboard.server.common.data.id.*;

public class CameraLog extends SearchTextBasedWithAdditionalInfo<CameraLogId> implements HasName, HasTenantId, HasCustomerId{
    private TenantId tenantId;
    private CustomerId customerId;
    private DamTomLogId damTomLogId;
    private String name;
    private String code;
    private String url;
    private boolean main;
    private String note;

    public CameraLog() {
        super();
    }

    public CameraLog(CameraLogId id) {
        super(id);
    }

    public CameraLog(CameraLogId cameraLogId, TenantId tenantId, CustomerId customerId,
                     DamTomLogId damtomLogId, String name, String code,
                     String url, boolean main, String note) {
        this.setId(cameraLogId);
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.damTomLogId = damtomLogId;
        this.name = name;
        this.code = code;
        this.url = url;
        this.main = main;
        this.note = note;

    }

    @Override
    public String getSearchText() {
        return name;
    }

    public CameraLog(CameraLog cameraLog) {
        super(cameraLog);
        this.tenantId = cameraLog.getTenantId();
        this.customerId = cameraLog.getCustomerId();
        this.damTomLogId = cameraLog.getDamTomLogId();
        this.name = cameraLog.getName();
        this.code = cameraLog.getCode();
        this.url = cameraLog.getUrl();
        this.main = cameraLog.isMain();
        this.note = cameraLog.getNote();
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DamTomLogId getDamTomLogId() {
        return damTomLogId;
    }

    public void setDamTomLogId(DamTomLogId damTomLogId) {
        this.damTomLogId = damTomLogId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    //    @JsonIgnore
//    public boolean isSystemAdmin() {
//        return tenantId == null || EntityId.NULL_UUID.equals(tenantId.getId());
//    }
//
//    @JsonIgnore
//    public boolean isTenantAdmin() {
//        return !isSystemAdmin() && (customerId == null || EntityId.NULL_UUID.equals(customerId.getId()));
//    }
//
//    @JsonIgnore
//    public boolean isCustomerUser() {
//        return !isSystemAdmin() && !isTenantAdmin();
//    }
}
