package org.thingsboard.server.dft.common.validator.khachhang;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.entities.KhachHangEntity;
import org.thingsboard.server.dft.repositories.DftUserRepository;
import org.thingsboard.server.dft.repositories.KhachHangRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ValidateKhachHangService {
  public static final String EMAIL = "email";
  public static final String PHONE = "soDienThoai";
  public static final String MAKHACHHANG = "maKhachHang";

  public static final String TYPE_ADD = "ADD_ENTITY";
  public static final String TYPE_EDIT = "EDIT_ENTITY";

  private final KhachHangRepository khachHangRepository;
  private final DftUserRepository dftUserRepository;

  @Autowired
  public ValidateKhachHangService(KhachHangRepository khachHangRepository, DftUserRepository dftUserRepository) {
    this.khachHangRepository = khachHangRepository;
    this.dftUserRepository = dftUserRepository;
  }


  public boolean validateKhachHang(String type, String field, String value, UUID id) {
    value = value.trim();
    switch (type) {
      case TYPE_ADD:
        switch (field) {
          case EMAIL:
            return dftUserRepository.findDistinctFirstByEmail(value) != null ? true: false;
          case PHONE:
            return dftUserRepository.findDistinctFirstByLastName(value) != null ? true: false;
          case MAKHACHHANG:
            return khachHangRepository.findAllByMaKhachHang(value) != null ? true: false;
          default:
            return false;
        }
      case TYPE_EDIT:
        switch (field) {
          case PHONE:
            KhachHangEntity khachHangEntity = khachHangRepository.findById(id).get();
            return dftUserRepository.findUserEntitiesByTenantIdNotLikeAndLastName(khachHangEntity.getTenantEntity().getId(), value).isEmpty() ? false : true;
          case MAKHACHHANG:
            return khachHangRepository.findAllByMaKhachHangAndIdNotEqual(value, id) != null ? true: false;
          default:
            return false;
        }
      default:
        return false;
    }
  }
}
