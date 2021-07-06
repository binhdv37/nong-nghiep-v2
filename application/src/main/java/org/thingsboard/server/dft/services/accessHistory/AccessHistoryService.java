package org.thingsboard.server.dft.services.accessHistory;

import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.audit.AuditLog;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.TimePageLink;
import org.thingsboard.server.dao.model.sql.AuditLogEntity;

import java.util.List;
import java.util.UUID;

public interface AccessHistoryService {
    AuditLogEntity findAuditLogEntityByTenantIdAndUserIdAndId(UUID tenantId, UUID userId, UUID id);

    PageData<AuditLog> findAuditLogsByTenantId(TenantId tenantId, List<EntityType> entityType, TimePageLink pageLink);

    PageData<AuditLog> findAuditLogsByTenantId(UUID tenantId, List<EntityType> entityType, TimePageLink pageLink);

    // check xem user da tung dang nhap chua
    boolean isUserLoggedInBefore(UUID tenantId, String username);
}
