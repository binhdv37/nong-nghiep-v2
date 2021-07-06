package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.TenantProfileEntity;

import java.util.UUID;

@Repository
public interface DftTenantProfileRepository extends JpaRepository<TenantProfileEntity, UUID> {

    @Query("SELECT tp from TenantProfileEntity tp where tp.isDefault = true")
    TenantProfileEntity findTenantProfileEntityDefault();
}
