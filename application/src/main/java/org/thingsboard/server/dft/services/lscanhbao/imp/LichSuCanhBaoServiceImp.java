package org.thingsboard.server.dft.services.lscanhbao.imp;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.model.sql.AssetEntity;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dft.controllers.web.lscanhbao.dtos.DamTomSnapshot;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface LichSuCanhBaoServiceImp {
    AssetEntity getAssetFromGatewayId(UUID gatewayId);

    List<DeviceEntity> getAllGatewayFromDamTom(UUID assetId);

    DeviceProfileEntity getDeviceProfileById(UUID tenantId, UUID id);

    Set<String> getListAlarmKey(DeviceProfileEntity deviceProfileEntity, String alarmType);

    List<TsKvEntry> getAllLatestTsKvInGateway(TenantId tenantId, UUID gatewayId)
            throws ExecutionException, InterruptedException;

    Alarm snapShotCanhBao(Alarm alarm, String type);

    PageData<DamTomSnapshot> getAllDamTomSnap(UUID tenantId, UUID damtomId,
                                              Pageable pageable, Long startTime, Long endTime, String searchText);

    void clearAlarm(UUID snapShotId);
}
