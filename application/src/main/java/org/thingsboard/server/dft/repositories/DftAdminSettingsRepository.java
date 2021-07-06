package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thingsboard.server.dao.model.sql.AdminSettingsEntity;

import java.util.UUID;

public interface DftAdminSettingsRepository extends JpaRepository<AdminSettingsEntity, UUID> {

    AdminSettingsEntity findAdminSettingsEntityByKey(String key);
}
