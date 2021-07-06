package org.thingsboard.server.dft.entities;

import org.thingsboard.server.dao.model.sql.TenantEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "damtom_khachhang")
public class KhachHangEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name="ma_khach_hang")
    private String maKhachHang;

    @Column(name="ten_khach_hang")
    private String tenKhachHang;

    @Column(name="ngay_bat_dau")
    private Date ngayBatDau;

    @Column(name = "active")
    private boolean active;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "tai_lieu")
    private String taiLieuDinhKem;

    @OneToOne
    @JoinColumn(name = "tenant_id")
    private TenantEntity tenantEntity;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_time")
    private long createdTime;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTaiLieuDinhKem() {
        return taiLieuDinhKem;
    }

    public void setTaiLieuDinhKem(String taiLieuDinhKem) {
        this.taiLieuDinhKem = taiLieuDinhKem;
    }

    public TenantEntity getTenantEntity() {
        return tenantEntity;
    }

    public void setTenantEntity(TenantEntity tenantEntity) {
        this.tenantEntity = tenantEntity;
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
