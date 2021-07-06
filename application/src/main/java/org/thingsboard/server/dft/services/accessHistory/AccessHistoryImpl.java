package org.thingsboard.server.dft.services.accessHistory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.audit.AuditLog;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.TimePageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.AuditLogEntity;
import org.thingsboard.server.dft.repositories.AccessHistoryRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.thingsboard.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class AccessHistoryImpl implements AccessHistoryService {

    private static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";

    private final AccessHistoryRepository accessHistoryRepository;

    @Autowired
    public AccessHistoryImpl(AccessHistoryRepository accessHistoryRepository) {
        this.accessHistoryRepository = accessHistoryRepository;
    }

    @Override
    public AuditLogEntity findAuditLogEntityByTenantIdAndUserIdAndId(UUID tenantId, UUID userId, UUID id) {
        return accessHistoryRepository.findAuditLogEntityByTenantIdAndUserIdAndId(tenantId, userId, id);
    }

    @Override
    public PageData<AuditLog> findAuditLogsByTenantId(TenantId tenantId, List<EntityType> entityType, TimePageLink pageLink) {
        log.trace("Executing findAuditLogs [{}]", pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return findAuditLogsByTenantId(tenantId.getId(), entityType, pageLink);
    }

    @Override
    public PageData<AuditLog> findAuditLogsByTenantId(UUID tenantId, List<EntityType> entityType, TimePageLink pageLink) {
        return DaoUtil.toPageData(
                accessHistoryRepository.findByTenantId(
                        tenantId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getStartTime(),
                        pageLink.getEndTime(),
                        entityType,
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public boolean isUserLoggedInBefore(UUID tenantId, String username) {
        return accessHistoryRepository.existsByTenantIdAndUserNameAndActionType(tenantId, username, ActionType.LOGIN);
    }
}
