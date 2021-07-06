package org.thingsboard.server.dft.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.dao.model.sql.AuditLogEntity;

import java.util.List;
import java.util.UUID;

public interface AccessHistoryRepository extends JpaRepository<AuditLogEntity, UUID> {
    AuditLogEntity findAuditLogEntityByTenantIdAndUserIdAndId(UUID tenantId, UUID userId, UUID id);

//    @Query("SELECT a FROM AuditLogEntity a WHERE " +
//            "a.tenantId = :tenantId " +
//            "AND (:startTime IS NULL OR a.createdTime >= :startTime) " +
//            "AND (:endTime IS NULL OR a.createdTime <= :endTime) " +
//            "AND ((:entityType) IS NULL OR a.entityType in (:entityType)) " +
//            "AND (LOWER(a.entityType) LIKE LOWER(CONCAT(:textSearch, '%'))" +
//            "OR LOWER(a.entityName) LIKE LOWER(CONCAT(:textSearch, '%'))" +
//            "OR LOWER(a.userName) LIKE LOWER(CONCAT(:textSearch, '%'))" +
//            "OR LOWER(a.actionType) LIKE LOWER(CONCAT(:textSearch, '%'))" +
//            "OR LOWER(a.actionStatus) LIKE LOWER(CONCAT(:textSearch, '%')))"
//    )


    // binh dv - tim kiem theo doi tuong (entityName) va tai khoan tac dong (userName)
        @Query("SELECT a FROM AuditLogEntity a WHERE " +
                "a.tenantId = :tenantId " +
                "AND (:startTime IS NULL OR a.createdTime >= :startTime) " +
                "AND (:endTime IS NULL OR a.createdTime <= :endTime) " +
                "AND ((:entityType) IS NULL OR a.entityType in (:entityType)) " +
                "AND (LOWER(a.entityName) LIKE LOWER(CONCAT('%', :textSearch, '%'))" +
                "OR LOWER(a.userName) LIKE LOWER(CONCAT('%', :textSearch, '%')))"
        )
    Page<AuditLogEntity> findByTenantId(
            @Param("tenantId") UUID tenantId,
            @Param("textSearch") String textSearch,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime,
            @Param("entityType") List<EntityType> entityType,
            Pageable pageable);

    boolean existsByTenantIdAndUserNameAndActionType(UUID tenantId, String username, ActionType actionType);


}
