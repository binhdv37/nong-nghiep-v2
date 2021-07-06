package org.thingsboard.server.dft.services.qlcamera;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thingsboard.server.dft.entities.DamTomCameraEntity;

import java.util.List;
import java.util.UUID;

public interface DamtomCameraService {
    Page<DamTomCameraEntity> findAllByTenantIdAndDamtomId(
            UUID tenantId, UUID damtomId,
            String textSearch, Pageable pageable);

    // mobile
    List<DamTomCameraEntity> findAllByTenantIdAndDamtomId(UUID tenantId, UUID damtomId);

    // for danh sach cam -  man hinh ql camera
    List<DamTomCameraEntity> findAllOrderByCreatedTimeDesc(UUID tenantId, UUID damtomId);

    DamTomCameraEntity findById(UUID id);

    DamTomCameraEntity save(DamTomCameraEntity cameraEntity);

    void deleteCamera(UUID id);

    // create check trùng mã
    boolean createSameCode(UUID damtomId, String code);

    // create check trùng tên
    boolean createSameName(UUID damtomId, String name);

    // create check trùng url
    boolean createSameUrl(UUID damtomId, String url);

    // update check trùng mã
    boolean updateSameCode(UUID damtomId, String code, UUID damtomCameraId);

    // update check trùng tên
    boolean updateSameName(UUID damtomId, String name, UUID damtomCameraId);

    // update check trùng url
    boolean updateSameUrl(UUID damtomId, String url, UUID damtomCameraId);

    DamTomCameraEntity findByMainEquals(UUID damtomId, boolean main);

    DamTomCameraEntity findByMainEqualsAndIdIsNot(UUID damtomId, boolean main, UUID damtomCameraId);
}
