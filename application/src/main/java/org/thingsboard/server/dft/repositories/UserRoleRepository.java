package org.thingsboard.server.dft.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dft.entities.UserRoleEntity;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UUID> {
    List<UserRoleEntity> findAllByTenantIdAndUserId(UUID tenantId, UUID userId);

    ArrayList<UserRoleEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    @Modifying
    @Transactional
    void deleteAllByUserId(UUID userId);


    @Modifying
    @Transactional
    @Query(value = "delete from damtom_user_role as u " +
            "where u.tenant_id = :tenantId and u.role_id = :roleId",
            nativeQuery=true)
    void deleteAllByTenantIdAndRoleId(
            @Param("tenantId") UUID tenantId,
            @Param("roleId") UUID roleId);
}
