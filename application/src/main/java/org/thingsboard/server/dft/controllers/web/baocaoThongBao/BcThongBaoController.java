package org.thingsboard.server.dft.controllers.web.baocaoThongBao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto.BcMultiDataDto;
import org.thingsboard.server.dft.entities.NotificationLogEntity;
import org.thingsboard.server.dft.services.baocaoThongBao.BcThongBaoService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.ArrayList;
import java.util.List;

import static org.thingsboard.server.dft.common.constants.NotiticationTypeConstant.*;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class BcThongBaoController extends BaseController {

    @Autowired
    BcThongBaoService bcThongBaoService;

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_GUI_TTCANHBAO + "\")")
    @GetMapping("/bc-gui-ttcb")
    public ResponseEntity<List<BcMultiDataDto>> getAllWarningInformByRangeDate(
            @RequestParam(name = "startTs") long startTs,
            @RequestParam(name = "endTs") long endTs
    ) throws ThingsboardException {
        try {
            List<BcMultiDataDto> result = new ArrayList<>();

            List<NotificationLogEntity> notificationLogEntities =
                    bcThongBaoService.getNotificationByCreatedTimeRange(getTenantId().getId(), startTs, endTs);

            if (notificationLogEntities.size() <= 0) {
                return ResponseEntity.ok(result);
            }

            result.add(new BcMultiDataDto("Notification",
                    bcThongBaoService.getDataToArraySeriesDto(TYPE_NOTIFICATION, notificationLogEntities)));

            result.add(new BcMultiDataDto("Tin nhắn (SMS)",
                    bcThongBaoService.getDataToArraySeriesDto(TYPE_SMS, notificationLogEntities)));

            result.add(new BcMultiDataDto("Email",
                    bcThongBaoService.getDataToArraySeriesDto(TYPE_EMAIL, notificationLogEntities)));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_GUI_TTCANHBAO + "\")")
    @GetMapping("/bc-gui-ttcb/mail")
    public ResponseEntity<String> getGuiThongBaoMailContent(
            @RequestParam(name = "startTs") long startTs,
            @RequestParam(name = "endTs") long endTs
    ) throws ThingsboardException {
        try {
            String mailContent = bcThongBaoService.getMailContentData(getTenantId().getId(), startTs, endTs);
            return ResponseEntity.ok(mailContent);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


}
