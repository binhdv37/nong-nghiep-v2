package org.thingsboard.server.dft.controllers.web.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.DeviceSetting;
import org.thingsboard.server.dft.services.rpc.RpcSettingService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/rpc-setting")
public class RpcSettingController extends BaseController {

  private final RpcSettingService rpcSettingService;

  @Autowired
  public RpcSettingController(RpcSettingService rpcSettingService) {
    this.rpcSettingService = rpcSettingService;
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping("/{id}")
  @ResponseBody
  public ResponseEntity<?> findById(@PathVariable("id") UUID id) {
    try {
      return new ResponseEntity<>(rpcSettingService.getById(id), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @DeleteMapping("/{id}")
  @ResponseBody
  public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) {
    try {
      rpcSettingService.deleteById(id);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @PostMapping
  @ResponseBody
  public ResponseEntity<?> saveOrUpdate(@RequestBody DeviceSetting deviceSetting) {
    try {
      return new ResponseEntity<>(rpcSettingService.saveOrUpdate(deviceSetting),HttpStatus.CREATED);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }
}
