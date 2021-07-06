package org.thingsboard.server.dft.services.khachhang.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.model.sql.CustomerEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangDto;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangEditDto;
import org.thingsboard.server.dft.entities.KhachHangEntity;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IKhachHangService {

    PageData<KhachHangDto> findAllByTenKhachHang(Pageable pageable, String searchText);

    KhachHangDto findKhachHangById(UUID id) throws JsonProcessingException;

    KhachHangDto save(KhachHangDto khachHangDto, UUID createById, CustomerId customerId) throws IOException;

    KhachHangEditDto update(KhachHangEditDto khachHangDto) throws JsonProcessingException;

    void deleteById(UUID id);

    KhachHangDto saveTaiLieu(UUID id, MultipartFile[] multipartFiles) throws JsonProcessingException;

    Resource downloadTaiLieu(UUID khachHangId, String tenTaiLieu);

    User createTenantAccount(KhachHangEntity khachHangEntity, CustomerId customerId);

    boolean checkActiveKhachHang(UUID tenantId);

    DeviceProfileEntity createGatewayDeviceProfile(UUID tenantId) throws JsonProcessingException;

    KhachHangDto deleteFileTaiLieu(UUID id, List<String> listFile) throws JsonProcessingException;

    CustomerEntity createNewCustomer(UUID tenantId);

    // binhdv
    KhachHangEntity findByTenantId(UUID tenantId);

    // binhdv
    KhachHangEntity updateKhachHang(KhachHangEntity khachHangEntity);
}
