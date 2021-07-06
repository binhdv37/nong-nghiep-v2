package org.thingsboard.server.dft.controllers.web.asset.dtos;

import org.thingsboard.server.dao.model.sql.AssetEntity;

public class AssetDto {

    private String id;
    private String tenantId;
    private String customerId;
    private long createdTime;
    private String name;
    private String type;
    private String label;
    private String searchText;

    public AssetDto() {
    }

    public AssetDto(AssetEntity entity) {
        this.id = entity.getId().toString();
        this.tenantId = entity.getTenantId().toString();
        this.customerId = entity.getCustomerId().toString();
        this.createdTime = entity.getCreatedTime();
        this.name = entity.getName();
        this.type = entity.getType();
        this.label = entity.getLabel();
        this.searchText = entity.getSearchText();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
