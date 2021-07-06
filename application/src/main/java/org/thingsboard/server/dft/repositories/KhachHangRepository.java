package org.thingsboard.server.dft.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.KhachHangEntity;

import java.util.UUID;

@Repository
public interface KhachHangRepository extends PagingAndSortingRepository<KhachHangEntity, UUID> {

  @Query(
      value = "select * from damtom_khachhang kh join tenant t on t.id = kh.tenant_id WHERE " +
              "LOWER(kh.ten_khach_hang) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
              "OR LOWER(kh.ma_khach_hang) LIKE LOWER(CONCAT('%', :searchText, '%'))" +
              "OR LOWER(t.email) LIKE LOWER(CONCAT('%', :searchText, '%'))" +
              "OR LOWER(t.phone) LIKE LOWER(CONCAT('%', :searchText, '%'))", nativeQuery = true)
  Page<KhachHangEntity> findAllByTenKhachHang(
      @Param("searchText") String searchText, Pageable pageable);

  KhachHangEntity save(KhachHangEntity khachHangEntity);

  @Query(value = "select * from damtom_khachhang kh where kh.tenant_id = :tenant_id", nativeQuery = true)
  KhachHangEntity findKhachHangEntitiesByTenantId(@Param("tenant_id") UUID tenantId);

  @Query(value = "select * from damtom_khachhang kh join tenant t on t.id = kh.tenant_id WHERE " +
                  "LOWER(t.email) LIKE LOWER(:email)", nativeQuery = true)
  KhachHangEntity findAllByEmail(@Param("email") String email);

  KhachHangEntity findAllByMaKhachHang(String maKhachHang);

  @Query(value = "select * from damtom_khachhang kh join tenant t on t.id = kh.tenant_id WHERE " +
                  "LOWER(t.phone) LIKE LOWER(:phone)", nativeQuery = true)
  KhachHangEntity findAllByPhone(@Param("phone") String phone);

  @Query(value = "select * from damtom_khachhang kh join tenant t on t.id = kh.tenant_id WHERE " +
          "LOWER(t.email) LIKE LOWER(:email) AND kh.id != :id", nativeQuery = true)
  KhachHangEntity findAllByEmailAndIdNotEqual(@Param("email") String email, @Param("id") UUID id);

  @Query("select kh from KhachHangEntity kh where " +
          "LOWER(kh.maKhachHang) LIKE LOWER(:maKhachHang) and kh.id <> :id")
  KhachHangEntity findAllByMaKhachHangAndIdNotEqual(@Param("maKhachHang") String maKhachHang, @Param("id") UUID id);

  @Query(value = "select * from damtom_khachhang kh join tenant t on t.id = kh.tenant_id WHERE " +
          "LOWER(t.phone) LIKE LOWER(:phone) AND kh.id != :id", nativeQuery = true)
  KhachHangEntity findAllByPhonelAndIdNotEqual(@Param("phone") String phone, @Param("id") UUID id);

  // binhdv
  @Query(value = "select * from damtom_khachhang kh where tenant_id = :tenantId ", nativeQuery = true)
  KhachHangEntity findByTenantId(@Param("tenantId")UUID tenantId);
}
