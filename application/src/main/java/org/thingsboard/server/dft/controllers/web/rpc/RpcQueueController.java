package org.thingsboard.server.dft.controllers.web.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.common.constants.command_rpc.CommandStatus;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.CommandQueueDto;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.RpcCommandDto;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;
import org.thingsboard.server.dft.services.rpc.RpcCommandQueueService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/rpc-queue")
public class RpcQueueController extends BaseController {

    private final RpcCommandQueueService rpcCommandQueueService;
    private final DftDeviceRepository dftDeviceRepository;

    @Autowired
    public RpcQueueController(RpcCommandQueueService rpcCommandQueueService, DftDeviceRepository dftDeviceRepository) {
        this.rpcCommandQueueService = rpcCommandQueueService;
        this.dftDeviceRepository = dftDeviceRepository;
    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> saveManualRpcToCommandQueue(@RequestBody RpcCommandDto rpcCommandDto) {
        try {
            UUID tenantId = getCurrentUser().getTenantId().getId();
            return new ResponseEntity<>(
                    rpcCommandQueueService.saveManualCommand(rpcCommandDto, tenantId), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping
  @ResponseBody
  public ResponseEntity<?> getAllSuccessCommand(
      @RequestParam(name = "damtomId") UUID damTomId,
      @RequestParam(name = "deviceId", required = false) UUID deviceId,
      @RequestParam(name = "pageSize") int pageSize,
      @RequestParam(name = "page") int page,
      @RequestParam(required = false, defaultValue = "id") String sortProperty,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "startTime", required = false) Long startTime,
      @RequestParam(name = "endTime", required = false) Long endTime) {
    try {
      SecurityUser securityUser = getCurrentUser();
      Pageable pageable =
          PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
      Page<CommandQueueDto> pageCommand =
          rpcCommandQueueService
              .getPageRpcCommandByStatus(
                  pageable,
                  securityUser.getTenantId().getId(),
                  damTomId,
                  deviceId,
                  startTime,
                  endTime,
                  CommandStatus.SUCCESS)
              .map(CommandQueueDto::new);
      for (CommandQueueDto commandQueueDto : pageCommand.getContent()) {
          DeviceEntity deviceEntity = dftDeviceRepository.findDeviceEntityById(commandQueueDto.getDeviceId());
          if(deviceEntity.getLabel() != null) {
              commandQueueDto.setLabel(deviceEntity.getLabel());
          }
      }
      PageData<CommandQueueDto> pageData =
          new PageData<>(
              pageCommand.getContent(),
              pageCommand.getTotalPages(),
              pageCommand.getTotalElements(),
              pageCommand.hasNext());
      return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize(
      "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DIEUKHIEN + "\")")
  @GetMapping("/count-new-command")
  @ResponseBody
  public ResponseEntity<?> coundNotViewed(@RequestParam(name = "damtomId") UUID damTomId) {
    try {
      SecurityUser securityUser = getCurrentUser();
      return new ResponseEntity<>(
          checkNotNull(
              rpcCommandQueueService.getCommandStatusNotViewed(
                  securityUser.getTenantId().getId(), damTomId)),
          HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }
}
