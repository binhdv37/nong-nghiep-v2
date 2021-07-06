package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dft.entities.GroupRpcEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRpcRepository extends JpaRepository<GroupRpcEntity, UUID> {

  @Modifying
  @Transactional
  void deleteByTenantIdAndId(UUID tenantId, UUID id);

  GroupRpcEntity findByTenantIdAndId(UUID tenantId, UUID id);

  @Query(
      "select gr from GroupRpcEntity gr "
          + "where gr.tenantId = :tenantId and gr.damTomId = :damTomId "
          + "order by gr.createdTime desc ")
  List<GroupRpcEntity> findAllByTenantIdAndDamTomId(
      @Param("tenantId") UUID tenantId, @Param("damTomId") UUID damTomId);

  @Query(
      "select gr from GroupRpcEntity gr where gr.damTomId = :damTomId and LOWER(gr.ten) = LOWER(:ten)")
  GroupRpcEntity findDistinctByTen(
      @Param("damTomId") UUID damTomId, @Param("ten") String groupRpcName);

  @Query(
      "select gr from GroupRpcEntity gr where gr.damTomId = :damTomId and LOWER(gr.ten) = LOWER(:ten) and gr.id <> :id")
  GroupRpcEntity findDistinctByTenAndIdNotLike(
      @Param("damTomId") UUID damTomId, @Param("ten") String ten, @Param("id") UUID id);

  boolean existsGroupRpcEntitiesByDamTomIdAndTen(UUID damTomId, String groupRpcName);

  boolean existsGroupRpcEntitiesByDamTomIdAndTenAndIdNot(UUID damTomId, String ten, UUID id);
}
