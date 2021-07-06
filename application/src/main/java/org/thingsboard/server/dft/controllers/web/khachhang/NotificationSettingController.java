package org.thingsboard.server.dft.controllers.web.khachhang;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.model.sql.AdminSettingsEntity;
import org.thingsboard.server.dft.common.service.DefaultPasswordService;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.CameraSetting;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.NotificationSetting;
import org.thingsboard.server.dft.controllers.web.thongbao.ThongBaoService;
import org.thingsboard.server.dft.repositories.DftAdminSettingsRepository;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;
import java.util.Date;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/sys-admin")
public class NotificationSettingController extends BaseController {

    private final DftAdminSettingsRepository dftAdminSettingsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public NotificationSettingController(DftAdminSettingsRepository dftAdminSettingsRepository) {
        this.dftAdminSettingsRepository = dftAdminSettingsRepository;
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @GetMapping(value = "/settings/notification")
    @ResponseBody
    public ResponseEntity<?> getSettingNotificationServer() {
        try {
            AdminSettingsEntity adminSettingsEntity =
                    dftAdminSettingsRepository.findAdminSettingsEntityByKey("notification");
            NotificationSetting notificationSetting =
                    objectMapper.treeToValue(adminSettingsEntity.getJsonValue(), NotificationSetting.class);
            return new ResponseEntity<>(notificationSetting, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @PostMapping(value = "/settings/notification")
    @ResponseBody
    public ResponseEntity<?> settingNotificationServer(@RequestBody @Valid NotificationSetting notificationSetting) {
        try {
            AdminSettingsEntity adminSettingsEntity = new AdminSettingsEntity();
            if (dftAdminSettingsRepository.findAdminSettingsEntityByKey("notification") != null) {
                adminSettingsEntity = dftAdminSettingsRepository.findAdminSettingsEntityByKey("notification");
            } else {
                adminSettingsEntity.setId(UUID.randomUUID());
                adminSettingsEntity.setCreatedTime(new Date().getTime());
            }
            JsonNode jsonNode = objectMapper.valueToTree(notificationSetting);
            adminSettingsEntity.setJsonValue(jsonNode);
            adminSettingsEntity.setKey("notification");
            ThongBaoService.setNotificationSetting(null);
            return new ResponseEntity<>(
                    dftAdminSettingsRepository.save(adminSettingsEntity), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }
}
