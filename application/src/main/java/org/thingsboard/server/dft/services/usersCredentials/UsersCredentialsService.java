package org.thingsboard.server.dft.services.usersCredentials;

import org.thingsboard.server.dao.model.sql.UserCredentialsEntity;

import java.util.UUID;

public interface UsersCredentialsService {

    public UserCredentialsEntity findUserCredentialsByUserId(UUID userId);
}
