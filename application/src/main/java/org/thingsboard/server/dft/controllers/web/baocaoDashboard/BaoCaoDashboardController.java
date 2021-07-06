package org.thingsboard.server.dft.controllers.web.baocaoDashboard;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.queue.util.TbCoreComponent;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class BaoCaoDashboardController extends BaseController {

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_DAMTOM + "\")")
    @GetMapping(value = "/bao-cao/dashboard")
    @ResponseBody
    public ResponseEntity<?> getViewBaoCaoDashboard() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
