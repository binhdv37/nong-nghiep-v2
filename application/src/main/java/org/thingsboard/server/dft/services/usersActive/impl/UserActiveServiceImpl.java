package org.thingsboard.server.dft.services.usersActive.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.entities.UserActiveEntity;
import org.thingsboard.server.dft.repositories.UserActiveRepository;
import org.thingsboard.server.dft.services.usersActive.UserActiveService;

import java.util.Random;
import java.util.UUID;

@Service
public class UserActiveServiceImpl implements UserActiveService {

    @Autowired
    UserActiveRepository userActiveRepository;

    public boolean checkUserIsActive(UUID tenantId, UUID userId) {
        UserActiveEntity userActiveEntity = userActiveRepository.findByTenantIdAndUserId(tenantId, userId);
        if(userActiveEntity == null) return false;
        return userActiveEntity.isActive();
    }

    @Override
    public UserActiveEntity findByTenantIdAndUserId(UUID tenantId, UUID userId) {
        return userActiveRepository.findByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public UserActiveEntity save(UserActiveEntity userActiveEntity) {
        return userActiveRepository.save(userActiveEntity);
    }

    @Override
    public void deleteByTenantIdAndUserId(UUID tenantId, UUID userId) {
        userActiveRepository.deleteByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public String genRandomActivateCode() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 6;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

}
