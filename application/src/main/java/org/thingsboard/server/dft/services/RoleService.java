package org.thingsboard.server.dft.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.common.dtos.PageDto;
import org.thingsboard.server.dft.controllers.web.role.dtos.RoleDto;
import org.thingsboard.server.dft.entities.RoleEntity;
import org.thingsboard.server.dft.repositories.RoleRepository;

@Service
@Slf4j
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Page<RoleEntity> findRoleByTenantId(UUID tenantId, String textSearch, Pageable pageable) {
        return roleRepository.findAllByTenantId(tenantId, textSearch, pageable);
    }

    // mobile
    public List<RoleEntity> findAllByTenantId(UUID tenantId){
        return this.roleRepository.findAllByTenantId(tenantId);
    }

    public RoleEntity findById(UUID id){
        return roleRepository.findById(id).orElse(null);
    }

    // create or update
    public RoleEntity save(RoleEntity roleEntity){
        return roleRepository.save(roleEntity);
    }

    public void deleteById(UUID id){
        this.roleRepository.deleteById(id);
    }

    // kiểm tra xem tên tạo mới có trùng không
    public boolean checkCreateDuplicateName(UUID tenantId, String roleName){
        return this.roleRepository.existsByTenantIdAndName(tenantId, roleName);
    }

    //kiểm tra xem tên cập nhật có trùng với các tên khác không
    public boolean checkUpdateDuplicateName(UUID tenantId, UUID currentRoleId, String updateRoleName){
       return this.roleRepository.existsByTenantIdAndIdNotAndNameEquals(tenantId, currentRoleId, updateRoleName);
    }

}
