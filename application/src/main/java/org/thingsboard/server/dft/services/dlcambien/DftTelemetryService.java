package org.thingsboard.server.dft.services.dlcambien;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.data.alarm.AlarmSeverity;
import org.thingsboard.server.common.data.device.profile.AlarmRule;
import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.query.*;
import org.thingsboard.server.dao.device.DeviceProfileService;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.sql.device.DeviceRepository;
import org.thingsboard.server.dft.services.dlcambien.dtos.AlarmCondition;
import org.thingsboard.server.dft.services.dlcambien.dtos.DftAlarmRule;
import org.thingsboard.server.dft.services.dlcambien.dtos.DftTelemetry;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DftTelemetryService {

  private final DeviceProfileService deviceProfileService;

  private final DeviceRepository deviceRepository;

  @Autowired
  public DftTelemetryService(
      DeviceProfileService deviceProfileService, DeviceRepository deviceRepository) {
    this.deviceProfileService = deviceProfileService;
    this.deviceRepository = deviceRepository;
  }

  public List<DftAlarmRule> getListAlarmByProfileAndKeys(DeviceProfile deviceProfile, String key) {
    List<DftAlarmRule> dftAlarmRules = new ArrayList<>();
    if (deviceProfile.getProfileData().getAlarms() != null) {
      List<DeviceProfileAlarm> deviceProfileAlarms =
              deviceProfile.getProfileData().getAlarms().stream()
                      .filter(
                              deviceProfileAlarm ->
                                      deviceProfileAlarm.getDftAlarmRule().isActive()
                                              && !deviceProfileAlarm.getDftAlarmRule().isRpcAlarm())
                      .collect(Collectors.toList());
      if (!deviceProfileAlarms.isEmpty()) {
        for (DeviceProfileAlarm deviceProfileAlarm : deviceProfileAlarms) {
          if (deviceProfileAlarm.getCreateRules().get(AlarmSeverity.CRITICAL) != null) {
            AlarmRule alarmRule = deviceProfileAlarm.getCreateRules().get(AlarmSeverity.CRITICAL);
            List<KeyFilter> keyFilterList = alarmRule.getCondition().getCondition();
            for (KeyFilter keyFilter : keyFilterList) {
              if (keyFilter.getKey().getKey().equalsIgnoreCase(key)) {
                if (keyFilter.getPredicate().getType().equals(FilterPredicateType.NUMERIC)) {
                  NumericFilterPredicate numericFilterPredicate =
                      (NumericFilterPredicate) keyFilter.getPredicate();
                  DftAlarmRule dftAlarmRule = new DftAlarmRule();
                  dftAlarmRule.setKey(keyFilter.getKey().getKey());
                  dftAlarmRule.setValue(numericFilterPredicate.getValue().getValue());
                  if (numericFilterPredicate
                      .getOperation()
                      .equals(NumericFilterPredicate.NumericOperation.EQUAL)) {
                    dftAlarmRule.setOperation(AlarmCondition.EQUAL);
                  } else if (numericFilterPredicate
                      .getOperation()
                      .equals(NumericFilterPredicate.NumericOperation.NOT_EQUAL)) {
                    dftAlarmRule.setOperation(AlarmCondition.NOT_EQUAL);
                  } else if (numericFilterPredicate
                      .getOperation()
                      .equals(NumericFilterPredicate.NumericOperation.GREATER)) {
                    dftAlarmRule.setOperation(AlarmCondition.GREATER);
                  } else if (numericFilterPredicate
                      .getOperation()
                      .equals(NumericFilterPredicate.NumericOperation.LESS)) {
                    dftAlarmRule.setOperation(AlarmCondition.LESS);
                  } else if (numericFilterPredicate
                      .getOperation()
                      .equals(NumericFilterPredicate.NumericOperation.GREATER_OR_EQUAL)) {
                    dftAlarmRule.setOperation(AlarmCondition.GREATER_OR_EQUAL);
                  } else if (numericFilterPredicate
                      .getOperation()
                      .equals(NumericFilterPredicate.NumericOperation.LESS_OR_EQUAL)) {
                    dftAlarmRule.setOperation(AlarmCondition.LESS_OR_EQUAL);
                  }
                  dftAlarmRules.add(dftAlarmRule);
                } else if (keyFilter.getPredicate().getType().equals(FilterPredicateType.COMPLEX)) {
                  List<KeyFilterPredicate> keyFilterPredicates =
                      ((ComplexFilterPredicate) keyFilter.getPredicate()).getPredicates();
                  for (KeyFilterPredicate keyFilterPredicate : keyFilterPredicates) {
                    NumericFilterPredicate numericFilterPredicate =
                        (NumericFilterPredicate) keyFilterPredicate;
                    DftAlarmRule dftAlarmRule = new DftAlarmRule();
                    dftAlarmRule.setKey(keyFilter.getKey().getKey());
                    dftAlarmRule.setValue(numericFilterPredicate.getValue().getValue());
                    if (numericFilterPredicate
                        .getOperation()
                        .equals(NumericFilterPredicate.NumericOperation.EQUAL)) {
                      dftAlarmRule.setOperation(AlarmCondition.EQUAL);
                    } else if (numericFilterPredicate
                        .getOperation()
                        .equals(NumericFilterPredicate.NumericOperation.NOT_EQUAL)) {
                      dftAlarmRule.setOperation(AlarmCondition.NOT_EQUAL);
                    } else if (numericFilterPredicate
                        .getOperation()
                        .equals(NumericFilterPredicate.NumericOperation.GREATER)) {
                      dftAlarmRule.setOperation(AlarmCondition.GREATER);
                    } else if (numericFilterPredicate
                        .getOperation()
                        .equals(NumericFilterPredicate.NumericOperation.LESS)) {
                      dftAlarmRule.setOperation(AlarmCondition.LESS);
                    } else if (numericFilterPredicate
                        .getOperation()
                        .equals(NumericFilterPredicate.NumericOperation.GREATER_OR_EQUAL)) {
                      dftAlarmRule.setOperation(AlarmCondition.GREATER_OR_EQUAL);
                    } else if (numericFilterPredicate
                        .getOperation()
                        .equals(NumericFilterPredicate.NumericOperation.LESS_OR_EQUAL)) {
                      dftAlarmRule.setOperation(AlarmCondition.LESS_OR_EQUAL);
                    }
                    dftAlarmRules.add(dftAlarmRule);
                  }
                }
              }
            }
          }
        }
      }
    }
    return dftAlarmRules;
  }

  public Map<String, List<DftTelemetry>> toDftTelemetry(
      TenantId tenantId, UUID deviceId, List<TsKvEntry> tsKvEntryList) {
    DeviceEntity deviceEntity = deviceRepository.findByTenantIdAndId(tenantId.getId(), deviceId);
    DeviceProfile deviceProfile =
        deviceProfileService.findDeviceProfileById(
            tenantId, new DeviceProfileId(deviceEntity.getDeviceProfileId()));
    Map<String, List<DftTelemetry>> mapTelemetry = new HashMap<>();
    for (TsKvEntry tsKvEntry : tsKvEntryList) {
      List<DftAlarmRule> dftAlarmRules =
          getListAlarmByProfileAndKeys(deviceProfile, tsKvEntry.getKey());
      List<DftTelemetry> dftTelemetries = new ArrayList<>();
      DftTelemetry dftTelemetry = new DftTelemetry();
      dftTelemetry.setTs(tsKvEntry.getTs());
      dftTelemetry.setDeviceId(deviceId);
      if (tsKvEntry.getDoubleValue().isPresent()) {
        dftTelemetry.setValue(tsKvEntry.getDoubleValue().get());
        dftTelemetry.setAlarm(
            reCheckTelemetryWithAlarmRule(dftAlarmRules, tsKvEntry.getDoubleValue().get()));
      } else if (tsKvEntry.getLongValue().isPresent()) {
        dftTelemetry.setValue(tsKvEntry.getLongValue().get());
        dftTelemetry.setAlarm(
            reCheckTelemetryWithAlarmRule(dftAlarmRules, (double) tsKvEntry.getLongValue().get()));
      }

      dftTelemetries.add(dftTelemetry);
      mapTelemetry.put(tsKvEntry.getKey(), dftTelemetries);
    }
    return mapTelemetry;
  }

  private boolean reCheckTelemetryWithAlarmRule(List<DftAlarmRule> alarmRules, Double value) {
    for (DftAlarmRule alarmRule : alarmRules) {
      if (checkTelemetry(alarmRule.getOperation(), value, alarmRule.getValue())) {
        return true;
      }
    }
    return false;
  }

  private boolean checkTelemetry(String condition, Double valueTelemetry, Double valueAlarm) {
    switch (condition) {
      case AlarmCondition.NOT_EQUAL:
        return !valueTelemetry.equals(valueAlarm);
      case AlarmCondition.EQUAL:
        return valueTelemetry.equals(valueAlarm);
      case AlarmCondition.GREATER:
        return valueTelemetry > valueAlarm;
      case AlarmCondition.GREATER_OR_EQUAL:
        return valueTelemetry >= valueAlarm;
      case AlarmCondition.LESS:
        return valueTelemetry < valueAlarm;
      case AlarmCondition.LESS_OR_EQUAL:
        return valueTelemetry <= valueAlarm;
      default:
        throw new RuntimeException("Operation not supported: " + condition);
    }
  }
}
