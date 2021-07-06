package org.thingsboard.server.dft.services.usersRole;

import org.thingsboard.server.dft.entities.UserRoleEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface UsersRoleService {

    List<String> findUserPermissionByTenantIdAndUserId(UUID tenantId, UUID userId);

    UserRoleEntity save(UserRoleEntity userRoleEntity);

    List<UserRoleEntity> findAllByTenantIdAndUserId(UUID tenantId, UUID userId);

    ArrayList<UserRoleEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    ArrayList<UserRoleEntity> setAttributesToUserRole(ArrayList<UUID> roleId, long createdTime, UUID createdBy, UUID tenantId, UUID userId);

    void deleteAllByUserId(UUID userId);

    // binhdv - use this function to delete user_role when delete role.
    void deleteAllByTenantIdAndRoleId(UUID tenantId, UUID roleId);
}
