package org.thingsboard.server.dft.controllers.web.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.RpcScheduleDto;
import org.thingsboard.server.dft.repositories.RpcScheduleRepository;
import org.thingsboard.server.dft.services.rpc.RpcScheduleService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/rpc-schedule")
public class RpcScheduleController extends BaseController {

  private final RpcScheduleService rpcScheduleService;
  @Autowired
  public RpcScheduleController(
      RpcScheduleService rpcScheduleService) {
    this.rpcScheduleService = rpcScheduleService;
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping
  @ResponseBody
  public ResponseEntity<?> findAllByDamTomId(@RequestParam UUID damTomId) {
    try {
      SecurityUser securityUser = getCurrentUser();
      return new ResponseEntity<>(
          rpcScheduleService.getListRpcScheduleByDamTomId(
              securityUser.getTenantId().getId(), damTomId),
          HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping("/{id}")
  @ResponseBody
  public ResponseEntity<?> findById(@PathVariable("id") UUID id) {
    try {
      SecurityUser securityUser = getCurrentUser();
      return new ResponseEntity<>(
          rpcScheduleService.getRpcScheduleById(securityUser.getTenantId().getId(), id),
          HttpStatus.OK);
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
      rpcScheduleService.deleteSchedule(id);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @PostMapping
  @ResponseBody
  public ResponseEntity<?> save(@RequestBody RpcScheduleDto rpcScheduleDto) {
    try {
      rpcScheduleDto.setName(rpcScheduleDto.getName().trim());
      if(!CronSequenceGenerator.isValidExpression(rpcScheduleDto.getCron())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Cron expression trong lập lịch không hợp lệ");
      }
      if (rpcScheduleService.existsByDamTomIdAndName(
          rpcScheduleDto.getDamTomId(), rpcScheduleDto.getName())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Tên hẹn giờ điều khiển đã tồn tại!");
      }
      SecurityUser securityUser = getCurrentUser();
      RpcScheduleDto result = rpcScheduleService.createRpcSchedule(rpcScheduleDto, securityUser);
      return new ResponseEntity<>(result, HttpStatus.CREATED);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @PutMapping("/{id}")
  @ResponseBody
  public ResponseEntity<?> update(
      @PathVariable("id") UUID id, @RequestBody RpcScheduleDto rpcScheduleDto) {
    try {
      rpcScheduleDto.setName(rpcScheduleDto.getName().trim());
      if(!CronSequenceGenerator.isValidExpression(rpcScheduleDto.getCron())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Cron expression hẹn giờ không hợp lệ");
      }
      if (rpcScheduleService.existsByDamTomIdAndNameAndIdNot(
          rpcScheduleDto.getDamTomId(), rpcScheduleDto.getName(), id)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Tên hẹn giờ điều khiển đã tồn tại!");
      }
      SecurityUser securityUser = getCurrentUser();
      RpcScheduleDto result =
          rpcScheduleService.updateRpcSchedule(id, rpcScheduleDto, securityUser);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
  @GetMapping(value = "/rpc-schedule/validate")
  public ResponseEntity<?> ValidateKhachHang(
          @RequestParam(name = "id", required = false) UUID id,
          @RequestParam(name = "damTomId") UUID damTomId,
          @RequestParam(name = "rpcSchduleName") String rpcSchduleName) {
    try {
      rpcSchduleName = rpcSchduleName.trim();
      boolean check;
      if(id != null) {
         check = rpcScheduleService.existsByDamTomIdAndNameAndIdNot(
                damTomId, rpcSchduleName, id);
      } else {
        check = rpcScheduleService.existsByDamTomIdAndName(
                damTomId, rpcSchduleName);
      }
      return ResponseEntity.ok(check);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }
}
