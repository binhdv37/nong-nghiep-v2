package org.thingsboard.server.dft.controllers.web.qlcamera;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.CameraLog;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.CameraLogId;
import org.thingsboard.server.common.data.id.DamTomLogId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.qlcamera.dto.DamtomCameraCreateDto;
import org.thingsboard.server.dft.controllers.web.qlcamera.dto.DamtomCameraDto;
import org.thingsboard.server.dft.controllers.web.qlcamera.dto.EditCamErrorDto;
import org.thingsboard.server.dft.entities.DamTomCameraEntity;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.qlcamera.DamtomCameraService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class DamtomCameraController extends BaseController {

    @Autowired
    DamtomCameraService damtomCameraService;

    @Autowired
    DamTomService damTomService;

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_QLCAMERA + "\")")
    @RequestMapping(value = "/cameras", method = RequestMethod.GET)
    @ResponseBody
    public PageData<DamtomCameraDto> getDamtomCameras(
            @RequestParam(name = "damtomId") UUID damtomId, // damtomId buoc phai co
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "") String textSearch,
            @RequestParam(required = false, defaultValue = "name") String sortProperty,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder)
            throws ThingsboardException {
        try {
            UUID tenantId = getCurrentUser().getTenantId().getId();
            Pageable pageable = PageRequest
                    .of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);

            // trim textSearch
            textSearch = textSearch.trim();

            Page<DamtomCameraDto> cameraDtoPage = damtomCameraService.findAllByTenantIdAndDamtomId(tenantId, damtomId, textSearch, pageable)
                    .map(DamtomCameraDto::new);

            PageData<DamtomCameraDto> pageData = new PageData<>(
                    cameraDtoPage.getContent(),
                    cameraDtoPage.getTotalPages(),
                    cameraDtoPage.getTotalElements(),
                    cameraDtoPage.hasNext());

            return checkNotNull(pageData);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // mobile
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_QLCAMERA + "\")")
    @GetMapping("/cameras-mb")
    public List<DamtomCameraDto> getAllForMobile(
            @RequestParam(name = "damtomId", required = true) UUID damtomId
    ) throws ThingsboardException{
        try{
            List<DamTomCameraEntity> damTomCameraEntities =
                    this.damtomCameraService.findAllByTenantIdAndDamtomId(getTenantId().getId(), damtomId);
            List<DamtomCameraDto> damtomCameraDtos = damTomCameraEntities
                    .stream()
                    .map(DamtomCameraDto::new)
                    .collect(Collectors.toList());
            return checkNotNull(damtomCameraDtos);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_QLCAMERA + "\")")
    @GetMapping("/cameras/getAll")
    public List<DamtomCameraDto> getAllCamera(
            @RequestParam(name = "damtomId") UUID damtomId
    ) throws ThingsboardException{
        try{
            List<DamTomCameraEntity> damTomCameraEntities =
                    this.damtomCameraService.findAllOrderByCreatedTimeDesc(getTenantId().getId(), damtomId);
            List<DamtomCameraDto> damtomCameraDtos = damTomCameraEntities
                    .stream()
                    .map(DamtomCameraDto::new)
                    .collect(Collectors.toList());
            return checkNotNull(damtomCameraDtos);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_QLCAMERA + "\")")
    @GetMapping("/cameras/{id}")
    public DamtomCameraDto getDamtomCameraById(@PathVariable(name = "id") UUID id) throws ThingsboardException{
        try{
            DamTomCameraEntity cameraEntity = this.damtomCameraService.findById(id);
            if(cameraEntity == null) return checkNotNull(null);
            return checkNotNull(new DamtomCameraDto(cameraEntity));
        } catch (Exception e){
            throw handleException(e);
        }
    }

    /*
        - required :
            + Trong 1 đầm, k đc có 2 cam trùng tên or mã or url
            + Chỉ có duy nhất 1 cam là main
     */
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_QLCAMERA_CREATE + "\", \"" + PermissionConstants.PAGES_QLCAMERA_EDIT + "\")")
    @PostMapping("/cameras")
    public DamtomCameraDto createOrUpdate(@Valid @RequestBody DamtomCameraCreateDto cameraCreateDto) throws ThingsboardException{
        try{
            if(cameraCreateDto.getId() == null) return checkNotNull(create(cameraCreateDto));
            return checkNotNull(update(cameraCreateDto));
        }
        catch (Exception e){
            // ghi log
            CameraLog cameraLog = new CameraLog(
                    null,
                    getTenantId(),
                    getCurrentUser().getCustomerId(),
                    new DamTomLogId(cameraCreateDto.getDamtomId()),
                    cameraCreateDto.getName(),
                    cameraCreateDto.getCode(),
                    cameraCreateDto.getUrl(),
                    cameraCreateDto.isMain(),
                    cameraCreateDto.getNote()
            );
            logEntityAction(emptyId(EntityType.CAMERA), cameraLog, null, cameraCreateDto.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_QLCAMERA_DELETE + "\")")
    @DeleteMapping("/cameras/{id}")
    public ResponseEntity<Integer> deleteDamtomCameraById(@PathVariable(name = "id")UUID id) throws ThingsboardException{
        try{
            // find camera de ghi log
            DamTomCameraEntity cameraEntity = this.damtomCameraService.findById(id);

            this.damtomCameraService.deleteCamera(id);

            // ghi log
            if(cameraEntity !=  null){
                CameraLog cameraLog = new CameraLog(
                        new CameraLogId(cameraEntity.getId()),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        new DamTomLogId(cameraEntity.getDamtomId()),
                        cameraEntity.getName(),
                        cameraEntity.getCode(),
                        cameraEntity.getUrl(),
                        cameraEntity.isMain(),
                        cameraEntity.getNote()
                );
                logEntityAction(cameraLog.getId(), cameraLog, getCurrentUser().getCustomerId(), ActionType.DELETED, null, id.toString());
            }

            return ResponseEntity.ok(1);
        }
        catch (Exception e){
            // find role de ghi log
            DamTomCameraEntity cameraEntity = damtomCameraService.findById(id);

            // ghi log
            if(cameraEntity != null){
                // tao dto de ghi log
                CameraLog cameraLog = new CameraLog(
                        new CameraLogId(cameraEntity.getId()),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        new DamTomLogId(cameraEntity.getDamtomId()),
                        cameraEntity.getName(),
                        cameraEntity.getCode(),
                        cameraEntity.getUrl(),
                        cameraEntity.isMain(),
                        cameraEntity.getNote()
                );
                logEntityAction(emptyId(EntityType.CAMERA), cameraLog, null, ActionType.DELETED, e, id.toString());
            }

            throw handleException(e);
        }
    }


    // create
    private DamtomCameraDto create(DamtomCameraCreateDto cameraCreateDto) throws ThingsboardException {
        // check xem damtomId có tồn tại không
        DamTomEntity damTomEntity = damTomService.getDamTomById(getTenantId().getId(), cameraCreateDto.getDamtomId());
        if(damTomEntity == null) return null;

        // trim data :
        cameraCreateDto.setCode(cameraCreateDto.getCode().trim());
        cameraCreateDto.setName(cameraCreateDto.getName().trim());
        cameraCreateDto.setUrl(convertUrl(cameraCreateDto.getUrl().trim()));
        cameraCreateDto.setNote(cameraCreateDto.getNote().trim());

        DamTomCameraEntity cameraEntity = new DamTomCameraEntity();

        // check trùng mã, tên, url
        if(this.damtomCameraService.createSameCode(cameraCreateDto.getDamtomId(), cameraCreateDto.getCode())){
            EditCamErrorDto errorDto = new EditCamErrorDto(1, "Trùng mã camera");
            return new DamtomCameraDto(errorDto);
        }

        if(this.damtomCameraService.createSameName(cameraCreateDto.getDamtomId(), cameraCreateDto.getName())){
            EditCamErrorDto errorDto = new EditCamErrorDto(2, "Trùng tên camera");
            return new DamtomCameraDto(errorDto);
        }

        if(this.damtomCameraService.createSameUrl(cameraCreateDto.getDamtomId(), cameraCreateDto.getUrl())){
            EditCamErrorDto errorDto = new EditCamErrorDto(3, "Trùng url camera");
            return new DamtomCameraDto(errorDto);
        }

        // Set dữ liệu
        cameraEntity.setId(UUID.randomUUID());
        cameraEntity.setTenantId(getTenantId().getId());
        cameraEntity.setDamtomId(cameraCreateDto.getDamtomId());
        cameraEntity.setName(cameraCreateDto.getName());
        cameraEntity.setCode(cameraCreateDto.getCode());
        cameraEntity.setUrl(cameraCreateDto.getUrl());
        cameraEntity.setMain(cameraCreateDto.isMain());
        cameraEntity.setNote(cameraCreateDto.getNote());
        cameraEntity.setCreatedBy(getCurrentUser().getId().getId());
        cameraEntity.setCreatedTime(System.currentTimeMillis());

        // main == true => tìm cam có main = true trong db, set = false
        if(cameraEntity.isMain()) {
            DamTomCameraEntity damTomCameraEntity = this.damtomCameraService
                    .findByMainEquals(cameraCreateDto.getDamtomId(), true);
            // if exist => set main = false, save to db
            if(damTomCameraEntity != null) {
                damTomCameraEntity.setMain(false);
                this.damtomCameraService.save(damTomCameraEntity);
            }
        }

        DamTomCameraEntity savedCamera = this.damtomCameraService.save(cameraEntity);

        // ghi log
        CameraLog cameraLog = new CameraLog(
                new CameraLogId(savedCamera.getId()),
                getTenantId(),
                getCurrentUser().getCustomerId(),
                new DamTomLogId(savedCamera.getDamtomId()),
                cameraEntity.getName(),
                cameraEntity.getCode(),
                cameraEntity.getUrl(),
                cameraEntity.isMain(),
                cameraEntity.getNote()
        );
        logEntityAction(cameraLog.getId(), cameraLog, getCurrentUser().getCustomerId(), ActionType.ADDED, null);

        // save :
        return new DamtomCameraDto(savedCamera);
    }


    // update
    private DamtomCameraDto update(DamtomCameraCreateDto cameraCreateDto) throws ThingsboardException {
        // check xem damtomId có tồn tại không
        DamTomEntity damTomEntity = damTomService.getDamTomById(getTenantId().getId(), cameraCreateDto.getDamtomId());
        if(damTomEntity == null) return null;

        // trim data :
        cameraCreateDto.setCode(cameraCreateDto.getCode().trim());
        cameraCreateDto.setName(cameraCreateDto.getName().trim());
        cameraCreateDto.setUrl(convertUrl(cameraCreateDto.getUrl().trim()));
        cameraCreateDto.setNote(cameraCreateDto.getNote().trim());

        // tim trong db :
        DamTomCameraEntity cameraEntity = this.damtomCameraService.findById(cameraCreateDto.getId());
        if(cameraEntity == null) return null;

        // check trùng tên, mã
        if(this.damtomCameraService.updateSameCode(cameraCreateDto.getDamtomId(), cameraCreateDto.getCode(), cameraCreateDto.getId())){
            EditCamErrorDto errorDto = new EditCamErrorDto(1, "Trùng mã camera");
            return new DamtomCameraDto(errorDto);
        }

        if(this.damtomCameraService.updateSameName(cameraCreateDto.getDamtomId(), cameraCreateDto.getName(), cameraCreateDto.getId())){
            EditCamErrorDto errorDto = new EditCamErrorDto(2, "Trùng tên camera");
            return new DamtomCameraDto(errorDto);
        }

        if(this.damtomCameraService.updateSameUrl(cameraCreateDto.getDamtomId(), cameraCreateDto.getUrl(), cameraCreateDto.getId())){
            EditCamErrorDto errorDto = new EditCamErrorDto(3, "Trùng url camera");
            return new DamtomCameraDto(errorDto);
        }

        // update cac truong :
        cameraEntity.setCode(cameraCreateDto.getCode());
        cameraEntity.setName(cameraCreateDto.getName());
        cameraEntity.setUrl(cameraCreateDto.getUrl());
        cameraEntity.setMain(cameraCreateDto.isMain());
        cameraEntity.setNote(cameraCreateDto.getNote());

        // cameraCreateDto.main == true => tìm cam có main = true trong db (except current cam)
        // if exist => set main = false
        if(cameraCreateDto.isMain()){
            DamTomCameraEntity damTomCameraEntity = this.damtomCameraService
                    .findByMainEqualsAndIdIsNot(cameraCreateDto.getDamtomId(), true, cameraCreateDto.getId());
            if(damTomCameraEntity != null){
                damTomCameraEntity.setMain(false);
                this.damtomCameraService.save(damTomCameraEntity);
            }
        }

        DamTomCameraEntity savedCamera = this.damtomCameraService.save(cameraEntity);

        // ghi log
        CameraLog cameraLog = new CameraLog(
                new CameraLogId(savedCamera.getId()),
                getTenantId(),
                getCurrentUser().getCustomerId(),
                new DamTomLogId(savedCamera.getDamtomId()),
                cameraEntity.getName(),
                cameraEntity.getCode(),
                cameraEntity.getUrl(),
                cameraEntity.isMain(),
                cameraEntity.getNote()
        );
        logEntityAction(cameraLog.getId(), cameraLog, getCurrentUser().getCustomerId(), ActionType.UPDATED, null);

        // save
        return new DamtomCameraDto(savedCamera);
    }

    // convert url - add http, https
    private String convertUrl(String url){
        if(!url.startsWith("http://") && !url.startsWith("https://")){
            return "http://" + url;
        }
        return url;
    }


}
