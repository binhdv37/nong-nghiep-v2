package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.UserAvatarEntity;

import java.util.UUID;

@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatarEntity, UUID> {

    UserAvatarEntity findUserAvatarEntityByUserId(UUID userId);
}
