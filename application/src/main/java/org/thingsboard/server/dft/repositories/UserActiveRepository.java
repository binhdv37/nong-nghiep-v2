package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dft.entities.UserActiveEntity;

import java.util.UUID;

@Repository
public interface UserActiveRepository extends JpaRepository<UserActiveEntity, UUID> {

    UserActiveEntity findByTenantIdAndUserId(UUID tenantId, UUID userId);

    @Modifying
    @Transactional
    void deleteByTenantIdAndUserId(UUID tenantId, UUID userId);
}
