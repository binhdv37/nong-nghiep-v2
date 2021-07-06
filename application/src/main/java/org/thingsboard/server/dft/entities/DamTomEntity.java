package org.thingsboard.server.dft.entities;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.thingsboard.server.dao.model.sql.AssetEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dft.common.id.DamTomId;
import org.thingsboard.server.dft.controllers.web.damtom.dtos.DamTomCreateDto;
import org.thingsboard.server.dft.controllers.web.damtom.dtos.DamTomDto;

@Entity
@Table(name = "damtom")
public class DamTomEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @OneToOne
    @JoinColumn(name = "asset_id", referencedColumnName = "id")
    private AssetEntity asset;

    @OneToOne
    @JoinColumn(name = "device_profile_id", referencedColumnName = "id")
    private DeviceProfileEntity deviceProfile;

//    @OneToMany(mappedBy = "damtomId", cascade = CascadeType.ALL)
//    private Collection<DamTomAlarmEntity> alarms;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "damtomId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<DamTomCameraEntity> cameras;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "note")
    private String note;

    @Column(name = "search_text")
    private String searchText;

    /*
     * trungdt - chuỗi lưu tên của ảnh lưu trên server
     * định dạng "45d58330-7a38-11eb-9eea-d551259ff106.png,45d58330-7a38-11eb-9eea-d551259ff107.png", phân cách nhau bằng dấu `,`
     * với mỗi ảnh người dùng upload, đổi tên ảnh sang UUID -> lưu vào folder trên máy chủ
     * khi cần hiển thị thì get từ URL http://xxx.xxx.xxx/public/img/45d58330-7a38-11eb-9eea-d551259ff106.png
     */
    @Column(name = "images")
    private String images;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "damtomId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<DamTomStaffEntity> staffs;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "damtomId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<DamTomGatewayEntity> gateways;


    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_time")
    private long createdTime;

    @Column(name = "active")
    private boolean active;

    public DamTomEntity() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetEntity getAsset() {
        return asset;
    }

    public void setAsset(AssetEntity asset) {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public Collection<DamTomGatewayEntity> getGateways() {
        return gateways;
    }

    public void setGateways(
        Collection<DamTomGatewayEntity> gateways) {
        this.gateways = gateways;
    }

    public DeviceProfileEntity getDeviceProfile() {
        return deviceProfile;
    }

    public void setDeviceProfile(DeviceProfileEntity deviceProfile) {
        this.deviceProfile = deviceProfile;
    }

    //    public Collection<DamTomAlarmEntity> getAlarms() {
//        return alarms;
//    }
//
//    public void setAlarms(Collection<DamTomAlarmEntity> alarms) {
//        this.alarms = alarms;
//    }

    public Collection<DamTomCameraEntity> getCameras() {
        return cameras;
    }

    public void setCameras(
        Collection<DamTomCameraEntity> cameras) {
        this.cameras = cameras;
    }


    public Collection<DamTomStaffEntity> getStaffs() {
        return staffs;
    }

    public void setStaffs(Collection<DamTomStaffEntity> staffs) {
        this.staffs = staffs;
    }

    public DamTomEntity(DamTomDto damTomDto) {
        this.setId(damTomDto.getId().getId());
        this.setName(damTomDto.getName());
        this.setAddress(damTomDto.getAddress());
        this.setNote(damTomDto.getNote());
        this.setCreatedTime(new Date().getTime());
        this.setActive(damTomDto.isActive());
        this.setSearchText(damTomDto.getName().toLowerCase());
        this.setCreatedBy(UUID.fromString(damTomDto.getCreatedBy()));
    }

    public void toEntity(DamTomDto damTomDto) {
        this.setName(damTomDto.getName());
        this.setAddress(damTomDto.getAddress());
        this.setNote(damTomDto.getNote());
        this.setActive(damTomDto.isActive());
        this.setSearchText(damTomDto.getSearchText());
        this.setImages(damTomDto.getImages());
    }

    public void toEntityCreate(DamTomCreateDto damTomCreateDto) {
        this.setName(damTomCreateDto.getName());
        this.setAddress(damTomCreateDto.getAddress());
        this.setNote(damTomCreateDto.getNote());
        this.setActive(damTomCreateDto.isActive());
        this.setSearchText(damTomCreateDto.getSearchText());
        this.setImages(damTomCreateDto.getImages());
    }

    public DamTomDto toDto() {
        DamTomDto damTomDto = new DamTomDto();
        damTomDto.setId(new DamTomId(this.id));
        damTomDto.setName(this.name);
        damTomDto.setAddress(this.address);
        damTomDto.setActive(this.active);
        damTomDto.setNote(this.note);
        damTomDto.setCreatedBy(this.createdBy.toString());
        return damTomDto;
    }

    public void pushStaffs(DamTomStaffEntity damTomStaffEntity) {
        this.staffs.add(damTomStaffEntity);
    }
}
