package org.thingsboard.server.dft.services.baoCaoDLGiamSat;

import org.thingsboard.server.common.data.id.TenantId;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface BcDlGiamSatService {
    double getDamtomKeyAvgToDouble(TenantId tenantId, UUID damtomId, String key, long startTs, long endTs)
            throws ExecutionException, InterruptedException;

    double getTenantActiveDamtomKeyAvgToDouble(TenantId tenantId, String key, long startTs, long endTs)
            throws ExecutionException, InterruptedException;

    String getMailContentData(UUID tenantId, long startTime, long endTime) throws ExecutionException, InterruptedException;
}
