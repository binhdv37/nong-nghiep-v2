package org.thingsboard.server.dft.controllers.web.baocaoketnoi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.services.baocaoketnoi.BaoCaoKetNoiService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class BaoCaoKetNoiController extends BaseController {

    private final BaoCaoKetNoiService baoCaoKetNoiService;

    public BaoCaoKetNoiController(BaoCaoKetNoiService baoCaoKetNoiService) {
        this.baoCaoKetNoiService = baoCaoKetNoiService;
    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_KETNOICAMBIEN + "\")")
    @GetMapping(value = "/bc-ket-noi/chart")
    @ResponseBody
    public ResponseEntity<?> getChartDataKetNoi(
            @RequestParam(name = "damTomId", required = false) UUID damTomId,
            @RequestParam(name = "startTime") long startTime,
            @RequestParam(name = "endTime") long endTime)
            throws ThingsboardException {
        TenantId tenantId = getCurrentUser().getTenantId();
        return new ResponseEntity<>(
                baoCaoKetNoiService.getChartData( damTomId, tenantId.getId(), startTime, endTime),
                HttpStatus.OK);
    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_KETNOICAMBIEN + "\")")
    @GetMapping(value = "/bc-ket-noi/table")
    @ResponseBody
    public ResponseEntity<?> getTableDataKetNoi(
            @RequestParam(name = "damTomId", required = false) UUID damTomId,
            @RequestParam(name = "startTime") long startTime,
            @RequestParam(name = "endTime") long endTime)
            throws ThingsboardException {
        TenantId tenantId = getCurrentUser().getTenantId();
        return new ResponseEntity<>(
                baoCaoKetNoiService.getTableData( damTomId, tenantId.getId(), startTime, endTime),
                HttpStatus.OK);
    }


    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_KETNOICAMBIEN + "\")")
    @GetMapping(value = "/bc-ket-noi/mail")
    @ResponseBody
    public ResponseEntity<?> getMailDataKetNoi(
            @RequestParam(name = "damTomId", required = false) UUID damTomId,
            @RequestParam(name = "startTime") long startTime,
            @RequestParam(name = "endTime") long endTime)
            throws ThingsboardException {
        return new ResponseEntity<>(
                baoCaoKetNoiService.getMailContentData(getTenantId().getId(), damTomId, startTime, endTime),
                HttpStatus.OK);
    }
}
