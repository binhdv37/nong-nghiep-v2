package org.thingsboard.server.dft.services.baocaoCanhBao;

import org.thingsboard.server.dft.controllers.web.baocaoCanhBao.dto.BcSingleDataDto;

import java.util.List;
import java.util.UUID;

public interface BcCanhBaoService {
    List<BcSingleDataDto> getBcCanhBaoDataByDamtomId(UUID damtomId, long startTs, long endTs);

    long countDamtomCanhBao(UUID damtomId, long startTs, long endTs);

    String getMailContentData(UUID tenantId, UUID damtomId, long startTs, long endTs);

}
