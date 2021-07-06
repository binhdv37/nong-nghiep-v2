package org.thingsboard.server.dft.services.usersCredentials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dao.model.sql.UserCredentialsEntity;
import org.thingsboard.server.dft.repositories.UsersCredentialsRepository;

import java.util.UUID;

@Service
public class UsersCredentialsImpl implements UsersCredentialsService{

    private final UsersCredentialsRepository usersCredentialsRepository;

    @Autowired
    public UsersCredentialsImpl(UsersCredentialsRepository usersCredentialsRepository) {
        this.usersCredentialsRepository = usersCredentialsRepository;
    }

    @Override
    public UserCredentialsEntity findUserCredentialsByUserId(UUID id) {
        return usersCredentialsRepository.findUserCredentialsByUserId(id);
    }
}
