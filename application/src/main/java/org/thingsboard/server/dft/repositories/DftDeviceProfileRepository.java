package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DftDeviceProfileRepository extends JpaRepository<DeviceProfileEntity, UUID> {

    @Query("select dp from DeviceProfileEntity dp where dp.tenantId = :tenantId and dp.name = :name")
    DeviceProfileEntity getDeviceProfileByName(@Param("tenantId") UUID id, @Param("name") String name);

    DeviceProfileEntity findDeviceProfileEntityByTenantIdAndId(UUID tenantId, UUID id);

    List<DeviceProfileEntity> findAllByIdIn(List<UUID> list);
}
