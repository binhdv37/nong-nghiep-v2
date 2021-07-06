package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.RpcSettingEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface RpcSettingRepository extends JpaRepository<RpcSettingEntity, UUID> {

  List<RpcSettingEntity> findByIdIn(List<UUID> ids);
}
