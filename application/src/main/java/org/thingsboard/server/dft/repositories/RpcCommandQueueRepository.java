package org.thingsboard.server.dft.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.RpcCommandQueueEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface RpcCommandQueueRepository extends JpaRepository<RpcCommandQueueEntity, UUID> {

  @Query(
      "select rcq from RpcCommandQueueEntity rcq where "
          + "rcq.commandStatus = :commandStatus AND rcq.commandTime <= :commandTime")
  List<RpcCommandQueueEntity> findAllByCommandStatusAndCommandTime(
      @Param("commandStatus") String commandStatus, @Param("commandTime") long commandTime);

  @Query(
      "select rcq from RpcCommandQueueEntity rcq where "
          + "rcq.commandStatus = :commandStatus "
          + "AND (:startTime IS NULL OR rcq.commandTime >= :startTime) "
          + "AND (:endTime IS NULL OR rcq.commandTime <= :endTime) "
          + "AND rcq.damTomId = :damTomId AND rcq.tenantId = :tenantId")
  Page<RpcCommandQueueEntity> findAllByStatus(
      Pageable pageable,
      @Param("tenantId") UUID tenantId,
      @Param("damTomId") UUID damTomId,
      @Param("startTime") Long startTime,
      @Param("endTime") Long endTime,
      @Param("commandStatus") String commandStatus);

  @Query(
          "select rcq from RpcCommandQueueEntity rcq where "
                  + "rcq.commandStatus = :commandStatus "
                  + "AND (:startTime IS NULL OR rcq.commandTime >= :startTime) "
                  + "AND (:endTime IS NULL OR rcq.commandTime <= :endTime) "
                  + "AND rcq.damTomId = :damTomId AND rcq.tenantId = :tenantId "
                  + "AND rcq.deviceId = :deviceId")
  Page<RpcCommandQueueEntity> findAllByStatusAndDeviceId(
          Pageable pageable,
          @Param("tenantId") UUID tenantId,
          @Param("damTomId") UUID damTomId,
          @Param("startTime") Long startTime,
          @Param("endTime") Long endTime,
          @Param("commandStatus") String commandStatus,
          @Param("deviceId") UUID deviceId);

  int countByTenantIdAndDamTomIdAndCommandStatusAndViewed(
      UUID tenantId, UUID damTomId, String commandStatus, boolean viewed);

  List<RpcCommandQueueEntity> findAllByTenantIdAndGroupRpcIdAndCommandStatus(
      UUID tenantId, UUID damTomId, String commandStatus);

  @Query(
      value =
          "SELECT * from damtom_rpc_command_queue rqc where rqc.tenant_id = :tenantId "
              + "and rqc.group_rpc_id = :groupRpcId and rqc.command_status = :commandStatus order by rqc.command_time desc limit 1",
      nativeQuery = true)
  RpcCommandQueueEntity findLastestCommandByGroupRpcAndStatus(
      @Param("tenantId") UUID tenantId,
      @Param("groupRpcId") UUID groupRpcId,
      @Param("commandStatus") String commandStatus);

  boolean existsByTenantIdAndDeviceIdAndRpcSettingIdAndCommandStatusIn(
      UUID tenantId, UUID deviceId, UUID rpcSettingId, List<String> commandStatusList);
}
