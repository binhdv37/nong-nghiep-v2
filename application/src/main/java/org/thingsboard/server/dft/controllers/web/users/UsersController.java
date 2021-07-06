package org.thingsboard.server.dft.controllers.web.users;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.audit.AuditLog;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.page.SortOrder;
import org.thingsboard.server.common.data.page.TimePageLink;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.model.sql.AuditLogEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.common.messageUser.MessageResponseUser;
import org.thingsboard.server.dft.controllers.web.users.dtos.ExcelDto;
import org.thingsboard.server.dft.controllers.web.users.dtos.MbUpdateUserDto;
import org.thingsboard.server.dft.controllers.web.users.dtos.UsersDto;
import org.thingsboard.server.dft.entities.KhachHangEntity;
import org.thingsboard.server.dft.entities.RoleEntity;
import org.thingsboard.server.dft.entities.UserActiveEntity;
import org.thingsboard.server.dft.entities.UserRoleEntity;
import org.thingsboard.server.dft.services.accessHistory.AccessHistoryService;
import org.thingsboard.server.dft.services.excel.ExcelService;
import org.thingsboard.server.dft.services.khachhang.KhachHangService;
import org.thingsboard.server.dft.services.usersActive.UserActiveService;
import org.thingsboard.server.dft.services.usersCredentials.UsersCredentialsService;
import org.thingsboard.server.dft.services.usersDft.UsersDftService;
import org.thingsboard.server.dft.services.usersRole.UsersRoleService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController
@TbCoreComponent
@RequestMapping("/api")
public class UsersController extends BaseController {

    public static final String CREATE = "create";
    public static final String EDIT = "edit";
    public static final String EMAIL = "email";
    public static final String LAST_NAME = "lastName";
    public static final String EMAIL_SUBJECT = "IOT Đầm Tôm - Mã kích hoạt";

    private final UsersRoleService usersRoleService;
    private final UsersCredentialsService usersCredentialsService;
    private final PasswordEncoder passwordEncoder;
    private final ExcelService excelService;
    private final UsersDftService usersDftService;
    private final AccessHistoryService accessHistoryService;
    private final UserActiveService userActiveService;
    private final TenantService tenantService;
    private final KhachHangService khachHangService;

    @Autowired
    public UsersController(UsersRoleService usersRoleService, UsersCredentialsService usersCredentialsService,
                           PasswordEncoder passwordEncoder, ExcelService excelService,
                           UsersDftService usersDftService, AccessHistoryService accessHistoryService,
                           UserActiveService userActiveService, TenantService tenantService,
                           KhachHangService khachHangService) {
        this.usersRoleService = usersRoleService;
        this.usersCredentialsService = usersCredentialsService;
        this.passwordEncoder = passwordEncoder;
        this.excelService = excelService;
        this.usersDftService = usersDftService;
        this.accessHistoryService = accessHistoryService;
        this.userActiveService = userActiveService;
        this.tenantService = tenantService;
        this.khachHangService = khachHangService;
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" +
            PermissionConstants.PAGES_USERS + "\", \"" +
            PermissionConstants.PAGES_DAMTOM_CREATE + "\", \"" +
            PermissionConstants.PAGES_DAMTOM_EDIT + "\")")
    @GetMapping("/list-users")
    @ResponseBody
    public PageData<UsersDto> getAllUsers(
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String sortProperty,
            @RequestParam(required = false) String sortOrder,
            @RequestParam int pageSize,
            @RequestParam int page) throws ThingsboardException {
        PageData<User> pageDataUser;
        PageData<UsersDto> pageDataUserDto;
        PageLink pageLink;
        SecurityUser currentUser;

        // trim search text
        if(textSearch != null) textSearch = textSearch.trim();

        try {
            pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
            currentUser = getCurrentUser();
            pageDataUser = usersDftService.findUsersByTenantId(currentUser.getTenantId(), pageLink);
            pageDataUserDto = new PageData<>(pageDataUser.getData().stream().map(UsersDto::new).collect(Collectors.toList()),
                    pageDataUser.getTotalPages(),
                    pageDataUser.getTotalElements(),
                    pageDataUser.hasNext());
            setRoleAndActiveToUsersDto(pageDataUserDto.getData());
        } catch (Exception ex) {
            throw handleException(ex);
        }
        return checkNotNull(pageDataUserDto);
    }

    // mobile
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_USERS + "\")")
    @GetMapping("/mb-users")
    public List<UsersDto> getAllTenantUsers() throws ThingsboardException {
        try{
            List<UserEntity> userEntities = this.usersDftService.findUsersByTenantId(getTenantId().getId());
            List<UsersDto> usersDtoList = userEntities.stream()
                    .map(UsersDto::new).collect(Collectors.toList());
            setRoleAndActiveToUsersDto(usersDtoList);
            return checkNotNull(usersDtoList);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }

    /*
        - response :
            + 400 bad request (messase) : phone da ton tai
            + 200 (User) : update success
     */
    // mobile - cap nhat thong tin ca nhan của tài khoản hiện tại đang đăng nhập (ten, sdt)
//    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_USERS_EDIT + "\")")
    @PostMapping("/mb-users")
    public ResponseEntity<?> updateCurrentUser(@Valid @RequestBody MbUpdateUserDto userDto) throws ThingsboardException{
        try{
            // check if name and phone is valid
            if(userDto.getFullName().isEmpty()){
                return new ResponseEntity<>("Full name cannot be blank", HttpStatus.BAD_REQUEST);
            }

            if(!isPhoneValid(userDto.getPhoneNumber())){
                return new ResponseEntity<>("Invalid phone number", HttpStatus.BAD_REQUEST);
            }

            // find current user :
            User currentUser = userService.findUserById(getCurrentUser().getTenantId(), getCurrentUser().getId());

            // convert sdt
            userDto.setPhoneNumber(convertPhoneNumber(userDto.getPhoneNumber()));

            // check trung sdt
            if(!userDto.getPhoneNumber().equals("") && !currentUser.getLastName().equals(userDto.getPhoneNumber())){
                if(usersDftService.isPhoneAlreadyExist(userDto.getPhoneNumber())){
                    return new ResponseEntity<>("Phone number already exist", HttpStatus.BAD_REQUEST);
                }
            }

            // save user
            currentUser.setFirstName(userDto.getFullName());
            currentUser.setLastName(userDto.getPhoneNumber());
            User savedUser = userService.saveUser(currentUser);

            // if user is tenant -> update table tenant and khach_hang
            if(currentUser.getAuthority() == Authority.TENANT_ADMIN){
                // find tenant
                Tenant tenant = tenantService.findTenantById(getTenantId());
                tenant.setPhone(userDto.getPhoneNumber());
                tenantService.saveTenant(tenant);

                // find khach hang
                KhachHangEntity khachHangEntity = khachHangService.findByTenantId(getTenantId().getId());
                khachHangEntity.setTenKhachHang(userDto.getFullName());
                khachHangService.updateKhachHang(khachHangEntity);
            }

            // ghi log:
            logEntityAction(savedUser.getId(), savedUser, savedUser.getCustomerId(), ActionType.UPDATED, null);
            return ResponseEntity.ok(currentUser);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }

    /*
        - response :
            - 200 (true) : phone da ton tai
            - 200 (false) : ok
     */
    // mobile - check trung sdt ( màn hình cập nhật thông tin cá nhân mobile)
//    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_USERS + "\")")
    @GetMapping("mb-users/check-phone-exist")
    public ResponseEntity<Boolean> checkDuplicatePhoneNumber(@RequestParam(name = "phoneNumber")String phoneNumber)
    throws ThingsboardException{
        try{
            // find current user :
            User currentUser = userService.findUserById(getCurrentUser().getTenantId(), getCurrentUser().getId());

            // convert phone number
            phoneNumber = convertPhoneNumber(phoneNumber);

            // check trung sdt
            if(!currentUser.getLastName().equals(phoneNumber)){
                if(usersDftService.isPhoneAlreadyExist(phoneNumber)){
                    return ResponseEntity.ok(true);
                }
            }

            return ResponseEntity.ok(false);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }


    // - for mobile : check email exist ( man hinh them moi tai khoan )
    /*
        - 200 : true -> da ton tai
        - 200 : false -> chua ton tai
     */
    // yeu cau : validate trc ben phia frontend
    @GetMapping("/mb-users/ql-user/check-mail-exist")
    public ResponseEntity<?> checkMailExist(@RequestParam String email) throws ThingsboardException{
        try{
            email = email.trim();

            UserEntity userEntity = usersDftService.findAllByEmail(email);
            if(userEntity == null) return ResponseEntity.ok(false);

            return ResponseEntity.ok(true);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }


    // - for mobile : check phone exist ( man hinh them moi, cap nhat tai khoan )
    /*
        - 200 : true -> da ton tai
        - 200 : false -> chua ton tai
     */
    // yeu cau : validate ben frontend trc khi call api
    @GetMapping("/mb-users/ql-user/check-phone-exist")
    public ResponseEntity<?> checkPhoneExist(
            @RequestParam(name = "userId", required = false) UUID userId,
            @RequestParam(name = "phoneNumber") String phoneNumber)
            throws ThingsboardException{
        try{
            // convert phone number
            phoneNumber = convertPhoneNumber(phoneNumber);

            // check tao moi sdt da ton tai :
            if(userId == null) {
                boolean isPhoneExist = usersDftService.isPhoneAlreadyExist(phoneNumber);
                return ResponseEntity.ok(isPhoneExist);
            }

            // userId != null => check update sdt da ton tai:
            boolean updateDuplicatePhone = usersDftService.updateDuplicatePhone(userId, phoneNumber);
            return ResponseEntity.ok(updateDuplicatePhone);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_USERS + "\")")
    @GetMapping("/users/{userId}")
    @ResponseBody
    public UsersDto getUser(@PathVariable("userId") String strUserId) throws ThingsboardException {
        UUID uuid;
        List<UsersDto> usersDtoList;
        UserId userId;
        User currentUser;
        UsersDto currentUsersDto;

        currentUsersDto = new UsersDto();
        currentUsersDto.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        currentUsersDto.setResponseMessage(MessageResponseUser.INTERNAL_001);
        try {
            uuid = UUID.fromString(strUserId);
            usersDtoList = new ArrayList<>();
            userId = new UserId(uuid);
            currentUser = userService.findUserById(getCurrentUser().getTenantId(), userId);
            if (currentUser == null) {
                currentUsersDto.setResponseCode(HttpStatus.BAD_REQUEST.value());
                currentUsersDto.setResponseMessage(MessageResponseUser.BAD_001);
            } else {
                currentUsersDto = new UsersDto(currentUser);
                if (currentUsersDto.getId() != null) {
                    usersDtoList.add(currentUsersDto);
                    setRoleAndActiveToUsersDto(usersDtoList);
                    currentUsersDto.setResponseCode(HttpStatus.OK.value());
                    currentUsersDto.setResponseMessage(MessageResponseUser.OK_001);
                } else {
                    currentUsersDto.setResponseCode(HttpStatus.BAD_REQUEST.value());
                    currentUsersDto.setResponseMessage(MessageResponseUser.BAD_001);
                }
            }
            return checkNotNull(currentUsersDto);
        } catch (Exception ex) {
            currentUsersDto.setResponseCode(HttpStatus.REQUEST_TIMEOUT.value());
            currentUsersDto.setResponseMessage(MessageResponseUser.BAD_001 + ex.getMessage());
            return checkNotNull(currentUsersDto);
        }
    }


    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_USERS_CREATE + "\", \"" + PermissionConstants.PAGES_USERS_EDIT + "\")")
    @PostMapping("/save-users/{action}")
    public UsersDto createUsers(@Valid @RequestBody UsersDto usersDto, @PathVariable("action") String strAction) throws ThingsboardException {
        User newUser = new User();
        User savedUser = new User();
        User currentUser;
        UserCredentials savedUserCredentials;
        UserRoleEntity savedUserRoleEntity = new UserRoleEntity();
        ArrayList<UserRoleEntity> arrayListSavedUserRoleEntity = new ArrayList<>();
        ArrayList<RoleEntity> arrayListSavedRoleEntity = new ArrayList<>();
        boolean validated;
        UsersDto savedUsersDto = new UsersDto();

        // binhdv - convert phone number
        usersDto.setLastName(convertPhoneNumber(usersDto.getLastName()));

        savedUsersDto.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        savedUsersDto.setResponseMessage(MessageResponseUser.INTERNAL_001);
        try {
            // validate email, phone number
            if (usersDto.getId() == null) {
                if (!usersDto.getEmail().equals("")) {
                    validated = usersDftService.validateUsers(EMAIL, usersDto.getEmail(), usersDto);
                    if (validated) {
                        savedUsersDto.setResponseCode(HttpStatus.BAD_REQUEST.value());
                        savedUsersDto.setResponseMessage(MessageResponseUser.BAD_002);
                        return savedUsersDto;
                    }
                }
                if (!usersDto.getLastName().equals("")) {
                    validated = usersDftService.validateUsers(LAST_NAME, usersDto.getLastName(), usersDto);
                    if (validated) {
                        savedUsersDto.setResponseCode(HttpStatus.BAD_REQUEST.value());
                        savedUsersDto.setResponseMessage(MessageResponseUser.BAD_003);
                        return savedUsersDto;
                    }
                }
            } else {
                currentUser = userService.findUserById(usersDto.getTenantId(), usersDto.getId());
                if (!currentUser.getEmail().equals(usersDto.getEmail())) {
                    validated = usersDftService.validateUsers(EMAIL, usersDto.getEmail(), usersDto);
                    if (validated) {
                        savedUsersDto.setResponseCode(HttpStatus.BAD_REQUEST.value());
                        savedUsersDto.setResponseMessage(MessageResponseUser.BAD_002);
                        return savedUsersDto;
                    }
                }
                if (!currentUser.getLastName().equals(usersDto.getLastName())) {
                    validated = usersDftService.validateUsers(LAST_NAME, usersDto.getLastName(), usersDto);
                    if (validated) {
                        savedUsersDto.setResponseCode(HttpStatus.BAD_REQUEST.value());
                        savedUsersDto.setResponseMessage(MessageResponseUser.BAD_003);
                        return savedUsersDto;
                    }
                }
            }
            // / validate email, phone number


            // update tb_user table
            newUser = usersDftService.convertUserDtoToUser(usersDto, getCurrentUser().getTenantId());
            savedUser = userService.saveUser(newUser);
            // / update tb_user table


            // update damtom_user_active
            if(strAction.equals(CREATE)){
                // neu la tao moi => insert vao table damtom_user_role
                UserActiveEntity userActiveEntity = new UserActiveEntity(
                        UUID.randomUUID(),
                        getTenantId().getId(),
                        savedUser.getId().getId(),
                        false,
                        userActiveService.genRandomActivateCode(),
                        System.currentTimeMillis()
                );
                // save
                userActiveService.save(userActiveEntity);
            }
            // / update damtom_user_active


            // update damtom_user_role table
            if (usersDto.getRoleId().size() > 0) {
                ArrayList<UserRoleEntity> arrayListUserRole = usersRoleService.setAttributesToUserRole(
                        usersDto.getRoleId(),
                        savedUser.getCreatedTime(),
                        getCurrentUser().getId().getId(),
                        getCurrentUser().getTenantId().getId(),
                        savedUser.getId().getId());
                for (UserRoleEntity userRole : arrayListUserRole) {
                    savedUserRoleEntity = usersRoleService.save(userRole);
                    arrayListSavedUserRoleEntity.add(savedUserRoleEntity);
                }

                for (UserRoleEntity userRoleEntity : arrayListSavedUserRoleEntity) {
                    arrayListSavedRoleEntity.add(userRoleEntity.getRole());
                }
            } else if (usersDto.getRoleId().size() == 0 && usersDto.getId() != null) {
                usersRoleService.deleteAllByUserId(usersDto.getId().getId());
            }
            // / update damtom_user_role table


            // update user_credential table
            if (strAction.equals(CREATE)) {
                if (checkNotNull(savedUser) != null) {
                    savedUsersDto = new UsersDto(savedUser);
                    if (usersDto.getRoleId() != null) {
                        savedUsersDto.setRoleEntity(arrayListSavedRoleEntity);
                    }
                    if (checkNotNull(savedUsersDto) != null) {
                        savedUserCredentials = usersDftService.savePasswordUsers(savedUser, usersDto.getPassword(), usersDto.isEnabled());
                        if (savedUserCredentials != null) {
                            savedUsersDto.setEnabled(savedUserCredentials.isEnabled());
                            savedUsersDto.setResponseCode(HttpStatus.OK.value());
                            savedUsersDto.setResponseMessage(MessageResponseUser.OK_002);
                        }
                    }
                }
            } else if (strAction.equals(EDIT)) {
                savedUsersDto = new UsersDto(savedUser);
                if (usersDto.getRoleId() != null) {
                    savedUsersDto.setRoleEntity(arrayListSavedRoleEntity);
                }
                UserCredentials currentUserCredentials = userService.findUserCredentialsByUserId(getTenantId(), usersDto.getId());
                if (currentUserCredentials != null) {
                    if (usersDto.isEnabled() != currentUserCredentials.isEnabled()) {
                        currentUserCredentials.setEnabled(usersDto.isEnabled());
                        UserCredentials savedCurrentUserCredentials = userService.saveUserCredentials(getTenantId(), currentUserCredentials);
                        if (savedCurrentUserCredentials != null) {
                            savedUsersDto.setEnabled(savedCurrentUserCredentials.isEnabled());
                            savedUsersDto.setResponseCode(HttpStatus.OK.value());
                            savedUsersDto.setResponseMessage(MessageResponseUser.OK_002);
                        } else {
                            savedUsersDto.setResponseCode(HttpStatus.BAD_REQUEST.value());
                            savedUsersDto.setResponseMessage(MessageResponseUser.BAD_006);
                        }
                    } else {
                        savedUsersDto.setEnabled(usersDto.isEnabled());
                        savedUsersDto.setResponseCode(HttpStatus.OK.value());
                        savedUsersDto.setResponseMessage(MessageResponseUser.OK_002);
                    }
                } else {
                    savedUsersDto.setResponseCode(HttpStatus.BAD_REQUEST.value());
                    savedUsersDto.setResponseMessage(MessageResponseUser.BAD_007);
                }
            }
            // / update user_credential table

            // ghi log
            logEntityAction(savedUser.getId(), savedUser, savedUser.getCustomerId(), newUser.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);
            return savedUsersDto;
        } catch (Exception ex) {
            savedUsersDto.setResponseCode(HttpStatus.REQUEST_TIMEOUT.value());
            savedUsersDto.setResponseMessage(MessageResponseUser.BAD_004 + ex.getMessage());
            logEntityAction(emptyId(EntityType.USER), newUser, null, newUser.getId() == null ? ActionType.ADDED : ActionType.UPDATED, ex);
            return savedUsersDto;
        }
    }


    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_USERS_DELETE+ "\")")
    @DeleteMapping("/users/{userId}")
    public UsersDto deleteUser(@PathVariable("userId") String strUserId) throws ThingsboardException {
        UserId userId;
        User user;
        UsersDto response = new UsersDto();
        response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setResponseMessage(MessageResponseUser.INTERNAL_001);
        try {
            userId = new UserId(toUUID(strUserId));
            user = userService.findUserById(getCurrentUser().getTenantId(), userId);
            if (user != null) {
                // binhdv - k cho xóa tài khoản đã từng đăng nhập
                if(accessHistoryService.isUserLoggedInBefore(getTenantId().getId(), user.getEmail())){
                    response.setResponseCode(HttpStatus.BAD_REQUEST.value());
                    response.setResponseMessage(MessageResponseUser.BAD_009);
                } else{
                    userService.deleteUser(user.getTenantId(), userId);

                    // binhdv - delete damtom_user_active
                    userActiveService.deleteByTenantIdAndUserId(getTenantId().getId(), userId.getId());

                    response.setResponseCode(HttpStatus.OK.value());
                    response.setResponseMessage(MessageResponseUser.OK_003);
                    logEntityAction(userId, user, user.getCustomerId(), ActionType.DELETED, null, strUserId);
                }
            } else {
                response.setResponseCode(HttpStatus.BAD_REQUEST.value());
                response.setResponseMessage(MessageResponseUser.BAD_005);
            }
            return response;
        } catch (Exception ex) {
            response.setResponseCode(HttpStatus.REQUEST_TIMEOUT.value());
            response.setResponseMessage(MessageResponseUser.BAD_005 + ex.getMessage());
            logEntityAction(emptyId(EntityType.USER), null, null, ActionType.DELETED, ex, strUserId);
            return response;
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_USERS_ACCESS_HISTORY+ "\")")
    @RequestMapping(value = "/users/logs", params = {"pageSize", "page"}, method = RequestMethod.GET)
    public PageData<AuditLog> getAuditLogs(
            @RequestParam int pageSize,
            @RequestParam int page,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String sortProperty,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(name = "entityType", required = false) String entityTypesStr) throws ThingsboardException {
        TenantId tenantId;
        List<EntityType> entityTypes;
        TimePageLink pageLink;

        // trim search text
        if(textSearch != null) textSearch = textSearch.trim();

        try {
            tenantId = getCurrentUser().getTenantId();
            entityTypes = parseEntityTypesStr(entityTypesStr);
            pageLink = createTimePageLink(pageSize, page, textSearch, sortProperty, sortOrder, startTime, endTime);
            return checkNotNull(accessHistoryService.findAuditLogsByTenantId(tenantId, entityTypes, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_USERS_ACCESS_HISTORY+ "\")")
    @RequestMapping(value = "/users/logs/{logId}", method = RequestMethod.GET)
    public AuditLog getLog(@PathVariable("logId") String strLogId) throws ThingsboardException {
        UUID tenantId;
        UUID userId;
        UUID logId;
        AuditLogEntity currentAuditLogEntity;
        AuditLog currentAuditLog = new AuditLog();
        try {
            tenantId = getCurrentUser().getTenantId().getId();
            userId = getCurrentUser().getId().getId();
            logId = toUUID(strLogId);
            currentAuditLogEntity = accessHistoryService.findAuditLogEntityByTenantIdAndUserIdAndId(tenantId, userId, logId);
            if (currentAuditLogEntity != null) {
                currentAuditLog.setActionStatus(currentAuditLogEntity.getActionStatus());
                currentAuditLog.setActionData(currentAuditLogEntity.getActionData());
                currentAuditLog.setActionFailureDetails(currentAuditLogEntity.getActionFailureDetails());
            }
            return checkNotNull(currentAuditLog);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_USERS_ACCESS_HISTORY+ "\")")
    @RequestMapping(value = "/users/logs/excel", params = {"pageSize", "page"}, method = RequestMethod.GET)
    public InputStreamResource exportExcel(
            @RequestParam int pageSize,
            @RequestParam int page,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String sortProperty,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(name = "entityType", required = false) String strEntityType) throws ThingsboardException {
        InputStreamResource inputStreamResource = null;
        ByteArrayInputStream byteArrayInputStream;
        List<EntityType> entityTypes;
        TimePageLink pageLink;
        PageData<AuditLog> auditLogPageData;
        List<ExcelDto> excelDtoList = new ArrayList<>();
        PageData<ExcelDto> excelDtoPageData;
        TenantId tenantId;

        // trim search text
        if(textSearch != null) textSearch = textSearch.trim();

        try {
            tenantId = getCurrentUser().getTenantId();
            entityTypes = parseEntityTypesStr(strEntityType);
            pageLink = createTimePageLink(pageSize, page, textSearch, sortProperty, sortOrder, startTime, endTime);
            auditLogPageData = accessHistoryService.findAuditLogsByTenantId(tenantId, entityTypes, pageLink);
            if (checkNotNull(auditLogPageData) != null) {
                pageLink = createTimePageLink(1000, 0, textSearch, sortProperty, sortOrder, startTime, endTime); // toi da 1k record
                auditLogPageData = accessHistoryService.findAuditLogsByTenantId(tenantId, entityTypes, pageLink);
                excelDtoList = auditLogPageData.getData().stream()
                        .map(ExcelDto::new).collect(Collectors.toList());
                byteArrayInputStream = excelService.writeExcel(excelDtoList);
                inputStreamResource = new InputStreamResource(byteArrayInputStream);
            }
        } catch (Exception ex) {
            throw handleException(ex);
        }
        return inputStreamResource;
    }

    // binhdv - convert +84, 84 ... to 0...
    private String convertPhoneNumber(String phoneNumber){
        // trim
        phoneNumber = phoneNumber.trim();

        // convert :
        if(phoneNumber.startsWith("+84")){
            phoneNumber = phoneNumber.replaceFirst("\\+84", "0");
        }
        else if(phoneNumber.startsWith("84")){
            phoneNumber = phoneNumber.replaceFirst("84", "0");
        }

        return phoneNumber;
    }

    private boolean isPhoneValid(String phoneNumber){
        phoneNumber = phoneNumber.trim();
        if(phoneNumber.equals("")) {
            return true;
        }
        Pattern p = Pattern.compile("^\\+?[0-9]{7,44}$");
        Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }

    TimePageLink createTimePageLink(int pageSize, int page, String textSearch, String sortProperty, String sortOrder, Long startTime, Long endTime) throws ThingsboardException {
        PageLink pageLink = this.createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        return new TimePageLink(pageLink, startTime, endTime);
    }

    private List<ActionType> parseActionTypesStr(String actionTypesStr) {
        List<ActionType> result = null;
        if (StringUtils.isNoneBlank(actionTypesStr)) {
            String[] tmp = actionTypesStr.split(",");
            result = Arrays.stream(tmp).map(at -> ActionType.valueOf(at.toUpperCase())).collect(Collectors.toList());
        }
        return result;
    }

    private List<EntityType> parseEntityTypesStr(String entityTypesStr) {
        List<EntityType> result = null;
        if (StringUtils.isNoneBlank(entityTypesStr)) {
            String[] tmp = entityTypesStr.split(",");
            result = Arrays.stream(tmp).map(at -> EntityType.valueOf(at.toUpperCase())).collect(Collectors.toList());
        }
        return result;
    }

    private void setRoleAndActiveToUsersDto(List<UsersDto> usersDtoList) throws ThingsboardException {
        for (UsersDto usersDto : usersDtoList) {
            boolean isActive = userService.findUserCredentialsByUserId(getCurrentUser().getTenantId(), usersDto.getId()).isEnabled();
            usersDto.setEnabled(isActive);
            ArrayList<UserRoleEntity> arrayListUserRole = usersRoleService.findByTenantIdAndUserId(getCurrentUser().getTenantId().getId(), usersDto.getId().getId());
            if (arrayListUserRole != null) {
                ArrayList<RoleEntity> arrayListRole = new ArrayList<>();
                for (UserRoleEntity userRoleEntity : arrayListUserRole) {
                    RoleEntity roleEntity = userRoleEntity.getRole();
                    arrayListRole.add(roleEntity);
                }
                usersDto.setRoleEntity(arrayListRole);
            }
        }
    }

    UUID toUUID(String id) {
        return UUID.fromString(id);
    }

    PageLink createPageLink(int pageSize, int page, String textSearch, String sortProperty, String sortOrder) throws ThingsboardException {
        if (!StringUtils.isEmpty(sortProperty)) {
            SortOrder.Direction direction = SortOrder.Direction.ASC;
            if (!StringUtils.isEmpty(sortOrder)) {
                try {
                    direction = SortOrder.Direction.valueOf(sortOrder.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new ThingsboardException("Unsupported sort order '" + sortOrder + "'! Only 'ASC' or 'DESC' types are allowed.", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
            }
            SortOrder sort = new SortOrder(sortProperty, direction);
            return new PageLink(pageSize, page, textSearch, sort);
        } else {
            return new PageLink(pageSize, page, textSearch);
        }
    }
}
