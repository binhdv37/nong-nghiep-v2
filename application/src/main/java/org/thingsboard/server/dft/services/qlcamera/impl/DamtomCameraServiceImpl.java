package org.thingsboard.server.dft.services.qlcamera.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.entities.DamTomCameraEntity;
import org.thingsboard.server.dft.repositories.DamtomCameraRepository;
import org.thingsboard.server.dft.services.qlcamera.DamtomCameraService;

import java.util.List;
import java.util.UUID;

@Service
public class DamtomCameraServiceImpl implements DamtomCameraService {

    @Autowired
    DamtomCameraRepository damtomCameraRepository;


    @Override
    public Page<DamTomCameraEntity> findAllByTenantIdAndDamtomId(UUID tenantId, UUID damtomId, String textSearch, Pageable pageable) {
        return this.damtomCameraRepository.findAllByTenantIdAndDamtomId(tenantId, damtomId, textSearch, pageable);
    }

    @Override
    public List<DamTomCameraEntity> findAllByTenantIdAndDamtomId(UUID tenantId, UUID damtomId) {
        // sap xep theo thoi gian tao tu cu den moi - for mobile
        return this.damtomCameraRepository.findAllByTenantIdAndDamtomIdOrderByCreatedTimeAsc(tenantId, damtomId);
    }

    @Override
    public List<DamTomCameraEntity> findAllOrderByCreatedTimeDesc(UUID tenantId, UUID damtomId) {
        return this.damtomCameraRepository.findALlByTenantIdAndDamtomIdOrderByCreatedTimeDesc(tenantId, damtomId);
    }

    @Override
    public DamTomCameraEntity findById(UUID id) {
        return this.damtomCameraRepository.findById(id).orElse(null);
    }

    @Override
    public DamTomCameraEntity save(DamTomCameraEntity cameraEntity) {
        DamTomCameraEntity entity = this.damtomCameraRepository.save(cameraEntity);
        return entity;
    }

    @Override
    public void deleteCamera(UUID id) {
        this.damtomCameraRepository.deleteById(id);
    }


    // check trùng tên, mã
    @Override
    public boolean createSameCode(UUID damtomId, String code) {
        return this.damtomCameraRepository.existsByDamtomIdEqualsAndCodeEquals(damtomId, code);
    }

    @Override
    public boolean createSameName(UUID damtomId, String name) {
        return this.damtomCameraRepository.existsByDamtomIdEqualsAndNameEquals(damtomId, name);
    }

    @Override
    public boolean createSameUrl(UUID damtomId, String url) {
        return this.damtomCameraRepository.existsByDamtomIdEqualsAndUrlEquals(damtomId, url);
    }

    @Override
    public boolean updateSameCode(UUID damtomId, String code, UUID damtomCameraId) {
        return this.damtomCameraRepository
                .existsByDamtomIdEqualsAndCodeEqualsAndIdIsNot(damtomId, code, damtomCameraId);
    }

    @Override
    public boolean updateSameName(UUID damtomId, String name, UUID damtomCameraId) {
        return this.damtomCameraRepository
                .existsByDamtomIdEqualsAndNameEqualsAndIdIsNot(damtomId, name, damtomCameraId);
    }

    @Override
    public boolean updateSameUrl(UUID damtomId, String url, UUID damtomCameraId) {
        return this.damtomCameraRepository
                .existsByDamtomIdEqualsAndUrlEqualsAndIdIsNot(damtomId, url, damtomCameraId);
    }

    @Override
    public DamTomCameraEntity findByMainEquals(UUID damtomId, boolean main) {
        return this.damtomCameraRepository.findByDamtomIdAndMainEquals(damtomId, main);
    }

    @Override
    public DamTomCameraEntity findByMainEqualsAndIdIsNot(UUID damtomId, boolean main, UUID damtomCameraId) {
        return this.damtomCameraRepository
                .findByDamtomIdAndMainEqualsAndIdIsNot(damtomId, main, damtomCameraId);
    }
}
