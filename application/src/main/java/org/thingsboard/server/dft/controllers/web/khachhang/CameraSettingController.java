package org.thingsboard.server.dft.controllers.web.khachhang;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Http;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.model.sql.AdminSettingsEntity;
import org.thingsboard.server.dft.common.service.DefaultPasswordService;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.CameraSetting;
import org.thingsboard.server.dft.repositories.DftAdminSettingsRepository;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;
import java.util.Date;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/sys-admin")
public class CameraSettingController extends BaseController {

    private final DefaultPasswordService defaultPasswordService;
    private final DftAdminSettingsRepository dftAdminSettingsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CameraSettingController(DefaultPasswordService defaultPasswordService,
                                   DftAdminSettingsRepository dftAdminSettingsRepository) {
        this.defaultPasswordService = defaultPasswordService;
        this.dftAdminSettingsRepository = dftAdminSettingsRepository;
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @PostMapping(value = "/settings/camera")
    @ResponseBody
    public ResponseEntity<?> settingCameraServer(@RequestBody @Valid CameraSetting cameraSetting) {
        try {
            AdminSettingsEntity adminSettingsEntity = new AdminSettingsEntity();
            if (dftAdminSettingsRepository.findAdminSettingsEntityByKey("camera") != null) {
                adminSettingsEntity = dftAdminSettingsRepository.findAdminSettingsEntityByKey("camera");
            } else {
                adminSettingsEntity.setId(UUID.randomUUID());
                adminSettingsEntity.setCreatedTime(new Date().getTime());
            }
            JsonNode jsonNode = objectMapper.valueToTree(cameraSetting);
            adminSettingsEntity.setJsonValue(jsonNode);
            adminSettingsEntity.setKey("camera");
            return new ResponseEntity<>(
                    dftAdminSettingsRepository.save(adminSettingsEntity), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @GetMapping(value = "/settings/camera")
    @ResponseBody
    public ResponseEntity<?> getSettingCameraServer() {
        try {
            AdminSettingsEntity adminSettingsEntity =
                    dftAdminSettingsRepository.findAdminSettingsEntityByKey("camera");
            CameraSetting cameraSetting =
                    objectMapper.treeToValue(adminSettingsEntity.getJsonValue(), CameraSetting.class);
            return new ResponseEntity<>(cameraSetting, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/settings/camera-server-url")
    @ResponseBody
    public ResponseEntity<?> getCameraServerUrl() {
        try {
            AdminSettingsEntity adminSettingsEntity =
                    dftAdminSettingsRepository.findAdminSettingsEntityByKey("camera");
            if(adminSettingsEntity == null){
                return ResponseEntity.ok("");
            }
            CameraSetting cameraSetting =
                    objectMapper.treeToValue(adminSettingsEntity.getJsonValue(), CameraSetting.class);
            return new ResponseEntity<>(cameraSetting.getCameraSever(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @PostMapping(value = "/camera-account/sign-up/{email}")
    @ResponseBody
    public ResponseEntity<?> signUpAccountShinobi(@PathVariable("email") String email) {
        try {
            AdminSettingsEntity adminSettingsEntity =
                    dftAdminSettingsRepository.findAdminSettingsEntityByKey("camera");
            CameraSetting cameraSetting =
                    objectMapper.treeToValue(adminSettingsEntity.getJsonValue(), CameraSetting.class);

            StringBuilder shinobiUrl = new StringBuilder();
            shinobiUrl.append(cameraSetting.getCameraSever());
            if (cameraSetting.getCameraSever().endsWith("/")) {
                shinobiUrl.append("admin/");
            } else {
                shinobiUrl.append("/admin/");
            }
            shinobiUrl.append(cameraSetting.getApiKey());
            if (cameraSetting.getSignUpPath().startsWith("/")) {
                shinobiUrl.append(cameraSetting.getSignUpPath());
            } else {
                shinobiUrl.append("/" + cameraSetting.getSignUpPath());
            }

            MultiValueMap httpHeaders = new LinkedMultiValueMap();
            httpHeaders.set("Content-Type", "application/json");

            JSONObject userShinobiInfo = new JSONObject();
            userShinobiInfo.put("mail", email);
            userShinobiInfo.put("ke", "");
            userShinobiInfo.put("pass", defaultPasswordService.getRawPassword());
            userShinobiInfo.put("detail", "");

            JSONObject requestJson = new JSONObject();
            requestJson.put("data", userShinobiInfo);
            HttpEntity<String> httpEntity = new HttpEntity<String>(requestJson.toString(), httpHeaders);

            RestTemplate restTemplate = new RestTemplate();
            String shinobiRespose = restTemplate.postForObject(shinobiUrl.toString(), httpEntity, String.class);
            return new ResponseEntity<>(shinobiRespose, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @DeleteMapping(value = "/camera-account/delete/{email}")
    @ResponseBody
    public ResponseEntity<?> deleteAccountShinobi(@PathVariable("email") String email) {
        try {
            AdminSettingsEntity adminSettingsEntity =
                    dftAdminSettingsRepository.findAdminSettingsEntityByKey("camera");
            CameraSetting cameraSetting =
                    objectMapper.treeToValue(adminSettingsEntity.getJsonValue(), CameraSetting.class);

            StringBuilder shinobiUrl = new StringBuilder();
            shinobiUrl.append(cameraSetting.getCameraSever());
            if (cameraSetting.getCameraSever().endsWith("/")) {
                shinobiUrl.append("admin/");
            } else {
                shinobiUrl.append("/admin/");
            }
            shinobiUrl.append(cameraSetting.getApiKey());
            if (cameraSetting.getDeletePath().startsWith("/")) {
                shinobiUrl.append(cameraSetting.getDeletePath());
            } else {
                shinobiUrl.append("/" + cameraSetting.getDeletePath());
            }

            MultiValueMap httpHeaders = new LinkedMultiValueMap();
            httpHeaders.set("Content-Type", "application/json");

            JSONObject userShinobiInfo = new JSONObject();
            userShinobiInfo.put("destination", email);

            JSONObject requestJson = new JSONObject();
            requestJson.put("data", userShinobiInfo);
            HttpEntity<String> httpEntity = new HttpEntity<String>(requestJson.toString(), httpHeaders);

            RestTemplate restTemplate = new RestTemplate();
            String shinobiRespose = restTemplate.postForObject(shinobiUrl.toString(), httpEntity, String.class);
            return new ResponseEntity<>(shinobiRespose, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }
}
