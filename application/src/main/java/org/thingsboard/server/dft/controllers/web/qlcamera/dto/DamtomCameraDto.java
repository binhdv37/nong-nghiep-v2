package org.thingsboard.server.dft.controllers.web.qlcamera.dto;

import org.thingsboard.server.dft.entities.DamTomCameraEntity;

import java.util.UUID;

public class DamtomCameraDto {
    private UUID id;
    private UUID tenantId;
    private UUID damtomId;
    private String code;
    private String name;
    private String url;
    private boolean main;
    private String note;
    private UUID createdBy;
    private long createdTime;
    private EditCamErrorDto errorDto;

    public DamtomCameraDto() {
    }

    public DamtomCameraDto(DamTomCameraEntity cameraEntity) {
        this.id = cameraEntity.getId();
        this.tenantId = cameraEntity.getTenantId();
        this.damtomId = cameraEntity.getDamtomId();
        this.name = cameraEntity.getName();
        this.code = cameraEntity.getCode();
        this.url = cameraEntity.getUrl();
        this.note = cameraEntity.getNote();
        this.main = cameraEntity.isMain();
        this.createdBy = cameraEntity.getCreatedBy();
        this.createdTime = cameraEntity.getCreatedTime();
    }

    public DamtomCameraDto(EditCamErrorDto errorDto){
        this.errorDto = errorDto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getDamtomId() {
        return damtomId;
    }

    public void setDamtomId(UUID damtomId) {
        this.damtomId = damtomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public EditCamErrorDto getErrorDto() {
        return errorDto;
    }

    public void setErrorDto(EditCamErrorDto errorDto) {
        this.errorDto = errorDto;
    }
}
