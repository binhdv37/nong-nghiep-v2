package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.DamTomNotifyTokenEntity;

import java.util.UUID;

@Repository
public interface DamTomNotifyTokenRepository extends JpaRepository<DamTomNotifyTokenEntity, UUID> {
    DamTomNotifyTokenEntity findFirstByUserId(UUID userId);
    DamTomNotifyTokenEntity findFirstByNotifyToken(String token);

}
