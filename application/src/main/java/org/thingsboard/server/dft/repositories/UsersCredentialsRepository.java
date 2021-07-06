package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.UserCredentialsEntity;

import java.util.UUID;

@Repository
public interface UsersCredentialsRepository extends JpaRepository<UserCredentialsEntity, UUID> {

    public UserCredentialsEntity findUserCredentialsByUserId(UUID userId);
}
