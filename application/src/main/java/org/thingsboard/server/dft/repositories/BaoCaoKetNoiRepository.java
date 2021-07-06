package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos.BaoCaoKetNoi;
import org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos.BaoCaoKetNoiChart;
import org.thingsboard.server.dft.entities.BaoCaoKetNoiEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface BaoCaoKetNoiRepository extends JpaRepository<BaoCaoKetNoiEntity, UUID> {

    @Query("SELECT new org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos.BaoCaoKetNoi (kn.deviceType, COUNT(kn.deviceType)) " +
            " FROM BaoCaoKetNoiEntity AS kn WHERE (:damtomId IS NULL OR kn.damtomId = :damtomId) AND" +
            " kn.tenantId = :tenantId AND kn.deviceType <> 'GATEWAY'  AND kn.deviceType <> 'ORP' AND" +
            " kn.createTime >= :startTs AND kn.createTime <= :endTs GROUP BY kn.deviceType")
    List<BaoCaoKetNoi> countMatKetNoiByDamtomId(
            @Param("damtomId") UUID damtomId,
            @Param("tenantId") UUID tenantId,
            @Param("startTs")long startTs,
            @Param("endTs")long endTs
    );

    @Query("SELECT new org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos.BaoCaoKetNoi (kn.deviceType, COUNT(kn.deviceType)) " +
            " FROM BaoCaoKetNoiEntity AS kn WHERE " +
            " kn.tenantId = :tenantId AND kn.deviceType <> 'GATEWAY' AND kn.deviceType <> 'ORP' AND" +
            " kn.createTime >= :startTs AND kn.createTime <= :endTs GROUP BY kn.deviceType")
    List<BaoCaoKetNoi> countMatKetNoi(
            @Param("tenantId") UUID tenantId,
            @Param("startTs")long startTs,
            @Param("endTs")long endTs
    );


}
