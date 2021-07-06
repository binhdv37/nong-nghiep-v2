package org.thingsboard.server.dft.services.baocaoThongBao;

import org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto.SeriesDto;
import org.thingsboard.server.dft.entities.NotificationLogEntity;

import java.util.List;
import java.util.UUID;

public interface BcThongBaoService {
    List<NotificationLogEntity> getNotificationByCreatedTimeRange(UUID tenantId, long startTs, long endTs);

    List<SeriesDto> getDataToArraySeriesDto(int type, List<NotificationLogEntity> notificationLogEntities);

    String getMailContentData(UUID tenantId, long startTs, long endTs);
}
