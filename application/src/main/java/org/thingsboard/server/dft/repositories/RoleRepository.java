package org.thingsboard.server.dft.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.RoleEntity;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    @Query("SELECT u FROM RoleEntity u WHERE u.tenantId = :tenantId " +
            "AND LOWER(u.name) LIKE LOWER(CONCAT('%',:searchText, '%'))")
    Page<RoleEntity> findAllByTenantId(
            @Param("tenantId") UUID tenantId ,
            @Param("searchText") String searchText,
            Pageable pageable);

    boolean existsByTenantIdAndName(UUID tenantId, String name);

    boolean existsByTenantIdAndIdNotAndNameEquals(UUID tenantId, UUID roleId, String name);

    // mobile
    List<RoleEntity> findAllByTenantId(UUID tenantId);
}
