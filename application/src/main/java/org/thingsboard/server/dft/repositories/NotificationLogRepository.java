package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.NotificationLogEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, UUID> {
    @Query(value = "SELECT l FROM NotificationLogEntity l WHERE l.tenantId = :tenantId AND " +
            " l.createdTime >= :startTs AND l.createdTime <= :endTs ")
    List<NotificationLogEntity> findNotificationLogByTenantIdAndTimeRange(
            @Param("tenantId") UUID tenantId,
            @Param("startTs") long startTs,
            @Param("endTs") long endTs);
}
