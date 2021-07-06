package org.thingsboard.server.dft.controllers.web.damtom.dtos;

import org.thingsboard.server.common.data.id.IdBased;
import org.thingsboard.server.dft.common.id.DamTomId;
import org.thingsboard.server.dft.controllers.web.asset.dtos.AssetDto;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomStaffEntity;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DamTomDto {
    private String tenantId;
    private DamTomId id;
    private AssetDto asset;
    private String name;
    private String address;
    private String note;
    private String searchText;
    private String images;
    private String createdBy;
    private long createdTime;
    private boolean active;
    private List<String> staffs;
    private int message;

    public int getMessage() {
        return message;
    }

    public void setMessage(int message) {
        this.message = message;
    }

    public List<String> getStaffs() {
        return staffs;
    }

    public void setStaffs(List<String> staffs) {
        this.staffs = staffs;
    }

    public DamTomDto() {
    }


    public DamTomDto(DamTomEntity entity) {
        this.id = new DamTomId(entity.getId());
        this.tenantId = entity.getTenantId().toString();
        this.name = entity.getName();
        this.address = entity.getAddress();
        this.note = entity.getNote();
        this.searchText = entity.getSearchText();
        this.images = entity.getImages();
        this.createdBy = entity.getCreatedBy().toString();
        this.active = entity.isActive();
        this.createdTime = entity.getCreatedTime();
        this.asset = new AssetDto(entity.getAsset());
//        this.staffs = entity.getStaffs();
    }

    public DamTomEntity toDamTomEntity() {
        DamTomEntity damTomEntity = new DamTomEntity();
        damTomEntity.setId(UUID.randomUUID());
        damTomEntity.setName(this.getName());
        damTomEntity.setAddress(this.getAddress());
        damTomEntity.setNote(this.getNote());
        damTomEntity.setCreatedTime(new Date().getTime());
        damTomEntity.setActive(this.isActive());
        damTomEntity.setCreatedBy(UUID.fromString(this.getCreatedBy()));
        return damTomEntity;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetDto getAsset() {
        return asset;
    }

    public void setAsset(AssetDto asset) {
        this.asset = asset;
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

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
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

    public DamTomId getId() {
        return id;
    }

    public void setId(DamTomId id) {
        this.id = id;
    }
}
