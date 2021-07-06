package org.thingsboard.server.dft.services.usersDft;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.dao.model.sql.UserCredentialsEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.controllers.web.users.dtos.UsersDto;

import java.util.List;
import java.util.UUID;

public interface UsersDftService {

    PageData<User> findUsersByTenantId(TenantId tenantId, PageLink pageLink);

    PageData<User> findByTenantId(UUID tenantId, PageLink pageLink);

    //mobile
    List<UserEntity> findUsersByTenantId(UUID tenantId);

    Page<UserEntity> findUsersByTenantId(UUID tenantId, String searchText, Pageable pageable);

    User convertUserDtoToUser(UsersDto usersDto, TenantId tenantId);

    Customer convertUserDtoToCustomer(UsersDto usersDto, TenantId tenantId);

    UserCredentials savePasswordUsers(User savedUser, String password, boolean isActive);

    UserEntity findAllByEmail(String email);

    UserEntity findAllByLastName(String lastName);

    UserEntity findAllByEmailAndIdNotEqual(String email, UUID id);

    UserEntity findAllByLastNameAndIdNotEqual(String lastName, UUID id);

    boolean validateUsers(String field, String value, UsersDto usersDto);

    //Tuan viet
    UserEntity findById(UUID id);

    UserEntity findByTenantIdAndAuthority(UUID userId, Authority value);
    // binhdv
    boolean isPhoneAlreadyExist(String phoneNumber);

    // binhdv - check update same phone
    boolean updateDuplicatePhone(UUID userId, String phoneNumber);

    // binhdv
    UserCredentialsEntity findUserCredentialsByUserId(UUID userId);


}
