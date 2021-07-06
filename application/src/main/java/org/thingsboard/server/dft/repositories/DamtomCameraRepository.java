package org.thingsboard.server.dft.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.dft.entities.DamTomCameraEntity;

import java.util.List;
import java.util.UUID;

public interface DamtomCameraRepository extends JpaRepository<DamTomCameraEntity, UUID> {
    @Query("SELECT u FROM DamTomCameraEntity u WHERE u.tenantId = :tenantId " +
            "AND u.damtomId = :damtomId " +
            "AND LOWER(u.name) LIKE LOWER(CONCAT('%',:searchText, '%'))")
    Page<DamTomCameraEntity> findAllByTenantIdAndDamtomId(
            @Param("tenantId") UUID tenantId,
            @Param("damtomId") UUID damtomId,
            @Param("searchText") String searchText,
            Pageable pageable);

    // mobile
    List<DamTomCameraEntity> findAllByTenantIdAndDamtomIdOrderByCreatedTimeAsc(UUID tenantId, UUID damtomId);

    List<DamTomCameraEntity> findALlByTenantIdAndDamtomIdOrderByCreatedTimeDesc(UUID tenantId, UUID damtomId);

    // create check trùng mã
    boolean existsByDamtomIdEqualsAndCodeEquals(UUID damtomId, String code);

    // create check trùng tên
    boolean existsByDamtomIdEqualsAndNameEquals(UUID damtomID, String name);

    // create check trùng url
    boolean existsByDamtomIdEqualsAndUrlEquals(UUID damtomId, String url);

    // update check trùng mã
    boolean existsByDamtomIdEqualsAndCodeEqualsAndIdIsNot(UUID damtomId, String code, UUID damtomCameraId);

    // update check trùng tên
    boolean existsByDamtomIdEqualsAndNameEqualsAndIdIsNot(UUID damtomId, String name, UUID damtomCameraId);

    // update check trùng url
    boolean existsByDamtomIdEqualsAndUrlEqualsAndIdIsNot(UUID damtomId, String url, UUID damtomCameraId);

    // create : find cam with main == true
    DamTomCameraEntity findByDamtomIdAndMainEquals(UUID damtomId, boolean main);

    // update : find cam with main == true, id !=
    DamTomCameraEntity findByDamtomIdAndMainEqualsAndIdIsNot(UUID damtomId, boolean main, UUID damtomCameraId);

}
