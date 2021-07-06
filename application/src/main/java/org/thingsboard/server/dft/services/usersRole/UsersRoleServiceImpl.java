package org.thingsboard.server.dft.services.usersRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.entities.PermissionEntity;
import org.thingsboard.server.dft.entities.RoleEntity;
import org.thingsboard.server.dft.entities.UserRoleEntity;
import org.thingsboard.server.dft.repositories.UserRoleRepository;
import org.thingsboard.server.dft.services.RoleService;

@Service
@Slf4j
public class UsersRoleServiceImpl implements UsersRoleService {

    private final UserRoleRepository userRoleRepository;
    private final RoleService roleService;

    @Autowired
    public UsersRoleServiceImpl(UserRoleRepository userRoleRepository, RoleService roleService) {
        this.userRoleRepository = userRoleRepository;
        this.roleService = roleService;
    }

    public List<String> findUserPermissionByTenantIdAndUserId(UUID tenantId, UUID userId) {
        List<UserRoleEntity> userRoleEntities = userRoleRepository
                .findAllByTenantIdAndUserId(tenantId, userId);
        List<String> permissions = new ArrayList<>();

        for (UserRoleEntity userRoleEntity : userRoleEntities) {
            for (PermissionEntity permissionEntity : userRoleEntity.getRole().getPermissions()) {
                permissions.add(permissionEntity.getName());
            }
        }

        return permissions;
    }

    @Override
    public UserRoleEntity save(UserRoleEntity userRoleEntity) {
        return userRoleRepository.save(userRoleEntity);
    }

    @Override
    public List<UserRoleEntity> findAllByTenantIdAndUserId(UUID tenantId, UUID userId) {
        return userRoleRepository.findAllByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public ArrayList<UserRoleEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId) {
        return userRoleRepository.findByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public ArrayList<UserRoleEntity> setAttributesToUserRole(ArrayList<UUID> roleId, long createdTime, UUID createdBy, UUID tenantId, UUID userId) {
        UUID userRoleId;
        List<UserRoleEntity> listCurrentUserRoleEntity;
        RoleEntity roleEntity;
        ArrayList<UserRoleEntity> arrayListUserRole = new ArrayList<>();
        try {
            if (roleId.size() > 0) {
                listCurrentUserRoleEntity = findAllByTenantIdAndUserId(tenantId, userId);
                for (UUID uuid : roleId) {
                    UserRoleEntity newUserRoleEntity = new UserRoleEntity();
                    roleEntity = roleService.findById(uuid);
                    if (listCurrentUserRoleEntity.size() == 0) {
                        userRoleId = UUID.randomUUID();
                        newUserRoleEntity.setId(userRoleId);
                    } else {
                        for (UserRoleEntity userRoleEntity : listCurrentUserRoleEntity) {
                            userRoleRepository.delete(userRoleEntity);
                        }

                        userRoleId = UUID.randomUUID();
                        newUserRoleEntity.setId(userRoleId);
                    }
                    newUserRoleEntity.setRole(roleEntity);
                    newUserRoleEntity.setCreatedTime(createdTime);
                    newUserRoleEntity.setCreatedBy(createdBy);
                    newUserRoleEntity.setTenantId(tenantId);
                    newUserRoleEntity.setUserId(userId);

                    arrayListUserRole.add(newUserRoleEntity);
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return arrayListUserRole;
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        userRoleRepository.deleteAllByUserId(userId);
    }

    @Override
    public void deleteAllByTenantIdAndRoleId(UUID tenantId, UUID roleId) {
        userRoleRepository.deleteAllByTenantIdAndRoleId(tenantId, roleId);
    }

}
