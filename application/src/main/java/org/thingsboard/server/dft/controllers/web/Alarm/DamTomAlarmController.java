package org.thingsboard.server.dft.controllers.web.Alarm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.AlarmRuleLog;
import org.thingsboard.server.common.data.AlarmRuleLogV2;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;
import org.thingsboard.server.common.data.device.profile.DeviceProfileData;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.AlarmRuleLogId;
import org.thingsboard.server.common.data.id.AlarmRuleLogIdV2;
import org.thingsboard.server.common.data.id.DamTomLogId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.device.DeviceProfileService;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dft.common.constants.AlarmNamePrefixConstant;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.Alarm.dtos.*;
import org.thingsboard.server.dft.entities.DamTomAlarmEntity;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomNotifyTokenEntity;
import org.thingsboard.server.dft.services.DamTomAlarm.DamTomAlarmService;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.thongbao.DamTomnotifyTokenService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class DamTomAlarmController extends BaseController {
    @Autowired
    DamTomAlarmService damTomAlarmService;

    @Autowired
    protected DamTomService damTomService;

    @Autowired
    protected DeviceProfileService deviceProfileService;

    @Autowired
    protected DamTomnotifyTokenService damTomnotifyTokenService;

    private static final String DAMTOM_DOES_NOT_EXIST = "Damtom does not exist";
    private static final String ALARM_RULE_NAME_ALREADY_EXIST = "Alarm rule name already exist";
    private static final String ALARM_RULE_DOES_NOT_EXIST = "Alarm rule does not exist";
    private static final String ALARM_RULE_NAME_CANNOT_BE_NULL = "Alarm rule name can not be null";
    private static final String ALARM_RULE_NAME_CAN_NOT_BE_DUPLICATE = "Alarm rule name can not be duplicate";

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_TLLUATCANHBAO
                    + "\")")
    @RequestMapping(value = "/alarm-dt/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getAlarmByDamTomId(@PathVariable("id") UUID damTomId)
            throws ThingsboardException {
        TenantId tenantId = getCurrentUser().getTenantId();
        DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId.getId(), damTomId);
        //        DamTomAlarmEntity damTomAlarmEntity =
        // damTomAlarmService.getDamTomAlarmByDamTomId(tenantId.getId(), damTomId);
        return new ResponseEntity<>(damTomEntity.getDeviceProfile(), HttpStatus.OK);
    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_TLLUATCANHBAO
                    + "\")")
    @RequestMapping(value = "v2/alarm-dt", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getAlarmByDamTomIdv2(@RequestParam("id") UUID damTomId)
            throws ThingsboardException {

        TenantId tenantId = getCurrentUser().getTenantId();
        List<DamTomAlarmEntity> resultListDamTomAlarmEntity = new ArrayList<>();

        Map<String, String> alarmIdMap = new HashMap<>();
        List<DamTomAlarmEntity> damTomAlarmEntityListPage =
                damTomAlarmService.findDistinctByAlarmIdAndDamtomIdAndTenantId(damTomId, tenantId.getId());
        for (DamTomAlarmEntity damTomAlarmEntity : damTomAlarmEntityListPage) {
            if (!alarmIdMap.containsKey(damTomAlarmEntity.getAlarmId())) {
                alarmIdMap.put(damTomAlarmEntity.getAlarmId(), "");
                resultListDamTomAlarmEntity.add(damTomAlarmEntity);
            }
        }
        return new ResponseEntity<>(resultListDamTomAlarmEntity, HttpStatus.OK);
    }

//    @PreAuthorize(
//            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_TLLUATCANHBAO + "\")")
//    @RequestMapping(value = "/alarm-dt", method = RequestMethod.POST)
//    @ResponseBody
//    public ResponseEntity<?> saveAlarmDamTom(@RequestBody AlarmDto alarmDto)
//            throws ThingsboardException, JsonProcessingException {
//        try {
//            UUID tenantId = getCurrentUser().getTenantId().getId();
//            UUID userId = getCurrentUser().getId().getId();
//
//            DamTomAlarmEntity damTomAlarmEntity = damTomAlarmService.findByDamTomAlarmId(tenantId, alarmDto.getAlarmId());
//
//            if (damTomAlarmEntity != null) {
//                //edit
//                damTomAlarmEntity.setName(alarmDto.getName());
//                damTomAlarmEntity.setNote(alarmDto.getNote());
//                damTomAlarmEntity.setViaSms(alarmDto.isViaSms());
//                damTomAlarmEntity.setViaEmail(alarmDto.isViaEmail());
//                damTomAlarmEntity.setViaNotification(alarmDto.isViaNotification());
//                if (!alarmDto.getThucHienDieuKhien().isEmpty()) {
//                    damTomAlarmEntity.setGroupRpcId(UUID.fromString(alarmDto.getThucHienDieuKhien()));
//                } else {
//                    damTomAlarmEntity.setGroupRpcId(null);
//                }
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode jsonNode =
//                        objectMapper.readTree(alarmDto.getProfile_Data());
//                damTomAlarmEntity.getDeviceProfile().setProfileData(jsonNode);
//                DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(damTomAlarmEntity.getDeviceProfile().toData()));
////            damTomAlarmService.saveDeviceProfile(damTomAlarmEntity.getDeviceProfile());
//                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
//                tbClusterService.onEntityStateChange(getCurrentUser().getTenantId(), savedDeviceProfile.getId(),
//                        ComponentLifecycleEvent.UPDATED);
//                DamTomAlarmEntity savedDamtomAlarm = damTomAlarmService.saveDamTomAlarm(damTomAlarmEntity);
//
//                // binhdv - ghi log
//                AlarmRuleLog alarmRuleLog = new AlarmRuleLog(
//                        new AlarmRuleLogId(savedDamtomAlarm.getId()),
//                        getTenantId(),
//                        getCurrentUser().getCustomerId(),
//                        new DamTomLogId(savedDamtomAlarm.getDamtomId()),
//                        savedDamtomAlarm.getDeviceProfile().toData(),
//                        savedDamtomAlarm.getAlarmId(),
//                        savedDamtomAlarm.getName(),
//                        savedDamtomAlarm.getNote(),
//                        savedDamtomAlarm.isViaEmail(),
//                        savedDamtomAlarm.isViaSms(),
//                        savedDamtomAlarm.isViaNotification(),
//                        savedDamtomAlarm.getGroupRpcId()
//                );
//                logEntityAction(alarmRuleLog.getId(), alarmRuleLog, getCurrentUser().getCustomerId(), ActionType.UPDATED, null);
//
//                return new ResponseEntity<>(savedDamtomAlarm, HttpStatus.OK);
////            return ResponseEntity.ok(1);
//            } else {
//                //create
////                DamTomAlarmEntity damTomAlarmEntity2 = new DamTomAlarmEntity();
////                damTomAlarmEntity2.setId(UUID.randomUUID());
////                damTomAlarmEntity2.setTenantId(tenantId);
////                damTomAlarmEntity2.setDamtomId(alarmDto.getDamtomId());
////                damTomAlarmEntity2.setName(alarmDto.getName());
////                damTomAlarmEntity2.setNote(alarmDto.getNote());
////                damTomAlarmEntity2.setViaSms(alarmDto.isViaSms());
////                damTomAlarmEntity2.setViaEmail(alarmDto.isViaEmail());
////                damTomAlarmEntity2.setViaNotification(alarmDto.isViaNotification());
////                damTomAlarmEntity2.setCreatedBy(userId);
////                damTomAlarmEntity2.setCreatedTime(System.currentTimeMillis());
////                damTomAlarmEntity2.setAlarmId(alarmDto.getAlarmId());
////                if(!alarmDto.getThucHienDieuKhien().isEmpty()){
////                    damTomAlarmEntity2.setGroupRpcId(UUID.fromString(alarmDto.getThucHienDieuKhien()));
////                }
//
////                DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, alarmDto.getDamtomId());
////
////                DeviceProfileEntity deviceProfileEntity = damTomEntity.getDeviceProfile();
////                ObjectMapper objectMapper = new ObjectMapper();
////                JsonNode jsonNode = objectMapper.readTree(alarmDto.getProfile_Data());
////                deviceProfileEntity.setProfileData(jsonNode);
////                DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfileEntity.toData()));
//////            damTomAlarmService.saveDeviceProfile(deviceProfileEntity);
////                damTomAlarmEntity2.setDeviceProfile(deviceProfileEntity);
////                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
////                tbClusterService.onEntityStateChange(getCurrentUser().getTenantId(), savedDeviceProfile.getId(),
////                        ComponentLifecycleEvent.CREATED);
////                DamTomAlarmEntity savedDamtomAlarm = damTomAlarmService.saveDamTomAlarm(damTomAlarmEntity2);
//
//                DamTomAlarmEntity damTomAlarmEntity2 = new DamTomAlarmEntity();
//                damTomAlarmEntity2.setId(UUID.randomUUID());
//                damTomAlarmEntity2.setTenantId(tenantId);
//                damTomAlarmEntity2.setDamtomId(alarmDto.getDamtomId());
//                damTomAlarmEntity2.setName(alarmDto.getName());
//                damTomAlarmEntity2.setNote(alarmDto.getNote());
//                damTomAlarmEntity2.setViaSms(alarmDto.isViaSms());
//                damTomAlarmEntity2.setViaEmail(alarmDto.isViaEmail());
//                damTomAlarmEntity2.setViaNotification(alarmDto.isViaNotification());
//                damTomAlarmEntity2.setCreatedBy(userId);
//                damTomAlarmEntity2.setCreatedTime(System.currentTimeMillis());
//                damTomAlarmEntity2.setAlarmId(alarmDto.getAlarmId());
//                if (!alarmDto.getThucHienDieuKhien().isEmpty()) {
//                    damTomAlarmEntity2.setGroupRpcId(UUID.fromString(alarmDto.getThucHienDieuKhien()));
//                }
//
//
////                DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, alarmDto.getDamtomId());
////                DeviceProfileEntity deviceProfileEntity = damTomEntity.getDeviceProfile();
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode jsonNode = objectMapper.readTree(alarmDto.getProfile_Data());
//                DamTomAlarmEntity savedDamtomAlarm = new DamTomAlarmEntity();
////                deviceProfileEntity.setProfileData(jsonNode);
//                List<DeviceProfileEntity> deviceProfileEntityList = damTomAlarmService.getDeviceProfileEntityList(tenantId, alarmDto.getDamtomId());
//                for (DeviceProfileEntity deviceProfileEntity : deviceProfileEntityList) {
//                    deviceProfileEntity.setProfileData(jsonNode);
//                    DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfileEntity.toData()));
//                    damTomAlarmEntity2.setDeviceProfile(deviceProfileEntity);
//                    tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
//                    tbClusterService.onEntityStateChange(getCurrentUser().getTenantId(), savedDeviceProfile.getId(),
//                            ComponentLifecycleEvent.CREATED);
//                    savedDamtomAlarm = damTomAlarmService.saveDamTomAlarm(damTomAlarmEntity2);
//                    damTomAlarmEntity2.setId(UUID.randomUUID());
//                    System.out.println(damTomAlarmEntity2.getAlarmId() + "" + damTomAlarmEntity2.getName());
//                }
//
////            damTomAlarmService.saveDeviceProfile(deviceProfileEntity);
//
//
//                // binhdv - ghi log
//                AlarmRuleLog alarmRuleLog = new AlarmRuleLog(
//                        new AlarmRuleLogId(savedDamtomAlarm.getId()),
//                        getTenantId(),
//                        getCurrentUser().getCustomerId(),
//                        new DamTomLogId(savedDamtomAlarm.getDamtomId()),
//                        savedDamtomAlarm.getDeviceProfile().toData(),
//                        savedDamtomAlarm.getAlarmId(),
//                        savedDamtomAlarm.getName(),
//                        savedDamtomAlarm.getNote(),
//                        savedDamtomAlarm.isViaEmail(),
//                        savedDamtomAlarm.isViaSms(),
//                        savedDamtomAlarm.isViaNotification(),
//                        savedDamtomAlarm.getGroupRpcId()
//                );
//                logEntityAction(alarmRuleLog.getId(), alarmRuleLog, getCurrentUser().getCustomerId(), ActionType.ADDED, null);
//
//                return new ResponseEntity<>(savedDamtomAlarm, HttpStatus.OK);
//            }
//        } catch (Exception e) {
//            // binhdv - ghi log
//            DamTomAlarmEntity damTomAlarmEntity = damTomAlarmService.findByDamTomAlarmId(getTenantId().getId(), alarmDto.getAlarmId());
//
//            DamTomLogId damTomLogId = alarmDto.getDamtomId() == null ? null : new DamTomLogId(alarmDto.getDamtomId());
//
//            DeviceProfile deviceProfile = null;
//            DamTomEntity damTomEntity = damTomService.getDamTomById(getTenantId().getId(), alarmDto.getDamtomId());
//            if (damTomEntity != null) {
//                DeviceProfileEntity deviceProfileEntity = damTomEntity.getDeviceProfile();
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode jsonNode = objectMapper.readTree(alarmDto.getProfile_Data());
//                deviceProfileEntity.setProfileData(jsonNode);
//                deviceProfile = deviceProfileEntity.toData();
//            }
//
//            AlarmRuleLog alarmRuleLog = new AlarmRuleLog(
//                    null,
//                    getTenantId(),
//                    getCurrentUser().getCustomerId(),
//                    damTomLogId,
//                    deviceProfile,
//                    alarmDto.getAlarmId(),
//                    alarmDto.getName(),
//                    alarmDto.getNote(),
//                    alarmDto.isViaEmail(),
//                    alarmDto.isViaSms(),
//                    alarmDto.isViaNotification(),
//                    alarmDto.getThucHienDieuKhien().isEmpty() ? null : UUID.fromString(alarmDto.getThucHienDieuKhien())
//            );
//            logEntityAction(emptyId(EntityType.ALARM_RULE), alarmRuleLog, null, damTomAlarmEntity == null ? ActionType.ADDED : ActionType.UPDATED, e);
//
//            throw handleException(e);
//        }
//    }

    /**
     * revert
     *
     * @param alarmDto
     * @return
     * @throws ThingsboardException
     * @throws JsonProcessingException
     */
    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_TLLUATCANHBAO + "\")")
    @RequestMapping(value = "/alarm-dt", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> saveAlarmDamTom(@RequestBody AlarmDto alarmDto)
            throws ThingsboardException, JsonProcessingException {
        try {
            UUID tenantId = getCurrentUser().getTenantId().getId();
            UUID userId = getCurrentUser().getId().getId();

            DamTomAlarmEntity damTomAlarmEntity =
                    damTomAlarmService.findByDamTomAlarmId(tenantId, alarmDto.getAlarmId());

            if (damTomAlarmEntity != null) {
                //edit
                damTomAlarmEntity.setName(alarmDto.getName());
                damTomAlarmEntity.setNote(alarmDto.getNote());
                damTomAlarmEntity.setViaSms(alarmDto.isViaSms());
                damTomAlarmEntity.setViaEmail(alarmDto.isViaEmail());
                damTomAlarmEntity.setViaNotification(alarmDto.isViaNotification());
//                if(!alarmDto.getThucHienDieuKhien().isEmpty()){
//                    damTomAlarmEntity.setGroupRpcId(UUID.fromString(alarmDto.getThucHienDieuKhien()));
//                }else{
//                    damTomAlarmEntity.setGroupRpcId(null);
//                }
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode =
                        objectMapper.readTree(alarmDto.getProfile_Data());
                damTomAlarmEntity.getDeviceProfile().setProfileData(jsonNode);
                DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(damTomAlarmEntity.getDeviceProfile().toData()));
//            damTomAlarmService.saveDeviceProfile(damTomAlarmEntity.getDeviceProfile());
                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
                tbClusterService.onEntityStateChange(getCurrentUser().getTenantId(), savedDeviceProfile.getId(),
                        ComponentLifecycleEvent.UPDATED);
                DamTomAlarmEntity savedDamtomAlarm = damTomAlarmService.saveDamTomAlarm(damTomAlarmEntity);

                // binhdv - ghi log
                AlarmRuleLog alarmRuleLog =
                        new AlarmRuleLog(
                                new AlarmRuleLogId(savedDamtomAlarm.getId()),
                                getTenantId(),
                                getCurrentUser().getCustomerId(),
                                new DamTomLogId(savedDamtomAlarm.getDamtomId()),
                                savedDamtomAlarm.getDeviceProfile().toData(),
                                savedDamtomAlarm.getAlarmId(),
                                savedDamtomAlarm.getName(),
                                savedDamtomAlarm.getNote(),
                                savedDamtomAlarm.isViaEmail(),
                                savedDamtomAlarm.isViaSms(),
                                savedDamtomAlarm.isViaNotification(),
                                null
                                //                        savedDamtomAlarm.getGroupRpcId()
                        );
                logEntityAction(
                        alarmRuleLog.getId(),
                        alarmRuleLog,
                        getCurrentUser().getCustomerId(),
                        ActionType.UPDATED,
                        null);

                return new ResponseEntity<>(savedDamtomAlarm, HttpStatus.OK);
//            return ResponseEntity.ok(1);
            } else {
                //create
                DamTomAlarmEntity damTomAlarmEntity2 = new DamTomAlarmEntity();
                damTomAlarmEntity2.setId(UUID.randomUUID());
                damTomAlarmEntity2.setTenantId(tenantId);
                damTomAlarmEntity2.setDamtomId(alarmDto.getDamtomId());
                damTomAlarmEntity2.setName(alarmDto.getName());
                damTomAlarmEntity2.setNote(alarmDto.getNote());
                damTomAlarmEntity2.setViaSms(alarmDto.isViaSms());
                damTomAlarmEntity2.setViaEmail(alarmDto.isViaEmail());
                damTomAlarmEntity2.setViaNotification(alarmDto.isViaNotification());
                damTomAlarmEntity2.setCreatedBy(userId);
                damTomAlarmEntity2.setCreatedTime(System.currentTimeMillis());
                damTomAlarmEntity2.setAlarmId(alarmDto.getAlarmId());
//                if(!alarmDto.getThucHienDieuKhien().isEmpty()){
//                    damTomAlarmEntity2.setGroupRpcId(UUID.fromString(alarmDto.getThucHienDieuKhien()));
//                }

                DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, alarmDto.getDamtomId());

                DeviceProfileEntity deviceProfileEntity = damTomEntity.getDeviceProfile();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(alarmDto.getProfile_Data());
                deviceProfileEntity.setProfileData(jsonNode);
                DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfileEntity.toData()));
//            damTomAlarmService.saveDeviceProfile(deviceProfileEntity);
                damTomAlarmEntity2.setDeviceProfile(deviceProfileEntity);
                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
                tbClusterService.onEntityStateChange(getCurrentUser().getTenantId(), savedDeviceProfile.getId(),
                        ComponentLifecycleEvent.CREATED);
                DamTomAlarmEntity savedDamtomAlarm = damTomAlarmService.saveDamTomAlarm(damTomAlarmEntity2);

                // binhdv - ghi log
                AlarmRuleLog alarmRuleLog = new AlarmRuleLog(
                        new AlarmRuleLogId(savedDamtomAlarm.getId()),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        new DamTomLogId(savedDamtomAlarm.getDamtomId()),
                        savedDamtomAlarm.getDeviceProfile().toData(),
                        savedDamtomAlarm.getAlarmId(),
                        savedDamtomAlarm.getName(),
                        savedDamtomAlarm.getNote(),
                        savedDamtomAlarm.isViaEmail(),
                        savedDamtomAlarm.isViaSms(),
                        savedDamtomAlarm.isViaNotification(),
                        null
                );
                logEntityAction(alarmRuleLog.getId(), alarmRuleLog, getCurrentUser().getCustomerId(), ActionType.ADDED, null);

                return new ResponseEntity<>(savedDamtomAlarm, HttpStatus.OK);
            }
        } catch (Exception e) {
            // binhdv - ghi log
            DamTomAlarmEntity damTomAlarmEntity = damTomAlarmService.findByDamTomAlarmId(getTenantId().getId(), alarmDto.getAlarmId());

            DamTomLogId damTomLogId =
                    alarmDto.getDamtomId() == null ? null : new DamTomLogId(alarmDto.getDamtomId());

            DeviceProfile deviceProfile = null;
            DamTomEntity damTomEntity = damTomService.getDamTomById(getTenantId().getId(), alarmDto.getDamtomId());
            if (damTomEntity != null) {
                DeviceProfileEntity deviceProfileEntity = damTomEntity.getDeviceProfile();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(alarmDto.getProfile_Data());
                deviceProfileEntity.setProfileData(jsonNode);
                deviceProfile = deviceProfileEntity.toData();
            }

            AlarmRuleLog alarmRuleLog = new AlarmRuleLog(
                    null,
                    getTenantId(),
                    getCurrentUser().getCustomerId(),
                    damTomLogId,
                    deviceProfile,
                    alarmDto.getAlarmId(),
                    alarmDto.getName(),
                    alarmDto.getNote(),
                    alarmDto.isViaEmail(),
                    alarmDto.isViaSms(),
                    alarmDto.isViaNotification(),
                    alarmDto.getThucHienDieuKhien().isEmpty() ? null : UUID.fromString(alarmDto.getThucHienDieuKhien())
            );
            logEntityAction(emptyId(EntityType.ALARM_RULE), alarmRuleLog, null, damTomAlarmEntity == null ? ActionType.ADDED : ActionType.UPDATED, e);

            throw handleException(e);
        }
    }


    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_TLLUATCANHBAO + "\")")
    @RequestMapping(value = "/alarm-dt/alarm/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getAlarmByIdAlarm(@PathVariable("id") String alarmId) throws ThingsboardException {
        TenantId tenantId = getCurrentUser().getTenantId();
        DamTomAlarmEntity damTomAlarmEntity = damTomAlarmService.findByDamTomAlarmId(tenantId.getId(), alarmId);
        return new ResponseEntity<>(damTomAlarmEntity, HttpStatus.OK);
    }

//    @PreAuthorize(
//            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
//                    + PermissionConstants.PAGES_DAMTOM_DELETE
//                    + "\")")
//    @RequestMapping(value = "/alarm-dt/delete", method = RequestMethod.POST)
//    @ResponseBody
//    public ResponseEntity<Integer> deleteDamTomDevice(@RequestBody AlarmDeleteDto alarmDeleteDto)
//            throws ThingsboardException, JsonProcessingException {
//        try {
//            TenantId tenantId = getCurrentUser().getTenantId();
//            List<DamTomAlarmEntity> damTomAlarmEntityList = damTomAlarmService.findAllByAlarmIdAndTenantId(tenantId.getId(), alarmDeleteDto.getAlarmId());
//
//            if (damTomAlarmEntityList != null) {
//                DeviceProfileEntity deviceProfileEntity = damTomAlarmEntityList.get(0).getDeviceProfile();
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode jsonNode = objectMapper.readTree(alarmDeleteDto.getProfile_Data());
//                deviceProfileEntity.setProfileData(jsonNode);
//                damTomAlarmService.saveDeviceProfile(deviceProfileEntity);
//                damTomAlarmService.deleteDamTomAlarm(tenantId.getId(), alarmDeleteDto.getAlarmId());
//                DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfileEntity.toData()));
//                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
//                tbClusterService.onEntityStateChange(getCurrentUser().getTenantId(), savedDeviceProfile.getId(),
//                        ComponentLifecycleEvent.UPDATED);
//                // binhdv - ghi log
//                AlarmRuleLog alarmRuleLog = new AlarmRuleLog(
//                        new AlarmRuleLogId(damTomAlarmEntityList.get(0).getId()),
//                        getTenantId(),
//                        getCurrentUser().getCustomerId(),
//                        new DamTomLogId(damTomAlarmEntityList.get(0).getDamtomId()),
//                        deviceProfileEntity.toData(),
//                        damTomAlarmEntityList.get(0).getAlarmId(),
//                        damTomAlarmEntityList.get(0).getName(),
//                        damTomAlarmEntityList.get(0).getNote(),
//                        damTomAlarmEntityList.get(0).isViaEmail(),
//                        damTomAlarmEntityList.get(0).isViaSms(),
//                        damTomAlarmEntityList.get(0).isViaNotification(),
//                        damTomAlarmEntityList.get(0).getGroupRpcId()
//                );
//                logEntityAction(alarmRuleLog.getId(), alarmRuleLog, getCurrentUser().getCustomerId(), ActionType.DELETED, null, damTomAlarmEntityList.get(0).getId().toString());
//
//                // xoa thanh cong
//                return ResponseEntity.ok(1);
//            }
//            // loi xay ra
//            return ResponseEntity.ok(0);
//        } catch (Exception e) {
//            // binhdv - ghi log
//            DamTomAlarmEntity damTomAlarmEntity = damTomAlarmService.findByDamTomAlarmId(getTenantId().getId(), alarmDeleteDto.getAlarmId());
//            if (damTomAlarmEntity != null) {
//                AlarmRuleLog alarmRuleLog = new AlarmRuleLog(
//                        new AlarmRuleLogId(damTomAlarmEntity.getId()),
//                        getTenantId(),
//                        getCurrentUser().getCustomerId(),
//                        new DamTomLogId(damTomAlarmEntity.getDamtomId()),
//                        damTomAlarmEntity.getDeviceProfile().toData(),
//                        damTomAlarmEntity.getAlarmId(),
//                        damTomAlarmEntity.getName(),
//                        damTomAlarmEntity.getNote(),
//                        damTomAlarmEntity.isViaEmail(),
//                        damTomAlarmEntity.isViaSms(),
//                        damTomAlarmEntity.isViaNotification(),
//                        damTomAlarmEntity.getGroupRpcId()
//                );
//                logEntityAction(emptyId(EntityType.ALARM_RULE), alarmRuleLog, null, ActionType.DELETED, e, damTomAlarmEntity.getId().toString());
//            }
//
//            throw handleException(e);
//        }
//    }

    /**
     * revert
     *
     * @param alarmDeleteDto
     * @return
     * @throws ThingsboardException
     * @throws JsonProcessingException
     */
    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_DAMTOM_DELETE
                    + "\")")
    @RequestMapping(value = "/alarm-dt/delete", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Integer> deleteDamTomDevice(@RequestBody AlarmDeleteDto alarmDeleteDto)
            throws ThingsboardException, JsonProcessingException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            DamTomAlarmEntity damTomAlarmEntity = damTomAlarmService.findByDamTomAlarmId(tenantId.getId(), alarmDeleteDto.getAlarmId());
            if (damTomAlarmEntity != null) {
                DeviceProfileEntity deviceProfileEntity = damTomAlarmEntity.getDeviceProfile();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(alarmDeleteDto.getProfile_Data());
                deviceProfileEntity.setProfileData(jsonNode);
                damTomAlarmService.saveDeviceProfile(deviceProfileEntity);
                damTomAlarmService.deleteDamTomAlarm(tenantId.getId(), alarmDeleteDto.getAlarmId());
                DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfileEntity.toData()));
                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
                tbClusterService.onEntityStateChange(getCurrentUser().getTenantId(), savedDeviceProfile.getId(),
                        ComponentLifecycleEvent.UPDATED);

                // binhdv - ghi log
                AlarmRuleLog alarmRuleLog = new AlarmRuleLog(
                        new AlarmRuleLogId(damTomAlarmEntity.getId()),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        new DamTomLogId(damTomAlarmEntity.getDamtomId()),
                        deviceProfileEntity.toData(),
                        damTomAlarmEntity.getAlarmId(),
                        damTomAlarmEntity.getName(),
                        damTomAlarmEntity.getNote(),
                        damTomAlarmEntity.isViaEmail(),
                        damTomAlarmEntity.isViaSms(),
                        damTomAlarmEntity.isViaNotification(),
                        null
                );
                logEntityAction(alarmRuleLog.getId(), alarmRuleLog, getCurrentUser().getCustomerId(), ActionType.DELETED, null, damTomAlarmEntity.getId().toString());

                // xoa thanh cong
                return ResponseEntity.ok(1);
            }
            // loi xay ra
            return ResponseEntity.ok(0);
        } catch (Exception e) {
            // binhdv - ghi log
            DamTomAlarmEntity damTomAlarmEntity = damTomAlarmService.findByDamTomAlarmId(getTenantId().getId(), alarmDeleteDto.getAlarmId());
            if (damTomAlarmEntity != null) {
                AlarmRuleLog alarmRuleLog = new AlarmRuleLog(
                        new AlarmRuleLogId(damTomAlarmEntity.getId()),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        new DamTomLogId(damTomAlarmEntity.getDamtomId()),
                        damTomAlarmEntity.getDeviceProfile().toData(),
                        damTomAlarmEntity.getAlarmId(),
                        damTomAlarmEntity.getName(),
                        damTomAlarmEntity.getNote(),
                        damTomAlarmEntity.isViaEmail(),
                        damTomAlarmEntity.isViaSms(),
                        damTomAlarmEntity.isViaNotification(),
                        null
                );
                logEntityAction(emptyId(EntityType.ALARM_RULE), alarmRuleLog, null, ActionType.DELETED, e, damTomAlarmEntity.getId().toString());
            }

            throw handleException(e);
        }
    }


    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN','CUSTOMER_USER')")
    @RequestMapping(value = "/notify-token", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<DamTomNotifyTokenEntity> getAlarmByDamTomId(@RequestBody DamTomNotifyTokenEntity damTomNotifyTokenEntity) throws ThingsboardException {
        DamTomNotifyTokenEntity damTomNotifyTokenEntity9 = damTomnotifyTokenService.saveToken(damTomNotifyTokenEntity);
        return new ResponseEntity<>(damTomNotifyTokenEntity9, HttpStatus.OK);
    }


    /**
     * @param damTomId
     * @param alarmType
     * @throws ThingsboardException
     * @throws JsonProcessingException
     * @author viettd
     */
    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_DAMTOM_DELETE
                    + "\")")
    @DeleteMapping("/damtom-alarm")
    public boolean deleteDamTomAlarm(@RequestParam("damTomId") UUID damTomId,
                                     @RequestParam("alarmType") String alarmType)
            throws ThingsboardException, JsonProcessingException {


        UUID tenantId = getCurrentUser().getTenantId().getId();
        List<DeviceProfileEntity> deviceProfileEntityList = this.damTomAlarmService.getDeviceProfileEntityList(tenantId, damTomId);
        if (deviceProfileEntityList == null && deviceProfileEntityList.isEmpty()) {
            return false;
        }
        DeviceProfileData deviceProfileData = deviceProfileEntityList.get(0).toData().getProfileData();
        DeviceProfileAlarm tempDpAlarm;
        for (DeviceProfileAlarm deviceProfileAlarm : deviceProfileData.getAlarms()) {
            tempDpAlarm = deviceProfileAlarm;
            if (tempDpAlarm.getAlarmType().equals(alarmType)) {
                deviceProfileData.getAlarms().remove(tempDpAlarm);
                break;
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.convertValue(deviceProfileData, JsonNode.class);
        for (DeviceProfileEntity deviceProfileEntity : deviceProfileEntityList) {
            deviceProfileEntity.setProfileData(jsonNode);
            DeviceProfile savedDeviceProfile = this.damTomAlarmService.saveDeviceProfile(deviceProfileEntity).toData();
            tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
            tbClusterService.onEntityStateChange(getCurrentUser().getTenantId(), savedDeviceProfile.getId(),
                    ComponentLifecycleEvent.UPDATED);
        }
        return true;
    }

    /**
     * check name is exist
     *
     * @param damTomId
     * @param alarmType
     * @return
     * @throws ThingsboardException
     * @author viettd
     */
    @GetMapping("/damtom-alarm/exist")
    public boolean IsExistDamtomAlarmType(
            @RequestParam("damTomId") UUID damTomId, @RequestParam("alarmType") String alarmType)
            throws ThingsboardException {

        boolean isExist = false;
        UUID tenantId = getCurrentUser().getTenantId().getId();
        List<DeviceProfileEntity> deviceEntityList =
                this.damTomAlarmService.getDeviceProfileEntityList(tenantId, damTomId);
        if (deviceEntityList != null && !deviceEntityList.isEmpty()) {
            DeviceProfileData deviceProfileData = deviceEntityList.get(0).toData().getProfileData();

            for (DeviceProfileAlarm deviceProfileAlarm : deviceProfileData.getAlarms()) {
                if (deviceProfileAlarm.getAlarmType().equals(alarmType)) {
                    isExist = true;
                    break;
                }
            }
        }
        return isExist;
    }

    /**
     * check name is exist
     *
     * @param damTomId
     * @param alarmType
     * @return
     * @throws ThingsboardException
     * @author viettd
     */
    @GetMapping("/damtom-alarm")
    @ResponseBody
    public ResponseEntity<DeviceProfileAlarm> getAlarmOfDamTomInDeviceProfile(@RequestParam("damTomId") UUID damTomId,
                                                                              @RequestParam("alarmType") String alarmType)
            throws ThingsboardException {

//        UUID tenantId = getCurrentUser().getTenantId().getId();
//        List<DeviceProfileEntity> deviceEntityList = this.damTomAlarmService.getDeviceProfileEntityList(tenantId,damTomId);
//
//        if(deviceEntityList != null && !deviceEntityList.isEmpty()){
//            DeviceProfileData deviceProfileData = deviceEntityList.get(0).toData().getProfileData();
//
//            for(DeviceProfileAlarm deviceProfileAlarm : deviceProfileData.getAlarms()){
//                if(deviceProfileAlarm.getAlarmType().equals("ALARM-"+alarmType)){
//                    deviceProfileAlarm.setAlarmType(alarmType);
//                    return new ResponseEntity<>(deviceProfileAlarm,HttpStatus.OK);
//                }
//            }
//        }
//        return new ResponseEntity<>(null,HttpStatus.NO_CONTENT);
        UUID tenantId = getCurrentUser().getTenantId().getId();
        DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damTomId);

        if (damTomEntity == null)
            throw new ThingsboardException(DAMTOM_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

        DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();
        if (deviceProfile.getProfileData().getAlarms() == null)
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        for (DeviceProfileAlarm deviceProfileAlarm : deviceProfile.getProfileData().getAlarms()) {
            if (deviceProfileAlarm.getAlarmType().equals(AlarmNamePrefixConstant.ALARM_PREFIX + alarmType)) {
                deviceProfileAlarm.setAlarmType(alarmType);
                return new ResponseEntity<>(deviceProfileAlarm, HttpStatus.OK);
            }
        }
        throw new ThingsboardException(ALARM_RULE_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }

    /**
     * @param damTomId
     * @return
     * @throws ThingsboardException
     * @author viettd
     */
    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_TLLUATCANHBAO + "\")")
    @RequestMapping(value = "/damtom-alarm/{damTomId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DeviceProfileData> getAlarmsByDamTomId(@PathVariable("damTomId") UUID damTomId)
            throws ThingsboardException {
        UUID tenantId = getCurrentUser().getTenantId().getId();
        List<DeviceProfileEntity> deviceEntityList = this.damTomAlarmService.getDeviceProfileEntityList(tenantId, damTomId);
        if (deviceEntityList == null && deviceEntityList.isEmpty()) {
            throw new ThingsboardException(ALARM_RULE_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
        DeviceProfileData deviceProfileData = deviceEntityList.get(0).toData().getProfileData();
        return new ResponseEntity<>(deviceProfileData, HttpStatus.OK);


    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_TLLUATCANHBAO + "\")")
    @RequestMapping(value = "/damtom-alarm/v2/{damTomId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<DeviceProfileAlarm>> getAllRpcByDamTomId(@PathVariable("damTomId") UUID damTomId)
            throws ThingsboardException {
        UUID tenantId = getCurrentUser().getTenantId().getId();
        DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damTomId);

        if (damTomEntity == null)
            throw new ThingsboardException(DAMTOM_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        List<DeviceProfileAlarm> alarmList = new ArrayList<>();
        DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();
        if (deviceProfile.getProfileData().getAlarms() == null)
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        for (int i = 0; i < deviceProfile.getProfileData().getAlarms().size(); i++) {
            if (deviceProfile.getProfileData().getAlarms().get(i).getDftAlarmRule().isRpcAlarm()) {
//        deviceProfile.getProfileData().getAlarms().remove(deviceProfile.getProfileData().getAlarms().get(i));
            } else {
                deviceProfile.getProfileData().getAlarms().get(i).setAlarmType(deviceProfile.getProfileData().getAlarms().get(i).getAlarmType().replace(AlarmNamePrefixConstant.ALARM_PREFIX, ""));
                alarmList.add(deviceProfile.getProfileData().getAlarms().get(i));
            }

        }
        return new ResponseEntity<>(alarmList, HttpStatus.OK);


    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_TLLUATCANHBAO
                    + "\")")
    @RequestMapping(value = "/alarm-dt/v2", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> saveAlarmDamTomv2(@RequestBody AlarmDto alarmDto)
            throws ThingsboardException, JsonProcessingException {

        UUID tenantId = getCurrentUser().getTenantId().getId();
        UUID userId = getCurrentUser().getId().getId();

        DamTomAlarmEntity damTomAlarmEntity2 = new DamTomAlarmEntity();
        damTomAlarmEntity2.setId(UUID.randomUUID());
        damTomAlarmEntity2.setTenantId(tenantId);
        damTomAlarmEntity2.setDamtomId(alarmDto.getDamtomId());
        damTomAlarmEntity2.setName(alarmDto.getName());
        damTomAlarmEntity2.setNote(alarmDto.getNote());
        damTomAlarmEntity2.setViaSms(alarmDto.isViaSms());
        damTomAlarmEntity2.setViaEmail(alarmDto.isViaEmail());
        damTomAlarmEntity2.setViaNotification(alarmDto.isViaNotification());
        damTomAlarmEntity2.setCreatedBy(userId);
        damTomAlarmEntity2.setCreatedTime(System.currentTimeMillis());
        damTomAlarmEntity2.setAlarmId(alarmDto.getAlarmId());
        //    if (!alarmDto.getThucHienDieuKhien().isEmpty()) {
        //      damTomAlarmEntity2.setGroupRpcId(UUID.fromString(alarmDto.getThucHienDieuKhien()));
        //    }

        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(alarmDto.getProfile_Data());
        JsonNode jsonNode = objectMapper.readTree(alarmDto.getProfile_Data());
        DamTomAlarmEntity savedDamtomAlarm = new DamTomAlarmEntity();

        List<DeviceProfileEntity> deviceProfileEntityList =
                damTomAlarmService.getDeviceProfileEntityList(tenantId, alarmDto.getDamtomId());
        for (DeviceProfileEntity deviceProfileEntity : deviceProfileEntityList) {
            deviceProfileEntity.setProfileData(jsonNode);
            DeviceProfile savedDeviceProfile =
                    checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfileEntity.toData()));
            damTomAlarmEntity2.setDeviceProfile(deviceProfileEntity);
            tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
            tbClusterService.onEntityStateChange(
                    getCurrentUser().getTenantId(),
                    savedDeviceProfile.getId(),
                    ComponentLifecycleEvent.CREATED);
            savedDamtomAlarm = damTomAlarmService.saveDamTomAlarm(damTomAlarmEntity2);
            damTomAlarmEntity2.setId(UUID.randomUUID());
            System.out.println(damTomAlarmEntity2.getAlarmId() + "" + damTomAlarmEntity2.getName());
        }
        return new ResponseEntity<>(damTomAlarmEntity2, HttpStatus.OK);
    }


    //  New Create, edit, delete API - v2
    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_TLLUATCANHBAO
                    + "\")")
    @RequestMapping(value = "/dt-alarm-v2", method = RequestMethod.POST)
    @ResponseBody
    public synchronized ResponseEntity<?> createOrEditDamtomAlarm(@Valid @RequestBody AlarmDtoV2 alarmDtoV2)
            throws ThingsboardException {
        try {
            UUID tenantId = getCurrentUser().getTenantId().getId();

            boolean isCreate = (alarmDtoV2.getDeviceProfileAlarm().getId() == null);

            // get damtom :
            DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, alarmDtoV2.getDamtomId());

            if (damTomEntity == null)
                throw new ThingsboardException(DAMTOM_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

            // alarmtype = null
            if (alarmDtoV2.getDeviceProfileAlarm().getAlarmType() == null || alarmDtoV2.getDeviceProfileAlarm().getAlarmType().isEmpty())
                throw new ThingsboardException(ALARM_RULE_NAME_CANNOT_BE_NULL, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

            // trim alarmType
            alarmDtoV2.getDeviceProfileAlarm().setAlarmType(alarmDtoV2.getDeviceProfileAlarm().getAlarmType().trim());

            String actualAlarmType = alarmDtoV2.getDeviceProfileAlarm().getAlarmType();

            // add alarmType (alarm name) prefix
            alarmDtoV2.getDeviceProfileAlarm()
                    .setAlarmType(AlarmNamePrefixConstant.ALARM_PREFIX + alarmDtoV2.getDeviceProfileAlarm().getAlarmType());

            DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();

            if (isCreate) {
                // create :
                alarmDtoV2.getDeviceProfileAlarm().setId(UUID.randomUUID().toString());
                alarmDtoV2.getDeviceProfileAlarm().getDftAlarmRule().setCreatedTime(System.currentTimeMillis());

                if (deviceProfile.getProfileData().getAlarms() == null)
                    deviceProfile.getProfileData().setAlarms(new ArrayList<>()); // prevent NullPointerException

                // check name exist
                for (DeviceProfileAlarm x : deviceProfile.getProfileData().getAlarms()) {
                    if (x.getAlarmType().equals(alarmDtoV2.getDeviceProfileAlarm().getAlarmType()))
                        throw new ThingsboardException(ALARM_RULE_NAME_ALREADY_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }

                // update profile data
                deviceProfile.getProfileData().getAlarms().add(alarmDtoV2.getDeviceProfileAlarm());

                DeviceProfile savedDeviceProfile =
                        checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
                tbClusterService.onEntityStateChange(getTenantId(), savedDeviceProfile.getId(),
                        ComponentLifecycleEvent.UPDATED);

                // write log :
                alarmDtoV2.getDeviceProfileAlarm().setAlarmType(actualAlarmType);// change name
                AlarmRuleLogV2 alarmRuleLogV2 = new AlarmRuleLogV2(
                        new AlarmRuleLogIdV2(UUID.fromString(alarmDtoV2.getDeviceProfileAlarm().getId())),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        new DamTomLogId(alarmDtoV2.getDamtomId()),
                        alarmDtoV2.getDeviceProfileAlarm()
                );
                logEntityAction(alarmRuleLogV2.getId(), alarmRuleLogV2, getCurrentUser().getCustomerId(), ActionType.ADDED, null);

                return ResponseEntity.ok(savedDeviceProfile);
            } else {
                // update
                // find alarm :
                if (deviceProfile.getProfileData().getAlarms() == null)
                    deviceProfile.getProfileData().setAlarms(new ArrayList<>());// prevent NullPointerException

                List<DeviceProfileAlarm> deviceProfileAlarmList = deviceProfile.getProfileData().getAlarms();

                Optional<DeviceProfileAlarm> o = deviceProfileAlarmList.stream()
                        .filter(x -> x.getId().equals(alarmDtoV2.getDeviceProfileAlarm().getId()))
                        .findFirst();
                if (!o.isPresent())
                    throw new ThingsboardException(ALARM_RULE_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

                // check trung ten
                boolean isNameExist = deviceProfileAlarmList.stream()
                        .anyMatch(x ->
                                !x.getId().equals(o.get().getId())
                                        && alarmDtoV2.getDeviceProfileAlarm().getAlarmType().equals(x.getAlarmType()));
                if (isNameExist)
                    throw new ThingsboardException(ALARM_RULE_NAME_ALREADY_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

                // set createdTime from o back to alarmDtoV2
                alarmDtoV2.getDeviceProfileAlarm().getDftAlarmRule().setCreatedTime(o.get().getDftAlarmRule().getCreatedTime());

                // update profile data:
                deviceProfileAlarmList.removeIf(x -> x.getId().equals(o.get().getId()));
                deviceProfileAlarmList.add(alarmDtoV2.getDeviceProfileAlarm());
                deviceProfile.getProfileData().setAlarms(deviceProfileAlarmList);

                DeviceProfile savedDeviceProfile =
                        checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
                tbClusterService.onEntityStateChange(getTenantId(), savedDeviceProfile.getId(),
                        ComponentLifecycleEvent.UPDATED);

                // write log :
                alarmDtoV2.getDeviceProfileAlarm().setAlarmType(actualAlarmType);
                AlarmRuleLogV2 alarmRuleLogV2 = new AlarmRuleLogV2(
                        new AlarmRuleLogIdV2(UUID.fromString(alarmDtoV2.getDeviceProfileAlarm().getId())),
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        new DamTomLogId(alarmDtoV2.getDamtomId()),
                        alarmDtoV2.getDeviceProfileAlarm()
                );
                logEntityAction(alarmRuleLogV2.getId(), alarmRuleLogV2, getCurrentUser().getCustomerId(), ActionType.UPDATED, null);

                return ResponseEntity.ok(savedDeviceProfile);
            }
        } catch (Exception e) {
            // write log
            AlarmRuleLogV2 alarmRuleLogV2 = new AlarmRuleLogV2(
                    null,
                    getTenantId(),
                    getCurrentUser().getCustomerId(),
                    new DamTomLogId(alarmDtoV2.getDamtomId()),
                    alarmDtoV2.getDeviceProfileAlarm()
            );
            logEntityAction(emptyId(EntityType.ALARM_RULE),
                    alarmRuleLogV2,
                    null,
                    alarmDtoV2.getDeviceProfileAlarm().getId() == null
                            ? ActionType.ADDED : ActionType.UPDATED,
                    e);

            throw handleException(e);
        }
    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_TLLUATCANHBAO
                    + "\")")
    @RequestMapping(value = "/dt-alarm-v2/multi", method = RequestMethod.POST)
    public synchronized ResponseEntity<?> createMultiAlarm(@Valid @RequestBody MultiAlarmDtoV2 multiDto)
            throws ThingsboardException {
        try {
            UUID tenantId = getCurrentUser().getTenantId().getId();

            // get damtom :
            DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, multiDto.getDamtomId());

            if (damTomEntity == null)
                throw new ThingsboardException(DAMTOM_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

            // alarmtype = null
            for(DeviceProfileAlarm x: multiDto.getAlarmList()){
                if(x.getAlarmType() == null || x.getAlarmType().isEmpty()){
                    throw new ThingsboardException(ALARM_RULE_NAME_CANNOT_BE_NULL, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
                x.setId(UUID.randomUUID().toString());
                x.setAlarmType(AlarmNamePrefixConstant.ALARM_PREFIX + x.getAlarmType());
                x.getDftAlarmRule().setCreatedTime(System.currentTimeMillis());
            };

            DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();

            // check input same name :
            List<String> inputAlarmTypeList = multiDto.getAlarmList().stream()
                    .map(DeviceProfileAlarm::getAlarmType)
                    .collect(Collectors.toList());
            Set<String> set = new HashSet<>(inputAlarmTypeList);
            if(inputAlarmTypeList.size() != set.size())
                throw new ThingsboardException(ALARM_RULE_NAME_CAN_NOT_BE_DUPLICATE, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

            // check name exist :
            List<String> currentAlarmTypeList = deviceProfile.getProfileData().getAlarms().stream()
                    .map(DeviceProfileAlarm::getAlarmType)
                    .collect(Collectors.toList());

            for(String c : currentAlarmTypeList){
                for (String i : inputAlarmTypeList){
                    if(i.equals(c))
                        throw new ThingsboardException(ALARM_RULE_NAME_ALREADY_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
            }

            deviceProfile.getProfileData().getAlarms().addAll(multiDto.getAlarmList());

            // save
            DeviceProfile savedDeviceProfile =
                    checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

            tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
            tbClusterService.onEntityStateChange(getTenantId(), savedDeviceProfile.getId(),
                    ComponentLifecycleEvent.UPDATED);

            // log
           for(DeviceProfileAlarm x : multiDto.getAlarmList()) {
               x.setAlarmType(x.getAlarmType().substring(AlarmNamePrefixConstant.ALARM_PREFIX.length()));// change name
               AlarmRuleLogV2 alarmRuleLogV2 = new AlarmRuleLogV2(
                       new AlarmRuleLogIdV2(UUID.fromString(x.getId())),
                       getTenantId(),
                       getCurrentUser().getCustomerId(),
                       new DamTomLogId(multiDto.getDamtomId()),
                       x
               );
               logEntityAction(alarmRuleLogV2.getId(), alarmRuleLogV2, getCurrentUser().getCustomerId(), ActionType.ADDED, null);
           };

            return ResponseEntity.ok(savedDeviceProfile);
        } catch (Exception e) {
            for(DeviceProfileAlarm x : multiDto.getAlarmList()){
                AlarmRuleLogV2 alarmRuleLogV2 = new AlarmRuleLogV2(
                        null,
                        getTenantId(),
                        getCurrentUser().getCustomerId(),
                        new DamTomLogId(multiDto.getDamtomId()),
                        x
                );
                logEntityAction(emptyId(EntityType.ALARM_RULE),
                        alarmRuleLogV2,
                        null,
                        ActionType.ADDED,
                        e);
            }
            throw handleException(e);
        }
    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_TLLUATCANHBAO
                    + "\")")
    @DeleteMapping("/dt-alarm-v2")
    public synchronized ResponseEntity<?> deleteDamtomAlarmV2(
            @RequestParam("damTomId") UUID damTomId,
            @RequestParam("alarmId") UUID alarmId)
            throws ThingsboardException {
        try {
            UUID tenantId = getTenantId().getId();

            // find damtom
            DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damTomId);
            if (damTomEntity == null)
                throw new ThingsboardException(DAMTOM_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

            DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();

            List<DeviceProfileAlarm> deviceProfileAlarmList = deviceProfile.getProfileData().getAlarms();

            if (deviceProfileAlarmList == null)
                throw new ThingsboardException(ALARM_RULE_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

            // remove alarm :
            Optional<DeviceProfileAlarm> o = deviceProfileAlarmList.stream()
                    .filter(x -> x.getId().equals(alarmId.toString()))
                    .findFirst();
            if (!o.isPresent())
                throw new ThingsboardException(ALARM_RULE_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            deviceProfileAlarmList.remove(o.get());

            // save
            deviceProfile.getProfileData().setAlarms(deviceProfileAlarmList);

            DeviceProfile savedDeviceProfile =
                    checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

            tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
            tbClusterService.onEntityStateChange(getTenantId(), savedDeviceProfile.getId(),
                    ComponentLifecycleEvent.UPDATED);

            // write log
            o.get().setAlarmType(o.get().getAlarmType().substring(AlarmNamePrefixConstant.ALARM_PREFIX.length()));
            AlarmRuleLogV2 alarmRuleLogV2 = new AlarmRuleLogV2(
                    new AlarmRuleLogIdV2(alarmId),
                    getTenantId(),
                    getCurrentUser().getCustomerId(),
                    new DamTomLogId(damTomId),
                    o.get()
            );
            logEntityAction(alarmRuleLogV2.getId(), alarmRuleLogV2, getCurrentUser().getCustomerId(),
                    ActionType.DELETED, null, alarmId.toString());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_TLLUATCANHBAO
                    + "\")")
    @PostMapping("/dt-alarm-v2/alarm-type-exist")
    public ResponseEntity<?> checkAlarmTypeExist(
            @Valid @RequestBody CheckNameExistDto dto)
            throws ThingsboardException {
        try {
            DamTomEntity damTomEntity = damTomService.getDamTomById(getTenantId().getId(), dto.getDamtomId());
            if (damTomEntity == null)
                throw new ThingsboardException(DAMTOM_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

            DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();

            List<DeviceProfileAlarm> deviceProfileAlarmList = deviceProfile.getProfileData().getAlarms();

            if (deviceProfileAlarmList != null && !deviceProfileAlarmList.isEmpty()) {
                for (DeviceProfileAlarm alarm : deviceProfileAlarmList) {
                    for (String inputAlarmType : dto.getAlarmTypes()) {
                        if (alarm.getAlarmType().equals(AlarmNamePrefixConstant.ALARM_PREFIX + inputAlarmType)) {
                            return ResponseEntity.ok(new CheckNameExistRespDto(true, inputAlarmType));
                        }
                    }
                }
            }
            return ResponseEntity.ok(new CheckNameExistRespDto(false, ""));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
