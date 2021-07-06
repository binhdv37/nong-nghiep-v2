package org.thingsboard.server.dft.controllers.web.role;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.Role;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.RoleId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.role.dtos.ErrorInfoDto;
import org.thingsboard.server.dft.controllers.web.role.dtos.RoleCreateDto;
import org.thingsboard.server.dft.controllers.web.role.dtos.RoleDto;
import org.thingsboard.server.dft.entities.PermissionEntity;
import org.thingsboard.server.dft.entities.RoleEntity;
import org.thingsboard.server.dft.entities.UserRoleEntity;
import org.thingsboard.server.dft.services.RoleService;
import org.thingsboard.server.dft.services.usersRole.UsersRoleService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class RoleController extends BaseController {

    @Autowired
    protected RoleService roleService;

    @Autowired
    private UsersRoleService usersRoleService;

    @PreAuthorize(
        "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_ROLES + "\")")
    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    @ResponseBody
    public PageData<RoleDto> getTenantRoles(
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

            // trim text search
            textSearch = textSearch.trim();

            Page<RoleDto> roleDtoPage = roleService.findRoleByTenantId(tenantId, textSearch, pageable).map(RoleDto::new);

            PageData<RoleDto> pageData = new PageData<>(
                    roleDtoPage.getContent(),
                    roleDtoPage.getTotalPages(),
                    roleDtoPage.getTotalElements(),
                    roleDtoPage.hasNext());

            return checkNotNull(pageData);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    // mobile
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_ROLES + "\")")
    @GetMapping("/allRoles")
    public List<RoleDto> getAllRoleForMobile() throws ThingsboardException{
        try{
            UUID tenantId = getCurrentUser().getTenantId().getId();
            return this.roleService.findAllByTenantId(tenantId)
                    .stream()
                    .map(RoleDto::new).collect(Collectors.toList());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @GetMapping("/mb-roles/check-name-exist")
    public ResponseEntity<?> mobiCheckRoleNameExist(
            @RequestParam(name = "roleId", required = false)UUID roleId,
            @RequestParam(name = "name")String roleName
    )
            throws ThingsboardException{
        try{

            roleName = roleName.trim();

            // roleId = null -> create role
            if(roleId == null){
                boolean isRoleExist = roleService.checkCreateDuplicateName(getTenantId().getId(), roleName);
                return ResponseEntity.ok(isRoleExist);
            }

            // roleId != null -> update role
            boolean isRoleExist = roleService.checkUpdateDuplicateName(getTenantId().getId(), roleId, roleName);
            return ResponseEntity.ok(isRoleExist);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', \"" + PermissionConstants.PAGES_ROLES + "\")")
    @GetMapping("/roles/{id}")
    public RoleDto getById(@PathVariable(name = "id")UUID id) throws ThingsboardException{
        try{
            RoleEntity entity = roleService.findById(id);
            if(entity == null) return checkNotNull(null);
            return checkNotNull(new RoleDto(entity));
        } catch (Exception e){
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', \"" + PermissionConstants.PAGES_ROLES_EDIT + "\", \"" + PermissionConstants.PAGES_ROLES_CREATE + "\")")
    @PostMapping("/roles")
    public RoleDto createOrUpdate(@Valid @RequestBody RoleCreateDto input) throws ThingsboardException{
        try{
            if(input.getId() == null) return checkNotNull(create(input));
            return checkNotNull(update(input));
        } catch (Exception e){

            // tao dto de ghi log
            Role role = new Role(
                    null,
                    getTenantId(),
                    getCurrentUser().getCustomerId(),
                    input.getName(),
                    input.getNote()
            );

            // ghi log
            logEntityAction(emptyId(EntityType.ROLE), role, null,input.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);

            throw handleException(e);
        }
    }

    /*
        - return 0 : role của user hiện tại, k dc xóa
        - return 1 : xóa thành công
     */
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN', \"" + PermissionConstants.PAGES_ROLES_DELETE + "\")")
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Integer> deleteById(@PathVariable(name = "id")UUID id) throws ThingsboardException{
        try{
            // không cho phép xóa role của user hiện tại.
            // get tất cả các role của user hiện tại
            UUID currentTenantId = getCurrentUser().getTenantId().getId();
            UUID currentUserId = getCurrentUser().getId().getId();
            List<UserRoleEntity> list = this.usersRoleService.findAllByTenantIdAndUserId(currentTenantId, currentUserId);
            // loop through, check xem id gửi lên có nằm trong đống id trong list hay k
            for(UserRoleEntity x : list){
                if(x.getRole().getId().equals(id)) return ResponseEntity.ok(0);
            }

            // find role de ghi log
            RoleEntity roleEntity = roleService.findById(id);

            // delete role
            roleService.deleteById(id);
            // delete user role
            usersRoleService.deleteAllByTenantIdAndRoleId(getTenantId().getId(), id);

            if(roleEntity != null){
                // tao dto de ghi log
                Role role = new Role(
                        new RoleId(id),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        roleEntity.getName(),
                        roleEntity.getNote()
                );
                // ghi log
                logEntityAction(role.getId(), role, getCurrentUser().getCustomerId(), ActionType.DELETED, null, id.toString());
            }

            return ResponseEntity.ok(1);
        } catch (Exception e){
            // find role de ghi log
            RoleEntity roleEntity = roleService.findById(id);

            if(roleEntity != null){
                // tao dto de ghi log
                Role role = new Role(
                        new RoleId(id),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        roleEntity.getName(),
                        roleEntity.getNote()
                );

                // ghi log
                logEntityAction(emptyId(EntityType.ROLE), role, null, ActionType.DELETED, e, id.toString());
            }

            throw handleException(e);
        }
    }

    private RoleDto create(RoleCreateDto input) throws ThingsboardException {
        // loai bo khoang trang input
        input.setName(input.getName().trim());
        input.setNote(input.getNote().trim());

        // check trung ten
        if(this.roleService.checkCreateDuplicateName( getTenantId().getId(), input.getName())) {
            RoleDto roleDto = new RoleDto();
            ErrorInfoDto errorInfo = new ErrorInfoDto();
            errorInfo.setCode(2);
            errorInfo.setMessage("Tên vai trò đã tồn tại!");
            roleDto.setErrorInfo(errorInfo);
            return roleDto;
        }

        // them 1 so truong cho entity, truyen xuong service de luu vao db
        RoleEntity entity = new RoleEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(getCurrentUser().getTenantId().getId());
        entity.setName(input.getName());
        entity.setNote(input.getNote());
        entity.setCreatedTime(System.currentTimeMillis());
        entity.setCreatedBy(getCurrentUser().getId().getId());
        entity.setPermissions(
                input.getPermissions()
                .stream()
                .map( p -> {
                    PermissionEntity pEntity = new PermissionEntity();
                    pEntity.setId(UUID.randomUUID());
                    pEntity.setTenantId(entity.getTenantId());
                    pEntity.setRoleId(entity.getId());
                    pEntity.setName(p.getName().trim());
                    pEntity.setCreatedBy(entity.getCreatedBy());
                    pEntity.setCreatedTime(entity.getCreatedTime());
                    return pEntity;
                }).collect(Collectors.toList())
        );

        // tao dto de ghi log
        Role role = new Role(
                new RoleId(entity.getId()),
                getTenantId(),
                getCurrentUser().getCustomerId(),
                entity.getName(),
                entity.getNote()
        );

        // ghi log
        logEntityAction(role.getId(), role, getCurrentUser().getCustomerId(), ActionType.ADDED, null);

        // truyen xuong service, luu vao db
        return new RoleDto(roleService.save(entity));
    }

    private RoleDto update(RoleCreateDto input) throws ThingsboardException {
        // loai bo khoang trang input
        input.setName(input.getName().trim());
        input.setNote(input.getNote().trim());

        // tim trong db
        RoleEntity entity = roleService.findById(input.getId());
        if(entity == null) return null;

        // check trung ten :
        if(roleService.checkUpdateDuplicateName(getTenantId().getId(), entity.getId(), input.getName())) {
            RoleDto roleDto = new RoleDto();
            ErrorInfoDto errorInfo = new ErrorInfoDto();
            errorInfo.setCode(2);
            errorInfo.setMessage("Tên vai trò đã tồn tại!");
            roleDto.setErrorInfo(errorInfo);
            return roleDto;
        }

        // update cac truong
        entity.setName(input.getName());
        entity.setNote(input.getNote());
        entity.setPermissions(
                input.getPermissions()
                .stream()
                .map(p -> {
                    PermissionEntity pEntity = new PermissionEntity();
                    pEntity.setId(UUID.randomUUID());
                    pEntity.setTenantId(entity.getTenantId());
                    pEntity.setRoleId(entity.getId());
                    pEntity.setName(p.getName().trim());
                    pEntity.setCreatedTime(System.currentTimeMillis());
                    pEntity.setCreatedBy(entity.getCreatedBy());
                    return pEntity;
                }).collect(Collectors.toList())
        );

        // tao dto de ghi log
        Role role = new Role(
                new RoleId(entity.getId()),
                getTenantId(),
                getCurrentUser().getCustomerId(),
                entity.getName(),
                entity.getNote()
        );

        // ghi log
        logEntityAction(role.getId(), role, getCurrentUser().getCustomerId(), ActionType.UPDATED, null);

        // truyen xuong service, luu vao db
        return new RoleDto(roleService.save(entity));
    }


}
