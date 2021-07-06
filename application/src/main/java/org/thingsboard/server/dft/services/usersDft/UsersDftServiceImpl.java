package org.thingsboard.server.dft.services.usersDft;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.CustomerEntity;
import org.thingsboard.server.dao.model.sql.UserCredentialsEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.sql.user.UserCredentialsRepository;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.dft.controllers.web.users.dtos.UsersDto;
import org.thingsboard.server.dft.repositories.DftCustomerRepository;
import org.thingsboard.server.dft.repositories.UsersRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.thingsboard.server.dao.service.Validator.validateId;
import static org.thingsboard.server.dao.service.Validator.validatePageLink;

@Service
@Slf4j
public class UsersDftServiceImpl implements UsersDftService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String CREATE = "create";
    public static final String EDIT = "edit";
    public static final String EMAIL = "email";
    public static final String LAST_NAME = "lastName";

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UsersRepository usersRepository;
    private final DftCustomerRepository dftCustomerRepository;
    private final UserCredentialsRepository userCredentialsRepository;

    public UsersDftServiceImpl(PasswordEncoder passwordEncoder, UserService userService, UsersRepository usersRepository, DftCustomerRepository dftCustomerRepository, UserCredentialsRepository userCredentialsRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.usersRepository = usersRepository;
        this.dftCustomerRepository = dftCustomerRepository;
        this.userCredentialsRepository = userCredentialsRepository;
    }

    @Override
    public PageData<User> findUsersByTenantId(TenantId tenantId, PageLink pageLink) {
        log.trace("Executing findUsersByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink);
        return findByTenantId(tenantId.getId(), pageLink);
    }

    @Override
    public PageData<User> findByTenantId(UUID tenantId, PageLink pageLink) {
        return DaoUtil.toPageData(
                usersRepository
                        .findByTenantId(
                                tenantId,
                                Objects.toString(pageLink.getTextSearch(), ""),
                                DaoUtil.toPageable(pageLink)));
    }

    // mobile
    @Override
    public List<UserEntity> findUsersByTenantId(UUID tenantId) {
        return this.usersRepository.findByTenantId(tenantId);
    }

    @Override
    public Page<UserEntity> findUsersByTenantId(UUID tenantId, String searchText, Pageable pageable) {
        return usersRepository.findByTenantId(tenantId, searchText, pageable);
    }

    @Override
    public User convertUserDtoToUser(UsersDto usersDto, TenantId tenantId) {
        User newUser = new User();
        newUser.setTenantId(tenantId);
        CustomerEntity defaultCustomer = dftCustomerRepository.findDistinctFirstByTenantId(tenantId.getId());
        newUser.setCustomerId(new CustomerId(defaultCustomer.getId()));
        newUser.setAuthority(Authority.CUSTOMER_USER);
        newUser.setFirstName(usersDto.getFirstName());
        newUser.setEmail(usersDto.getEmail());
        newUser.setLastName(String.valueOf(usersDto.getLastName()));
        newUser.setAdditionalInfo(usersDto.getAdditionalInfo());

        if (usersDto.getId() != null) {
            newUser.setId(usersDto.getId());
            newUser.setCreatedTime(usersDto.getCreatedTime());
            User foundUser = userService.findUserById(tenantId, usersDto.getId());
            newUser.setEmail(foundUser.getEmail());
        }

        return newUser;
    }

    @Override
    public Customer convertUserDtoToCustomer(UsersDto usersDto, TenantId tenantId) {
        Customer customer = new Customer();

        // customer title must be unique
        customer.setTitle(DigestUtils.md5Hex(usersDto.getFirstName() + usersDto.getEmail()));
        customer.setPhone(usersDto.getLastName());
        customer.setTenantId(tenantId);

        return customer;
    }

    @Override
    public UserCredentials savePasswordUsers(User savedUser, String password, boolean isActive) {
        UserCredentials savedUserCredentials = new UserCredentials();
        UserCredentials newUserCredentials;
        String passwordEncode;
        try {
            if (savedUser != null) {
                passwordEncode = passwordEncoder.encode(password);
                newUserCredentials = userService.findUserCredentialsByUserId(savedUser.getTenantId(), savedUser.getId());
                newUserCredentials.setActivateToken(null);
                if (isActive != newUserCredentials.isEnabled() || !passwordEncode.equals(newUserCredentials.getPassword())) {
                    newUserCredentials.setEnabled(isActive);
                    newUserCredentials.setPassword(passwordEncode);
                    savedUserCredentials = userService.saveUserCredentials(savedUser.getTenantId(), newUserCredentials);
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return savedUserCredentials;
    }

    @Override
    public UserEntity findAllByEmail(String email) {
        return usersRepository.findAllByEmail(email);
    }

    @Override
    public UserEntity findAllByLastName(String lastName) {
        return usersRepository.findAllByLastName(lastName);
    }

    @Override
    public UserEntity findAllByEmailAndIdNotEqual(String email, UUID id) {
        return usersRepository.findAllByEmailAndIdNotEqual(email, id);
    }

    @Override
    public UserEntity findAllByLastNameAndIdNotEqual(String lastName, UUID id) {
        return usersRepository.findAllByLastNameAndIdNotEqual(lastName, id);
    }

    @Override
    public boolean validateUsers(String field, String value, UsersDto usersDto) {
        switch (field) {
            case EMAIL:
                return findAllByEmail(value) != null;
            case LAST_NAME:
                return findAllByLastName(value) != null;
            default:
                return false;
        }
    }

    @Override
    public UserEntity findById(UUID id) {
        return this.usersRepository.findById(id).get();
    }

    @Override
    public boolean isPhoneAlreadyExist(String phoneNumber) {
        return usersRepository.existsByLastName(phoneNumber);
    }

    @Override
    public boolean updateDuplicatePhone(UUID userId, String phoneNumber) {
        return usersRepository.existsByIdIsNotAndLastNameEquals(userId, phoneNumber);
    }

    @Override
    public UserCredentialsEntity findUserCredentialsByUserId(UUID userId) {
        return userCredentialsRepository.findByUserId(userId);
    }
    @Override
    public UserEntity findByTenantIdAndAuthority(UUID userId, Authority value) {
        List<UserEntity> list = usersRepository.findByTenantIdAndAuthority(userId, value);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
