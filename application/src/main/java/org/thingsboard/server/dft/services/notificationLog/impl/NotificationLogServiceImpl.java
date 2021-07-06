package org.thingsboard.server.dft.services.notificationLog.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.entities.NotificationLogEntity;
import org.thingsboard.server.dft.repositories.NotificationLogRepository;
import org.thingsboard.server.dft.services.notificationLog.NotificationLogService;

import java.util.UUID;

@Service
public class NotificationLogServiceImpl implements NotificationLogService {

    @Autowired
    NotificationLogRepository notificationLogRepository;

    @Override
    public NotificationLogEntity save(NotificationLogEntity notificationLogEntity) {
        return notificationLogRepository.save(notificationLogEntity);
    }

    @Override
    public NotificationLogEntity save(UUID id, UUID tenantId, UUID userId,
                                      String email, String phone, int type, int status, long createdTime) {
        NotificationLogEntity entity = new NotificationLogEntity();
        entity.setId(id);
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setEmail(email);
        entity.setPhone(phone);
        entity.setType(type);
        entity.setStatus(status);
        entity.setCreatedTime(createdTime);

        return notificationLogRepository.save(entity);
    }
}
