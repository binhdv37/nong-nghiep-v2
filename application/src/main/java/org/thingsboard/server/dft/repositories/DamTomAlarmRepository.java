package org.thingsboard.server.dft.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.DamTomAlarmEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DamTomAlarmRepository extends JpaRepository<DamTomAlarmEntity, String> {

    DamTomAlarmEntity findByTenantIdAndAlarmId(UUID tenantId, String alarmId);

    DamTomAlarmEntity findByDamtomIdAndName(UUID damtomId, String name);

    List<DamTomAlarmEntity> findAllByDamtomIdAndTenantId(UUID damTomId,UUID tenantId);


    List<DamTomAlarmEntity> findAllByAlarmIdAndTenantId(String alarmId,UUID tenantId);
}
