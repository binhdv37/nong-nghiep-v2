package org.thingsboard.server.dft.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.controllers.web.baocaoCanhBao.dto.BcSingleDataDto;
import org.thingsboard.server.dft.entities.DamTomSnapshotEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DamTomSnapshotRepository extends JpaRepository<DamTomSnapshotEntity, UUID> {

  @Query(
      value =
          "select ds from DamTomSnapshotEntity ds where ds.tenantId = :tenantId "
              + "and (:damtomId IS NULL OR ds.damtomId = :damtomId) "
              + "AND (:startTime IS NULL OR ds.timeSnapshot >= :startTime) "
              + "AND (:endTime IS NULL OR ds.timeSnapshot <= :endTime) "
              + "and LOWER(ds.alarmName) LIKE LOWER(CONCAT('%', :searchText, '%'))")
  Page<DamTomSnapshotEntity> findAll(
      @Param("tenantId") UUID tenantId,
      @Param("damtomId") UUID damtomId,
      @Param("startTime") Long startTime,
      @Param("endTime") Long endTime,
      @Param("searchText") String searchText,
      Pageable pageable);

  @Query(
      value =
          "select ds from DamTomSnapshotEntity ds where ds.tenantId = :tenantId "
              + "AND (:startTime IS NULL OR ds.timeSnapshot >= :startTime) "
              + "AND (:endTime IS NULL OR ds.timeSnapshot <= :endTime) "
              + "and LOWER(ds.alarmName) LIKE LOWER(CONCAT('%', :searchText, '%'))")
  Page<DamTomSnapshotEntity> findAll(
      @Param("tenantId") UUID tenantId,
      @Param("startTime") Long startTime,
      @Param("endTime") Long endTime,
      @Param("searchText") String searchText,
      Pageable pageable);

  @Query(
      value =
          "select * from damtom_snapshot ds where ds.tenant_id = :tenantId "
              + "AND ds.clear = false "
              + "ORDER BY ds.time_snapshot desc limit 1",
      nativeQuery = true)
  DamTomSnapshotEntity findLastestDamTomSnapshot(@Param("tenantId") UUID tenantId);

  DamTomSnapshotEntity findFirstByTenantIdAndClearAndDamtomIdInOrderByTimeSnapshot(
      UUID tenantId, boolean clear, List<UUID> damTomIds);

  // binhdv - query for bao cao canh bao
  @Query(
      "SELECT new org.thingsboard.server.dft.controllers.web.baocaoCanhBao.dto.BcSingleDataDto (c.alarmName, COUNT(c.alarmName)) "
          + " FROM DamTomSnapshotEntity AS c WHERE c.damtomId = :damtomId AND "
          + " c.timeSnapshot >= :startTs AND c.timeSnapshot <= :endTs GROUP BY c.alarmName")
  List<BcSingleDataDto> countCanhBaoByDamtomIdAndAlarmName(
      @Param("damtomId") UUID damtomId, @Param("startTs") long startTs, @Param("endTs") long endTs);

  // binhdv - query for bao cao tong hop
  @Query(
      value =
          "SELECT COUNT(*) FROM damtom_snapshot AS ds "
              + "WHERE ds.damtom_id = :damtomId AND ds.time_snapshot >= :startTs AND ds.time_snapshot <= :endTs",
      nativeQuery = true)
  long countAllByDamtomIdAndTimeRange(
      @Param("damtomId") UUID damtomId, @Param("startTs") long startTs, @Param("endTs") long endTs);

  // binhdv query for bao cao
  @Query(
      value =
          "SELECT COUNT(*) FROM damtom_snapshot AS ds "
              + "WHERE ds.tenant_id = :tenantId AND "
              + " ds.damtom_id IN :activeDamTomIdList AND "
              + " ds.time_snapshot >= :startTs AND ds.time_snapshot <= :endTs",
      nativeQuery = true)
  long countAllByTenantIDAndDamtomIdInAndTimeRange(
      @Param("tenantId") UUID tenantId,
      @Param("activeDamTomIdList") List<UUID> activeDamTomIdList,
      @Param("startTs") long startTs,
      @Param("endTs") long endTs);

  @Query(
      value =
          "select * from damtom_snapshot ds where ds.tenant_id = :tenantId "
              + "AND ds.clear = false AND ds.damtom_id = :damTomId "
              + "ORDER BY ds.time_snapshot desc",
      nativeQuery = true)
  List<DamTomSnapshotEntity> findAllDamTomSnapshotClearFalseByDamTomId(
      @Param("tenantId") UUID tenantId, @Param("damTomId") UUID damTomId);

  List<DamTomSnapshotEntity> findAllByTenantIdAndClearAndDamtomIdInOrderByTimeSnapshot(
      UUID tenantId, boolean clear, List<UUID> damTomIds);

  List<DamTomSnapshotEntity> findAllByTenantIdAndClearAndDamtomIdOrderByTimeSnapshot(
      UUID tenantId, boolean clear, UUID damTomId);
}
