package org.thingsboard.server.dft.services.baocaoTongHop;

import java.util.List;
import java.util.UUID;

public interface BcTongHopService {

    // tong so canh bao trong 1 khoang thoi gian cua 1 dam tom
    long countDamtomCanhbao(UUID damtomId, long startTs, long endTs);

    long countTenantActiveDamtomCanhBao(UUID tenantId, List<UUID> activeDamtomIdList, long startTs, long endTs);

    String getMailContentData(UUID tenantId, UUID damtomId, long startTs, long endTs);
}
