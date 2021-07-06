package org.thingsboard.server.dft.services.dlcambien.imp;

import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dft.controllers.web.dulieucb.dao.BoDuLieuCamBien;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface DuLieuCamBiemServiceImp {

  List<DeviceEntity> getAllGatewayFromDamTom(UUID tenantId, UUID damTomId);

  List<DeviceEntity> getAllGatewayFromAssert(UUID assetId);

  List<DeviceEntity> getAllDeviceFromGateway(UUID deviceId);

  List<String> findAllKeysByEntityIds(TenantId tenantId, List<EntityId> entityIds);

  List<TsKvEntry> finAllTsKvByRangeTime(
          TenantId tenantId, UUID deviceId, List<String> keys, long startTs, long endTs)
          throws ExecutionException, InterruptedException;

  PageData<BoDuLieuCamBien> getBoDuLieuCamBien(
      TenantId tenantId, UUID assetId, long startTime, long endTime, int pageSize, int page, String sort);
}
