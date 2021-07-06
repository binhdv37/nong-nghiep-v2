package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.sql.UserEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DftUserRepository extends JpaRepository<UserEntity, UUID> {

    @Query("SELECT u FROM UserEntity u where u.tenantId = :tenantId")
    List<UserEntity> findAllByTenantId(@Param("tenantId") UUID tenantId);

    UserEntity findDistinctFirstByEmail(String email);

    @Query("SELECT u FROM UserEntity u where u.tenantId = :tenantId and LOWER(u.email) NOT LIKE LOWER(:email)")
    List<UserEntity> findUserEntitiesByTenantIdAndEmailNotLike(@Param("tenantId") UUID tenantId,@Param("email") String email);

    UserEntity findDistinctFirstByLastName(String phone);

    @Query("SELECT u FROM UserEntity u where u.tenantId <> :tenantId and u.lastName = :phone")
    List<UserEntity> findUserEntitiesByTenantIdNotLikeAndLastName(@Param("tenantId") UUID tenantId,@Param("phone") String phone);

}
