package org.thingsboard.server.dft.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomGatewayEntity;

import java.util.List;
import java.util.UUID;

public interface GateWayRepository extends JpaRepository<DamTomGatewayEntity, UUID> {

    Page<DamTomGatewayEntity> findByDamtomIdAndDevice_NameContaining(UUID damtomId, String searchText, Pageable pageable);

    DamTomGatewayEntity findByTenantIdAndId(UUID tenantId, UUID id);

    List<DamTomGatewayEntity> findByTenantIdAndDamtomId(UUID tenantId, UUID damtomID);



}
