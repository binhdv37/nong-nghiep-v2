package org.thingsboard.server.dft.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.DamTomEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DamTomRepository extends JpaRepository<DamTomEntity, UUID> {

  @Query(
      "SELECT dt FROM DamTomEntity dt WHERE (LOWER(dt.name) LIKE LOWER(CONCAT('%', :searchText, '%')) "
          + "OR LOWER(dt.searchText) LIKE LOWER(CONCAT('%', :searchText, '%')) "
          + "OR LOWER(dt.address) LIKE LOWER(CONCAT('%', :searchText, '%'))) "
          + "AND dt.tenantId = :tenantId")
  Page<DamTomEntity> findAllByTenantId(
      @Param("searchText") String searchText, @Param("tenantId") UUID tenantId, Pageable pageable);

  DamTomEntity findDamTomEntityByTenantIdAndId(UUID tenantId, UUID id);

  @Query(
      "SELECT dt FROM DamTomEntity dt WHERE (LOWER(dt.name) LIKE LOWER(CONCAT('%', :searchText, '%')) "
          + "OR LOWER(dt.searchText) LIKE LOWER(CONCAT('%', :searchText, '%')) "
          + "OR LOWER(dt.address) LIKE LOWER(CONCAT('%', :searchText, '%'))) "
          + "AND (dt.id in (:list) OR dt.id = :idCreate)")
  Page<DamTomEntity> findByIdInOrCreatedBy(
      @Param("searchText") String searchText,
      @Param("list") List<UUID> list,
      @Param("idCreate") UUID idCreate,
      Pageable pageable);

  @Query(
      "SELECT dt FROM DamTomEntity dt WHERE (LOWER(dt.name) LIKE LOWER(CONCAT('%', :searchText, '%')) "
          + "OR LOWER(dt.searchText) LIKE LOWER(CONCAT('%', :searchText, '%'))) "
          + "AND dt.tenantId = :tenantId "
          + "ORDER BY dt.createdTime DESC ")
  List<DamTomEntity> findAllByTenantIdMobi(
      @Param("searchText") String searchText, @Param("tenantId") UUID tenantId);

  List<DamTomEntity> findByIdInOrCreatedByOrderByCreatedTimeDesc(List<UUID> list, UUID idCreate);

  List<DamTomEntity> findAllByTenantIdAndName(UUID tenantId, String name);

  DamTomEntity findDamTomEntityByAssetId(UUID assetId);

  List<DamTomEntity> findAllByTenantId(UUID tenantId);

  @Query(
      "SELECT dt FROM DamTomEntity dt WHERE (LOWER(dt.name) LIKE LOWER(CONCAT('%', :searchText, '%')) "
          + "OR LOWER(dt.searchText) LIKE LOWER(CONCAT('%', :searchText, '%'))) "
          + "AND dt.active = true "
          + "AND dt.tenantId = :tenantId "
          + "ORDER BY dt.createdTime DESC ")
  List<DamTomEntity> findAllByTenantIdActiveMobi(
      @Param("searchText") String searchText, @Param("tenantId") UUID tenantId);

  List<DamTomEntity> findAllByActiveIsTrueAndIdInOrCreatedByAndActiveOrderByCreatedTimeDesc(
      List<UUID> list, UUID idCreate, boolean active);

  DamTomEntity findDamTomEntityByTenantIdAndDeviceProfileId(UUID tenantId, UUID profileId);
}
