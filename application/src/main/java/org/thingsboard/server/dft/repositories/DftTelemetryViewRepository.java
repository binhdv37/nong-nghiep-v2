package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.GroupRpcEntity;

import java.util.UUID;

@Repository
public interface DftTelemetryViewRepository extends JpaRepository<GroupRpcEntity, UUID> {

  @Query(
      value =
          "SELECT ts FROM ("
              + "SELECT *,LAG(long_value,1) OVER (ORDER BY ts ) NT FROM damtom_ts_kv_mapping as a "
              + "WHERE device_id = :deviceId "
              + "and key = :key order by ts desc) b "
              + "WHERE b.long_value = :status AND NT <> :status limit 1",
      nativeQuery = true)
  Long getFirstTimeChangeLongStatus(
      @Param("deviceId") UUID deviceId, @Param("key") String key, @Param("status") long status);

  @Query(
      value =
          "SELECT ts FROM ("
              + "SELECT *,LAG(double_value,1) OVER (ORDER BY ts ) NT FROM damtom_ts_kv_mapping as a "
              + "WHERE device_id = :deviceId "
              + "and key = :key order by ts desc) b "
              + "WHERE b.double_value = :status AND NT <> :status limit 1",
      nativeQuery = true)
  Long getFirstTimeChangeDoubleStatus(
      @Param("deviceId") UUID deviceId, @Param("key") String key, @Param("status") double status);
}
