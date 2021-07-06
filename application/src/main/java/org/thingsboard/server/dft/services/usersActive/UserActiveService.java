package org.thingsboard.server.dft.services.usersActive;

import org.thingsboard.server.dft.entities.UserActiveEntity;

import java.util.UUID;

public interface UserActiveService {
    // check if user account active hay chua - for mobile
    boolean checkUserIsActive(UUID tenantId, UUID userId);

    UserActiveEntity findByTenantIdAndUserId(UUID tenantId, UUID userId);

    UserActiveEntity save(UserActiveEntity userActiveEntity);

    void deleteByTenantIdAndUserId(UUID tenantId, UUID userId);

    // gen random 6 activate code :
    String genRandomActivateCode();
}
