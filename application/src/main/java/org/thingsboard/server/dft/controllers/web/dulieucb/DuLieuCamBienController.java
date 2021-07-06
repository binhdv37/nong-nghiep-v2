package org.thingsboard.server.dft.controllers.web.dulieucb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.services.dlcambien.DuLieuCamBienService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
@Slf4j
public class DuLieuCamBienController extends BaseController {

  private final DuLieuCamBienService duLieuCamBienService;

  @Autowired
  public DuLieuCamBienController(DuLieuCamBienService duLieuCamBienService) {
    this.duLieuCamBienService = duLieuCamBienService;
  }

  @PreAuthorize(
          "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_GIAMSAT + "\")")
  @GetMapping(value = "/giam-sat")
  @ResponseBody
  public ResponseEntity<?> getViewDuLieuCamBien() {
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize(
          "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DLCAMBIEN + "\")")
  @GetMapping(value = "/du-lieu-cb")
  @ResponseBody
  public ResponseEntity<?> getDuLieuCamBien(
      @RequestParam(name = "damTomId") UUID damTomId,
      @RequestParam(name = "startTime") long startTime,
      @RequestParam(name = "endTime") long endTime,
      @RequestParam(name = "pageSize") int pageSize,
      @RequestParam(name = "page") int page,
      @RequestParam(name = "sort", required = false, defaultValue = "desc") String sort)
      throws ThingsboardException {
    TenantId tenantId = getCurrentUser().getTenantId();
    return new ResponseEntity<>(
        duLieuCamBienService.getBoDuLieuCamBien(tenantId, damTomId, startTime, endTime, pageSize, page, sort),
        HttpStatus.OK);
  }
}
