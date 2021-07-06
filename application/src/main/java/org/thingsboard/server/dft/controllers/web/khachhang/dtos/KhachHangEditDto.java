package org.thingsboard.server.dft.controllers.web.khachhang.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.common.validator.khachhang.anotation.NoMoreWhiteSpace;
import org.thingsboard.server.dft.common.validator.khachhang.anotation.UniqueEditMaKhachHang;
import org.thingsboard.server.dft.common.validator.khachhang.anotation.UniqueEditPhoneNumber;
import org.thingsboard.server.dft.entities.KhachHangEntity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@UniqueEditMaKhachHang(message = "UniqueMaKhachHang")
@UniqueEditPhoneNumber(message = "UniquePhoneNumber")
public class KhachHangEditDto {

  private UUID id;

  @NotNull(message = "required")
  @NotBlank(message = "required")
//  @NoSpace(message = "whitespace")
  @Size(max = 50, message = "maxlenght")
  private String maKhachHang;

  @NotNull(message = "required")
  @NotBlank(message = "required")
//  @NoMoreWhiteSpace(message = "muchWhiteSpace")
  @Size(max = 255, message = "maxlenght")
  private String tenKhachHang;

  @NotNull(message = "required")
  @NotBlank(message = "required")
  private String soDienThoai;

  @NotNull(message = "required")
  @NotBlank(message = "required")
  @Email(message = "email")
//  @NoSpace(message = "whitespace")
  private String email;

  private long ngayBatDau;

  @NoMoreWhiteSpace(message = "muchWhiteSpace")
  @Size(max = 4000, message = "maxlength")
  private String ghiChu;

  private boolean active;
  private List<String> dsTaiLieu;
  private String createdBy;
  private long createdTime;

  public KhachHangEditDto(KhachHangEntity khachHangEntity) {
    this.id = khachHangEntity.getId();
    this.maKhachHang = khachHangEntity.getMaKhachHang();
    this.tenKhachHang = khachHangEntity.getTenKhachHang();
    this.soDienThoai = khachHangEntity.getTenantEntity().getPhone();
    this.email = khachHangEntity.getTenantEntity().getEmail();
    this.ngayBatDau = khachHangEntity.getNgayBatDau().getTime();
    this.ghiChu = khachHangEntity.getGhiChu();
    this.active = khachHangEntity.isActive();
    this.createdBy = khachHangEntity.getCreatedBy().toString();
    this.createdTime = khachHangEntity.getCreatedTime();
  }
}
