package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thingsboard.server.dao.model.sql.CustomerEntity;

import java.util.UUID;

public interface DftCustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    CustomerEntity findDistinctFirstByTenantId(UUID tenantId);
}
