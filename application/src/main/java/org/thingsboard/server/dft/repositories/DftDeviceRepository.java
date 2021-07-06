package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.DeviceEntity;

import java.util.UUID;
import java.util.List;

@Repository
public interface DftDeviceRepository extends JpaRepository<DeviceEntity, UUID> {

    DeviceEntity findDeviceEntityById(UUID id);

    DeviceEntity findDeviceEntityByIdAndTenantId(UUID id, UUID tenantId);

    List<DeviceEntity> findAllByIdIn(List<UUID> list);
}

