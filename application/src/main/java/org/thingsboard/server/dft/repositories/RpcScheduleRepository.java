package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.RpcScheduleEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface RpcScheduleRepository extends JpaRepository<RpcScheduleEntity, UUID> {

    List<RpcScheduleEntity> findAllByActive(boolean active);

    List<RpcScheduleEntity> findAllByTenantIdAndDamTomIdOrderByCreatedTimeDesc(UUID tenantId, UUID damTomId);

    RpcScheduleEntity findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByDamTomIdAndName(UUID damTomId, String name);

    boolean existsByDamTomIdAndNameAndIdNot(UUID damTomId, String name, UUID id);

}
