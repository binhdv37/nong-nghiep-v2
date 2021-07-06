package org.thingsboard.server.dft.controllers.web.khachhang;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.model.sql.AdminSettingsEntity;
import org.thingsboard.server.dft.common.validator.khachhang.ValidateKhachHangService;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.CameraSetting;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangDto;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangEditDto;
import org.thingsboard.server.dft.repositories.DftAdminSettingsRepository;
import org.thingsboard.server.dft.services.khachhang.KhachHangService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/sys-admin")
public class KhachHangController extends BaseController {

  private final KhachHangService khachHangService;
  private final ValidateKhachHangService validateKhachHangService;


  @Autowired
  public KhachHangController(
          KhachHangService khachHangService,
          ValidateKhachHangService validateKhachHangService) {
    this.khachHangService = khachHangService;
    this.validateKhachHangService = validateKhachHangService;
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @GetMapping(value = "/khach-hang")
  @ResponseBody
  public ResponseEntity<?> getAllKhachHang(
      @RequestParam(name = "pageSize") int pageSize,
      @RequestParam(name = "page") int page,
      @RequestParam(required = false, defaultValue = "") String textSearch,
      @RequestParam(required = false, defaultValue = "id") String sortProperty,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
    try {
      Pageable pageable =
          PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
      PageData<KhachHangDto> pageData =
          khachHangService.findAllByTenKhachHang(pageable, textSearch);
      return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @PostMapping(value = "/khach-hang")
  public ResponseEntity<?> saveKhachHang(@RequestBody @Valid KhachHangDto khachHangDto) {
    if (khachHangDto.getSoDienThoai().startsWith("+84")) {
      khachHangDto.setSoDienThoai(khachHangDto.getSoDienThoai().replace("+84", "0"));
    } else if (khachHangDto.getSoDienThoai().startsWith("84")) {
      khachHangDto.setSoDienThoai(khachHangDto.getSoDienThoai().replaceFirst("84", "0"));
    }
    try {
      UUID idUserCreate = getCurrentUser().getUuidId();
      CustomerId customerId = getCurrentUser().getCustomerId();
      khachHangDto.setMaKhachHang(khachHangDto.getMaKhachHang().trim());
      khachHangDto.setTenKhachHang(khachHangDto.getTenKhachHang().trim());
      khachHangDto.setGhiChu(khachHangDto.getGhiChu().trim());
      khachHangDto.setEmail(khachHangDto.getEmail().trim());
      return new ResponseEntity<>(
          checkNotNull(khachHangService.save(khachHangDto, idUserCreate, customerId)),
          HttpStatus.CREATED);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @RequestMapping(value = "/khach-hang/{id}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getKhachHangById(@PathVariable("id") UUID id) {
    try {
      return new ResponseEntity<>(
          checkNotNull(khachHangService.findKhachHangById(id)), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @RequestMapping(value = "/khach-hang/{id}", method = RequestMethod.PUT)
  @ResponseBody
  public ResponseEntity<?> updateKhachHang(
      @PathVariable("id") UUID id, @RequestBody @Valid KhachHangEditDto khachHangDto) {
    if (khachHangDto.getSoDienThoai().startsWith("+84")) {
      khachHangDto.setSoDienThoai(khachHangDto.getSoDienThoai().replace("+84", "0"));
    } else if (khachHangDto.getSoDienThoai().startsWith("84")) {
      khachHangDto.setSoDienThoai(khachHangDto.getSoDienThoai().replaceFirst("84", "0"));
    }
    try {
      khachHangDto.setMaKhachHang(khachHangDto.getMaKhachHang().trim());
      khachHangDto.setTenKhachHang(khachHangDto.getTenKhachHang().trim());
      khachHangDto.setGhiChu(khachHangDto.getGhiChu().trim());
      khachHangDto.setEmail(khachHangDto.getEmail().trim());
      return new ResponseEntity<>(
          checkNotNull(khachHangService.update(khachHangDto)), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @DeleteMapping(value = "/khach-hang/{id}")
  public ResponseEntity<?> deleteKhachHang(@PathVariable("id") UUID id) {
    try {
      khachHangService.deleteById(id);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @PostMapping(value = "/khach-hang/{id}/upload-files")
  public ResponseEntity<?> uploadTaiLieuKhachHang(
      @PathVariable("id") UUID id, @RequestParam("taiLieu") MultipartFile[] files) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(khachHangService.saveTaiLieu(id, files));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @PostMapping(value = "/khach-hang/{id}/remove-files")
  public ResponseEntity<?> removeFile(
      @PathVariable("id") UUID id, @RequestBody List<String> listTaiLieuRemove) {
    try {
      return ResponseEntity.status(HttpStatus.OK)
          .body(khachHangService.deleteFileTaiLieu(id, listTaiLieuRemove));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @GetMapping(value = "/khach-hang/{id}/download-file")
  public ResponseEntity<?> downloadTaiLieuKhachHang(
      @PathVariable("id") UUID id, @RequestParam("taiLieu") String fileName) {
    try {
      Resource file = khachHangService.downloadTaiLieu(id, fileName);
      Path path = file.getFile().toPath();

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path))
          .header(
              HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + file.getFilename() + "\"")
          .body(file);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @GetMapping(value = "/khach-hang/validate")
  public ResponseEntity<?> ValidateKhachHang(
      @RequestParam(name = "type") String type,
      @RequestParam(name = "id", required = false) UUID id,
      @RequestParam(name = "email", required = false) String email,
      @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
      @RequestParam(name = "maKhachHang", required = false) String maKhachHang) {
    try {
      if (type.equals(ValidateKhachHangService.TYPE_EDIT)) {
        if (id == null) {
          return new ResponseEntity<>(false, HttpStatus.OK);
        }
      } else {
        id = UUID.randomUUID();
      }
      if (email != null) {
        boolean check =
            validateKhachHangService.validateKhachHang(
                type, ValidateKhachHangService.EMAIL, email, id);
        return new ResponseEntity<>(check, HttpStatus.OK);
      } else if (phoneNumber != null) {
        if (phoneNumber.startsWith(" ")) {
          phoneNumber = phoneNumber.replaceFirst(" ", "+");
        }
        if (phoneNumber.startsWith("+84")) {
          phoneNumber = phoneNumber.replace("+84", "0");
        } else if (phoneNumber.startsWith("84")) {
          phoneNumber = phoneNumber.replaceFirst("84", "0");
        }
        boolean check =
            validateKhachHangService.validateKhachHang(
                type, ValidateKhachHangService.PHONE, phoneNumber, id);
        return new ResponseEntity<>(check, HttpStatus.OK);
      } else if (maKhachHang != null) {
        boolean check =
            validateKhachHangService.validateKhachHang(
                type, ValidateKhachHangService.MAKHACHHANG, maKhachHang, id);
        return new ResponseEntity<>(check, HttpStatus.OK);
      }
      return new ResponseEntity<>(false, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }
}
