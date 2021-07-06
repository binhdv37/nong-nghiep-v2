package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.AlarmEntity;

import java.util.UUID;

@Repository
public interface DftAlarmRepository extends JpaRepository<AlarmEntity, UUID> {

}
