package org.thingsboard.server.dft.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.entities.DamTomStaffEntity;

@Repository
public interface DamTomStaffRepository extends JpaRepository<DamTomStaffEntity, UUID> {

    Page<DamTomStaffEntity> findAllByDamtomId(UUID damtomId, Pageable pageable);

    List<DamTomStaffEntity> findAllByStaffId(UUID staffId);

}
