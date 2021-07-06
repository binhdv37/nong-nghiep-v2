package org.thingsboard.server.dft.controllers.web.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.FileStorageConstant;
import org.thingsboard.server.dft.common.service.FilesStorageService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
@Slf4j
public class FileController extends BaseController {

  @Value("${install.data_dir:application/src/main/data}")
  private String dataDir;

  private final FilesStorageService filesStorageService;

  @Autowired
  public FileController(
      FilesStorageService filesStorageService) {
    this.filesStorageService = filesStorageService;
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/image/file/upload")
  public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
    if(file.getSize() > 10485760) {
      return new ResponseEntity<>("File size > 10MB", HttpStatus.BAD_REQUEST);
    }
    try {
      UUID userId = getCurrentUser().getId().getId();

      String trueSavePath = dataDir + FileStorageConstant.KHACHHANG_DOCUMENT_PATH + File.separator + userId;
      String savedFileName =
          filesStorageService.save(file, trueSavePath, file.getOriginalFilename());
      return new ResponseEntity<>(savedFileName, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/image/{id}/download/{fileName}")
  public ResponseEntity<?> getFile(
          @PathVariable(name = "id") UUID pathId, @PathVariable(name = "fileName") String fileName) {
    try {
      String trueSavePath =
              dataDir + FileStorageConstant.KHACHHANG_DOCUMENT_PATH + File.separator + pathId;
      log.info("Download file from: " + trueSavePath + "- file name : " + fileName);
      Resource file = filesStorageService.download(trueSavePath, fileName);
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
}
