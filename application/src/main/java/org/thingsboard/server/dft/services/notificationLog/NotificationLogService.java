package org.thingsboard.server.dft.services.notificationLog;

import org.thingsboard.server.dft.entities.NotificationLogEntity;

import java.util.UUID;

public interface NotificationLogService {
    NotificationLogEntity save(NotificationLogEntity notificationLogEntity);

    NotificationLogEntity save(UUID id, UUID tenantId, UUID userId,
                               String email, String phone, int type,
                               int status, long createdTime);

}
