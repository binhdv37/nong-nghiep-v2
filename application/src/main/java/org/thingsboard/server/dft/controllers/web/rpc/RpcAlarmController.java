package org.thingsboard.server.dft.controllers.web.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.AlarmNamePrefixConstant;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.controllers.web.Alarm.dtos.AlarmDtoV2;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class RpcAlarmController extends BaseController {
    private static final String DATOM_DOES_NOT_EXIST = "Nhà vườn không tồn tại";
    private static final String ALARM_RULE_NAME_ALREADY_EXIST = "Tên điều khiển tự động đã tồn tại!";
    private static final String ALARM_RULE_DOES_NOT_EXIST = "Luật điều khiển không tồn tại";

    @Autowired
    protected DamTomService damTomService;

    /**
     * @param damTomId
     * @return
     * @throws ThingsboardException
     * @author viettd
     */

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_TLLUATCANHBAO + "\")")
    @RequestMapping(value = "/damtom-rpc-alarm/{damTomId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getAllRpcByDamTomId(@PathVariable("damTomId") UUID damTomId)
            throws ThingsboardException {
        UUID tenantId = getCurrentUser().getTenantId().getId();
        DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damTomId);

        if (damTomEntity == null)
//            throw new ThingsboardException(DATOM_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DATOM_DOES_NOT_EXIST);
        List<DeviceProfileAlarm> alarmList = new ArrayList<>();
        DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();
        if (deviceProfile.getProfileData().getAlarms() == null)
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        for (int i = 0; i < deviceProfile.getProfileData().getAlarms().size(); i++) {
            if (deviceProfile.getProfileData().getAlarms().get(i).getDftAlarmRule().isRpcAlarm()) {
//        deviceProfile.getProfileData().getAlarms().remove(deviceProfile.getProfileData().getAlarms().get(i));
                deviceProfile.getProfileData().getAlarms().get(i).setAlarmType(deviceProfile.getProfileData().getAlarms().get(i).getAlarmType().replace(AlarmNamePrefixConstant.RPC_PREFIX, ""));
                alarmList.add(deviceProfile.getProfileData().getAlarms().get(i));
            }

        }
        Collections.sort(alarmList, (o1, o2) -> o1.getDftAlarmRule().getCreatedTime() <= o2.getDftAlarmRule().getCreatedTime() ? 1 : -1);
        return new ResponseEntity<>(alarmList, HttpStatus.OK);


    }

    /**
     * @param damTomId
     * @param alarmType
     * @return
     * @throws ThingsboardException
     * @author viettd
     */
    @GetMapping("/damtom-rpc-alarm")
    @ResponseBody
    public ResponseEntity<?> getRpcOfDamTomInDeviceProfile(@RequestParam("damTomId") UUID damTomId,
                                                                            @RequestParam("alarmType") String alarmType)
            throws ThingsboardException {

        UUID tenantId = getCurrentUser().getTenantId().getId();
        DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damTomId);

        if (damTomEntity == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DATOM_DOES_NOT_EXIST);
//            throw new ThingsboardException(DATOM_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

        DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();
        if (deviceProfile.getProfileData().getAlarms() == null)
            return new ResponseEntity<>(null, HttpStatus.OK);
        for (DeviceProfileAlarm deviceProfileAlarm : deviceProfile.getProfileData().getAlarms()) {
            if (deviceProfileAlarm.getAlarmType().equals(AlarmNamePrefixConstant.RPC_PREFIX + alarmType)) {
                deviceProfileAlarm.setAlarmType(alarmType);
                return new ResponseEntity<>(deviceProfileAlarm, HttpStatus.OK);
            }
        }

//        throw new ThingsboardException(ALARM_RULE_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ALARM_RULE_DOES_NOT_EXIST);
    }

    //  New Create, edit, delete API - v2
    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_TLLUATCANHBAO
                    + "\")")
    @RequestMapping(value = "/damtom-rpc-alarm", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> createOrEditDamtomAlarmRpc(@Valid @RequestBody AlarmDtoV2 alarmDtoV2)
            throws ThingsboardException {
        try {
            UUID tenantId = getCurrentUser().getTenantId().getId();

            // get damtom :
            alarmDtoV2.getDeviceProfileAlarm().getDftAlarmRule().setRpcAlarm(true);
            DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, alarmDtoV2.getDamtomId());
            List<UUID> rpsIds = alarmDtoV2.getDeviceProfileAlarm().getDftAlarmRule().getRpcSettingIds();
            List<UUID> groupRpcIds = alarmDtoV2.getDeviceProfileAlarm().getDftAlarmRule().getGroupRpcIds();
            if (damTomEntity == null)
//                throw new ThingsboardException(DATOM_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DATOM_DOES_NOT_EXIST);
            if (rpsIds == null && groupRpcIds == null)
//                throw new ThingsboardException("RpcSettingIds hoặc GroupRpcIds phải có giá trị", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("RpcSettingIds hoặc GroupRpcIds phải có giá trị");
            if (rpsIds.isEmpty() && groupRpcIds.isEmpty())
//                throw new ThingsboardException("RpcSettingIds hoặc GroupRpcIds phải có giá trị", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("RpcSettingIds hoặc GroupRpcIds phải có giá trị");
            // add alarmType (alarm name) prefix
            alarmDtoV2.getDeviceProfileAlarm()
                    .setAlarmType(AlarmNamePrefixConstant.RPC_PREFIX + alarmDtoV2.getDeviceProfileAlarm().getAlarmType());

            alarmDtoV2.getDeviceProfileAlarm().getDftAlarmRule().setCreatedTime(System.currentTimeMillis());
            DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();

            if (alarmDtoV2.getDeviceProfileAlarm().getId() == null) {
                // create :
                alarmDtoV2.getDeviceProfileAlarm().setId(UUID.randomUUID().toString());

                if (deviceProfile.getProfileData().getAlarms() == null)
                    deviceProfile.getProfileData().setAlarms(new ArrayList<>()); // prevent NullPointerException

                // check name exist
                for (DeviceProfileAlarm x : deviceProfile.getProfileData().getAlarms()) {
                    if (x.getAlarmType().equals(alarmDtoV2.getDeviceProfileAlarm().getAlarmType()))
//                        throw new ThingsboardException(ALARM_RULE_NAME_ALREADY_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ALARM_RULE_NAME_ALREADY_EXIST);

                }

                // update profile data
                deviceProfile.getProfileData().getAlarms().add(alarmDtoV2.getDeviceProfileAlarm());

                DeviceProfile savedDeviceProfile =
                        checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
                tbClusterService.onEntityStateChange(getTenantId(), savedDeviceProfile.getId(),
                        ComponentLifecycleEvent.UPDATED);

                // ghi log :

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
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ALARM_RULE_DOES_NOT_EXIST);

//                    throw new ThingsboardException(ALARM_RULE_DOES_NOT_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

                // check trung ten
                boolean isNameExist = deviceProfileAlarmList.stream()
                        .anyMatch(x ->
                                !x.getId().equals(o.get().getId())
                                        && alarmDtoV2.getDeviceProfileAlarm().getAlarmType().equals(x.getAlarmType()));
                if (isNameExist)
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ALARM_RULE_NAME_ALREADY_EXIST);
//                    throw new ThingsboardException(ALARM_RULE_NAME_ALREADY_EXIST, ThingsboardErrorCode.BAD_REQUEST_PARAMS);

                // update profile data :
                deviceProfileAlarmList.removeIf(x -> x.getId().equals(o.get().getId()));
                deviceProfileAlarmList.add(alarmDtoV2.getDeviceProfileAlarm());
                deviceProfile.getProfileData().setAlarms(deviceProfileAlarmList);

                DeviceProfile savedDeviceProfile =
                        checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

                tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
                tbClusterService.onEntityStateChange(getTenantId(), savedDeviceProfile.getId(),
                        ComponentLifecycleEvent.UPDATED);

                // ghi log :

                return ResponseEntity.ok(savedDeviceProfile);
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize(
            "hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\""
                    + PermissionConstants.PAGES_TLLUATCANHBAO
                    + "\")")
    @DeleteMapping("/damtom-rpc-alarm")
    public void deleteDamtomAlarmV2(
            @RequestParam("damTomId") UUID damTomId,
            @RequestParam("alarmId") UUID alarmId)
            throws ThingsboardException {
        try {
            UUID tenantId = getTenantId().getId();

            // find damtom
            DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damTomId);
            if (damTomEntity == null) return;

            DeviceProfile deviceProfile = damTomEntity.getDeviceProfile().toData();

            List<DeviceProfileAlarm> deviceProfileAlarmList = deviceProfile.getProfileData().getAlarms();

            if (deviceProfileAlarmList == null) return;

            // remove alarm :
            deviceProfileAlarmList = deviceProfileAlarmList.stream()
                    .filter(x -> !x.getId().equals(alarmId.toString()))
                    .collect(Collectors.toList());

            // save
            deviceProfile.getProfileData().setAlarms(deviceProfileAlarmList);

            DeviceProfile savedDeviceProfile =
                    checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

            tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
            tbClusterService.onEntityStateChange(getTenantId(), savedDeviceProfile.getId(),
                    ComponentLifecycleEvent.UPDATED);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
