package org.thingsboard.server.dft.controllers.web.bothietbi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.bothietbi.dtos.LabelDevice;
import org.thingsboard.server.dft.repositories.DftTelemetryViewRepository;
import org.thingsboard.server.dft.services.device.DftDeviceService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class DftDeviceController extends BaseController {

  private final DftDeviceService deviceService;
  private final DftTelemetryViewRepository dftTelemetryViewRepository;

  public DftDeviceController(DftDeviceService deviceService,
                             DftTelemetryViewRepository dftTelemetryViewRepository) {
    this.deviceService = deviceService;
    this.dftTelemetryViewRepository = dftTelemetryViewRepository;
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
          + PermissionConstants.PAGES_QLTHIETBI_EDIT
          + "\",\""
          + PermissionConstants.PAGES_DIEUKHIEN
          + "\")")
  @RequestMapping(value = "/dam-tom/device/{id}/change-label", method = RequestMethod.PUT)
  @ResponseBody
  public ResponseEntity<?> changeLabelDevice(
      @PathVariable("id") UUID deviceId, @RequestBody LabelDevice labelDevice) {
    try {
      SecurityUser securityUser = getCurrentUser();
      return new ResponseEntity<>(
          deviceService.changeDeviceLabel(
              securityUser.getTenantId().getId(), deviceId, labelDevice.getLabel()),
          HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

//  @RequestMapping(value = "/dam-tom/device/test-query", method = RequestMethod.PUT)
//  @ResponseBody
//  public ResponseEntity<?> testQuery() {
//    try {
//      SecurityUser securityUser = getCurrentUser();
//      return new ResponseEntity<>(
//              dftTelemetryViewRepository.getFirstTimeStamp(),
//              HttpStatus.OK);
//    } catch (Exception e) {
//      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
//    }
//  }
}
