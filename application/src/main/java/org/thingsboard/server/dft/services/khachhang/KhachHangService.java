package org.thingsboard.server.dft.services.khachhang;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.common.data.DeviceProfileProvisionType;
import org.thingsboard.server.common.data.DeviceProfileType;
import org.thingsboard.server.common.data.DeviceTransportType;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.dao.device.DeviceProfileService;
import org.thingsboard.server.dao.model.sql.CustomerEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dao.model.sql.TenantEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.dft.common.constants.EntityNameConstant;
import org.thingsboard.server.dft.common.constants.FileStorageConstant;
import org.thingsboard.server.dft.common.service.DefaultPasswordService;
import org.thingsboard.server.dft.common.service.FilesStorageService;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangDto;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangEditDto;
import org.thingsboard.server.dft.entities.KhachHangEntity;
import org.thingsboard.server.dft.entities.UserActiveEntity;
import org.thingsboard.server.dft.repositories.DftCustomerRepository;
import org.thingsboard.server.dft.repositories.DftDeviceProfileRepository;
import org.thingsboard.server.dft.repositories.DftTenantProfileRepository;
import org.thingsboard.server.dft.repositories.DftUserRepository;
import org.thingsboard.server.dft.repositories.KhachHangRepository;
import org.thingsboard.server.dft.services.khachhang.imp.IKhachHangService;
import org.thingsboard.server.dft.services.usersActive.UserActiveService;
import org.thingsboard.server.service.install.InstallScripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class KhachHangService implements IKhachHangService {

  private final KhachHangRepository khachHangRepository;
  private final FilesStorageService filesStorageService;
  private final DftUserRepository dftUserRepository;
  private final DftDeviceProfileRepository dftDeviceProfileRepository;
  private final DftCustomerRepository dftCustomerRepository;
  private final UserActiveService userActiveService;

  private final TenantService tenantService;
  private final InstallScripts installScripts;

  private final UserService userService;
  private final DefaultPasswordService defaultPasswordService;

  private static final ObjectMapper jsonMapper = new ObjectMapper();


  @Value("${install.data_dir:application/src/main/data}")
  private String dataDir;

  @Autowired
  public KhachHangService(
          KhachHangRepository khachHangRepository,
          FilesStorageService filesStorageService,
          DftUserRepository dftUserRepository,
          DftDeviceProfileRepository dftDeviceProfileRepository,
          DftCustomerRepository dftCustomerRepository,
          UserActiveService userActiveService, TenantService tenantService,
          InstallScripts installScripts, UserService userService,
          DefaultPasswordService defaultPasswordService) {
    this.khachHangRepository = khachHangRepository;
    this.filesStorageService = filesStorageService;
    this.dftUserRepository = dftUserRepository;
    this.dftDeviceProfileRepository = dftDeviceProfileRepository;
    this.dftCustomerRepository = dftCustomerRepository;
    this.userActiveService = userActiveService;
    this.tenantService = tenantService;
    this.installScripts = installScripts;
    this.userService = userService;
    this.defaultPasswordService = defaultPasswordService;
  }

  @Override
  public PageData<KhachHangDto> findAllByTenKhachHang(Pageable pageable, String searchText) {
    if (searchText == null || searchText.isEmpty()) {
      searchText = "";
    }
    searchText = searchText.trim();
    Page<KhachHangDto> pageKhachHang =
        khachHangRepository.findAllByTenKhachHang(searchText, pageable).map(KhachHangDto::new);
    PageData<KhachHangDto> pageDataKhachHang =
        new PageData<>(
            pageKhachHang.getContent(),
            pageKhachHang.getTotalPages(),
            pageKhachHang.getTotalElements(),
            pageKhachHang.hasNext());
    return pageDataKhachHang;
  }

  @Override
  public KhachHangDto findKhachHangById(UUID id) {
    Optional<KhachHangEntity> khachHangEntity = khachHangRepository.findById(id);
    KhachHangDto khachHangDto = khachHangEntity.map(KhachHangDto::new).get();
    try {
      khachHangDto.setDsTaiLieu(
          jsonMapper.readValue(khachHangEntity.get().getTaiLieuDinhKem(), ArrayList.class));
    } catch (Exception e) {
      khachHangDto.setDsTaiLieu(Collections.EMPTY_LIST);
    }
    return khachHangDto;
  }

  @Override
  public KhachHangDto save(KhachHangDto khachHangDto, UUID createById, CustomerId customerId)
          throws IOException {
    if(dftUserRepository.findDistinctFirstByEmail(khachHangDto.getEmail()) != null) {
      throw new IllegalArgumentException("User with email '" + khachHangDto.getEmail() + "'already present in database!");
    }
    KhachHangEntity khachHangEntity = new KhachHangEntity();
    khachHangEntity.setId(UUID.randomUUID());
    khachHangEntity.setCreatedTime(new Date().getTime());
    khachHangEntity.setCreatedBy(createById);
    khachHangEntity.setMaKhachHang(khachHangDto.getMaKhachHang());
    khachHangEntity.setTenKhachHang(khachHangDto.getTenKhachHang());
    khachHangEntity.setNgayBatDau(new Date(khachHangDto.getNgayBatDau()));
    khachHangEntity.setGhiChu(khachHangDto.getGhiChu());
    khachHangEntity.setActive(khachHangDto.isActive());
    khachHangEntity.setTaiLieuDinhKem(jsonMapper.writeValueAsString(Collections.EMPTY_LIST));
    log.info("Create new khach hang entitiy: " + khachHangEntity.getId());

    Tenant tenant = new Tenant();
    tenant.setTitle(khachHangDto.getMaKhachHang());
    tenant.setPhone(khachHangDto.getSoDienThoai());
    tenant.setEmail(khachHangDto.getEmail());
    tenant.setCreatedTime(new Date().getTime());
    Tenant saveTenant = tenantService.saveTenant(tenant);
    installScripts.createDefaultRuleChains(saveTenant.getId());
    DeviceProfileEntity gatewayProfile = createGatewayDeviceProfile(saveTenant.getId().getId());
    createNewCustomer(saveTenant.getId().getId());
    khachHangEntity.setTenantEntity(new TenantEntity(saveTenant));
    khachHangRepository.save(khachHangEntity);
    createTenantAccount(khachHangEntity, customerId);
    log.info("Create success khach hang");
    khachHangDto.setId(khachHangEntity.getId());

    return khachHangDto;
  }

  @Override
  public KhachHangEditDto update(KhachHangEditDto khachHangDto) throws JsonProcessingException {
    log.info("Update khach hang entitiy: " + khachHangDto.getId());
    KhachHangEntity khachHangEntity = khachHangRepository.findById(khachHangDto.getId()).get();
    khachHangEntity.setMaKhachHang(khachHangDto.getMaKhachHang());
    khachHangEntity.setTenKhachHang(khachHangDto.getTenKhachHang());
    khachHangEntity.setNgayBatDau(new Date(khachHangDto.getNgayBatDau()));
    khachHangEntity.setGhiChu(khachHangDto.getGhiChu());
    khachHangEntity.setActive(khachHangDto.isActive());
//    khachHangEntity.setTaiLieuDinhKem(jsonMapper.writeValueAsString(khachHangDto.getDsTaiLieu()));

    khachHangEntity.getTenantEntity().setTitle(khachHangDto.getMaKhachHang());
    khachHangEntity
        .getTenantEntity()
        .setSearchText(khachHangDto.getMaKhachHang().toLowerCase(Locale.ROOT));
    khachHangEntity.getTenantEntity().setPhone(khachHangDto.getSoDienThoai());
    tenantService.saveTenant(khachHangEntity.getTenantEntity().toData());
    khachHangRepository.save(khachHangEntity);
    User userTenant = userService.findUserByEmail(new TenantId(khachHangEntity.getTenantEntity().getId()), khachHangEntity.getTenantEntity().getEmail());
    userTenant.setLastName(khachHangEntity.getTenantEntity().getPhone());
    userTenant.setFirstName(khachHangEntity.getTenKhachHang());
    userService.saveUser(userTenant);
    return khachHangDto;
  }

  @Override
  public void deleteById(UUID id) {
    KhachHangEntity khachHangEntity = khachHangRepository.findById(id).get();
    String email = khachHangEntity.getTenantEntity().getEmail();
    TenantEntity tenantEntity = khachHangEntity.getTenantEntity();
    List<UserEntity> list = dftUserRepository
            .findUserEntitiesByTenantIdAndEmailNotLike(tenantEntity.getId(), email);
    if(!list.isEmpty()) {
      throw new IllegalStateException("Không thể xóa khách hàng đã hoạt động.");
    }
    khachHangRepository.deleteById(id);
    dftUserRepository.delete(dftUserRepository.findDistinctFirstByEmail(email));
  }

  @Override
  public KhachHangDto saveTaiLieu(UUID id, MultipartFile[] multipartFiles)
      throws JsonProcessingException {
    KhachHangEntity khachHangEntity = khachHangRepository.findById(id).get();
    List<String> listTaiLieu =
        khachHangEntity.getTaiLieuDinhKem().isEmpty()
            ? new ArrayList<>()
            : jsonMapper.readValue(khachHangEntity.getTaiLieuDinhKem(), ArrayList.class);
    for (MultipartFile file : multipartFiles) {
      String fileName = file.getOriginalFilename();
      String trueSavePath =
              dataDir + FileStorageConstant.KHACHHANG_DOCUMENT_PATH + File.separator + khachHangEntity.getId();
      try {
        String savedFileName = filesStorageService.save(file, trueSavePath, fileName);
        if (!listTaiLieu.contains(fileName)) {
          listTaiLieu.add(savedFileName);
        }
      } catch (Exception e) {
        listTaiLieu.remove(fileName);
      }
      log.info("Save file success: " + fileName + " to dir: " + trueSavePath);
    }
    khachHangEntity.setTaiLieuDinhKem(jsonMapper.writeValueAsString(listTaiLieu));
    khachHangRepository.save(khachHangEntity);
    return new KhachHangDto(khachHangEntity);
  }

  @Override
  public Resource downloadTaiLieu(UUID khachHangId, String tenTaiLieu) {
    String trueSavePath = dataDir + FileStorageConstant.KHACHHANG_DOCUMENT_PATH + File.separator + khachHangId;
    log.info("Download file from: " + trueSavePath + "- file name : " + tenTaiLieu);
    return filesStorageService.download(trueSavePath, tenTaiLieu);
  }

  @Override
  public User createTenantAccount(KhachHangEntity khachHangEntity, CustomerId customerId) {
    User tenantUser = new User();
    tenantUser.setEmail(khachHangEntity.getTenantEntity().getEmail());
    tenantUser.setFirstName(khachHangEntity.getTenKhachHang());
    tenantUser.setLastName(khachHangEntity.getTenantEntity().getPhone());
    tenantUser.setTenantId(khachHangEntity.getTenantEntity().toData().getId());
    tenantUser.setCustomerId(customerId);
    tenantUser.setAuthority(Authority.TENANT_ADMIN);
    tenantUser.setCreatedTime(new Date().getTime());
    User savedUser = userService.saveUser(tenantUser);

    UserCredentials userCredentials =
        userService.findUserCredentialsByUserId(savedUser.getTenantId(), savedUser.getId());
    userCredentials.setActivateToken(null);
    userCredentials.setPassword(defaultPasswordService.getDefaultPassword());
    userCredentials.setEnabled(khachHangEntity.isActive());
    userCredentials.setUserId(savedUser.getId());
    userService.saveUserCredentials(savedUser.getTenantId(), userCredentials);
    log.info("Active tenant account: " + savedUser.getId().getId());

    UserActiveEntity userActiveEntity = new UserActiveEntity(
            UUID.randomUUID(),
            savedUser.getTenantId().getId(),
            savedUser.getId().getId(),
            false,
            userActiveService.genRandomActivateCode(),
            System.currentTimeMillis()
    );
    userActiveService.save(userActiveEntity);

    return tenantUser;
  }

  @Override
  public boolean checkActiveKhachHang(UUID tenantId) {
    KhachHangEntity khachHangEntity = khachHangRepository.findKhachHangEntitiesByTenantId(tenantId);
    if (khachHangEntity != null) {
      return khachHangEntity.isActive();
    } else {
      return false;
    }
  }

  @Override
  public DeviceProfileEntity createGatewayDeviceProfile(UUID tenantId) throws JsonProcessingException {
    DeviceProfileEntity deviceProfileEntity = new DeviceProfileEntity();
    deviceProfileEntity.setId(UUID.randomUUID());
    deviceProfileEntity.setTenantId(tenantId);
    deviceProfileEntity.setCreatedTime(new Date().getTime());
    deviceProfileEntity.setName(EntityNameConstant.DEVICE_PROFILE_GATEWAY);
    deviceProfileEntity.setType(DeviceProfileType.DEFAULT);
    deviceProfileEntity.setTransportType(DeviceTransportType.DEFAULT);
    deviceProfileEntity.setProvisionType(DeviceProfileProvisionType.DISABLED);
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode =
            objectMapper.readTree(
                    "{\"alarms\": null, \"configuration\": {\"type\": \"DEFAULT\"}, \"provisionConfiguration\": {\"type\": \"DISABLED\", \"provisionDeviceSecret\": null}, \"transportConfiguration\": {\"type\": \"DEFAULT\"}}");
    deviceProfileEntity.setProfileData(jsonNode);
    deviceProfileEntity.setDescription("Gateway Dam tom profile");
    deviceProfileEntity.setSearchText(EntityNameConstant.DEVICE_PROFILE_GATEWAY.toLowerCase(Locale.ROOT));
    deviceProfileEntity.setDefault(false);
    return dftDeviceProfileRepository.save(deviceProfileEntity);
  }

  @Override
  public KhachHangDto deleteFileTaiLieu(UUID id, List<String> listFile) throws JsonProcessingException {
    KhachHangEntity khachHangEntity = khachHangRepository.findById(id).get();
    List<String> listTaiLieu =
            khachHangEntity.getTaiLieuDinhKem().isEmpty()
                    ? new ArrayList<>()
                    : jsonMapper.readValue(khachHangEntity.getTaiLieuDinhKem(), ArrayList.class);
    String trueSavePath = dataDir + FileStorageConstant.KHACHHANG_DOCUMENT_PATH + File.separator + id;
    listFile.forEach(file -> {
      if (filesStorageService.deleteFileInPath(trueSavePath, file)) {
        listTaiLieu.remove(file);
      }
    });
    khachHangEntity.setTaiLieuDinhKem(jsonMapper.writeValueAsString(listTaiLieu));
    khachHangRepository.save(khachHangEntity);
    return new KhachHangDto(khachHangEntity);
  }

  @Override
  public CustomerEntity createNewCustomer(UUID tenantId) {
    CustomerEntity customerEntity = new CustomerEntity();
    customerEntity.setId(UUID.randomUUID());
    customerEntity.setTitle("Customer of: " + tenantId);
    customerEntity.setSearchText("Customer of: " + tenantId);
    customerEntity.setTenantId(tenantId);
    customerEntity.setCreatedTime(new Date().getTime());
    return dftCustomerRepository.save(customerEntity);
  }

  // binhdv
  @Override
  public KhachHangEntity findByTenantId(UUID tenantId) {
    return khachHangRepository.findByTenantId(tenantId);
  }

  // binhdv
  @Override
  public KhachHangEntity updateKhachHang(KhachHangEntity khachHangEntity) {
    return khachHangRepository.save(khachHangEntity);
  }


}
