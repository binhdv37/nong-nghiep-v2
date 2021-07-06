package org.thingsboard.server.common.data;

import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DamTomLogId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.UUID;

public class DamTomLog extends SearchTextBasedWithAdditionalInfo<DamTomLogId> implements HasName, HasTenantId, HasCustomerId{

    private TenantId tenantId;
    private CustomerId customerId;
    private String name;
    private String address;
    private String note;
    private String images;

    public DamTomLog() {
    }

    public DamTomLog(DamTomLogId damTomLogId){
        super(damTomLogId);
    }

    public DamTomLog(DamTomLogId id, TenantId tenantId, CustomerId customerId,
                     String name, String address,
                     String note, String images) {
        this.setId(id);
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.note = note;
        this.images = images;
    }

    @Override
    public String getSearchText() {
        return name;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

}
