/**
 * Copyright © 2016-2021 The Thingsboard Authors
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.service.telemetry;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.alarm.*;
import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;
import org.thingsboard.server.common.data.id.AlarmId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.query.AlarmData;
import org.thingsboard.server.common.data.query.AlarmDataQuery;
import org.thingsboard.server.common.msg.queue.ServiceType;
import org.thingsboard.server.common.msg.queue.TbCallback;
import org.thingsboard.server.common.msg.queue.TopicPartitionInfo;
import org.thingsboard.server.dao.alarm.AlarmDao;
import org.thingsboard.server.dao.alarm.AlarmOperationResult;
import org.thingsboard.server.dao.alarm.AlarmService;
import org.thingsboard.server.dao.model.sql.AlarmEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dao.sql.alarm.AlarmRepository;
import org.thingsboard.server.dft.common.constants.EntityConstant;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.GroupRpcStatus;
import org.thingsboard.server.dft.controllers.web.thongbao.ThongBaoService;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.GroupRpcEntity;
import org.thingsboard.server.dft.entities.RpcSettingEntity;
import org.thingsboard.server.dft.repositories.DamTomRepository;
import org.thingsboard.server.dft.repositories.GroupRpcRepository;
import org.thingsboard.server.dft.repositories.RpcSettingRepository;
import org.thingsboard.server.dft.services.lscanhbao.LichSuCanhBaoService;
import org.thingsboard.server.dft.services.rpc.RpcAlarmService;
import org.thingsboard.server.dft.services.rpc.RpcCommandQueueService;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.queue.discovery.PartitionService;
import org.thingsboard.server.service.queue.TbClusterService;
import org.thingsboard.server.service.subscription.SubscriptionManagerService;
import org.thingsboard.server.service.subscription.TbSubscriptionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Created by ashvayka on 27.03.18. */
@Service
@Slf4j
public class DefaultAlarmSubscriptionService extends AbstractSubscriptionService
    implements AlarmSubscriptionService {

  private final AlarmService alarmService;
  private final AlarmRepository alarmRepository;
  private final LichSuCanhBaoService lichSuCanhBaoService;
  private final ThongBaoService thongBaoService;
  private final RpcAlarmService rpcAlarmService;
  private final AlarmDao alarmDao;
  private final DamTomRepository damTomRepository;
  private final RpcCommandQueueService rpcCommandQueueService;
  private final GroupRpcRepository groupRpcRepository;
  private final RpcSettingRepository rpcSettingRepository;

  public DefaultAlarmSubscriptionService(
      TbClusterService clusterService,
      PartitionService partitionService,
      @Lazy AlarmService alarmService,
      @Lazy AlarmRepository alarmRepository,
      @Lazy LichSuCanhBaoService lichSuCanhBaoService,
      @Lazy ThongBaoService thongBaoService,
      @Lazy RpcAlarmService rpcAlarmService,
      @Lazy AlarmDao alarmDao,
      @Lazy DamTomRepository damTomRepository,
      @Lazy RpcCommandQueueService rpcCommandQueueService,
      @Lazy GroupRpcRepository groupRpcRepository,
      @Lazy RpcSettingRepository rpcSettingRepository) {
    super(clusterService, partitionService);
    this.alarmService = alarmService;
    this.alarmRepository = alarmRepository;
    this.lichSuCanhBaoService = lichSuCanhBaoService;
    this.thongBaoService = thongBaoService;
    this.rpcAlarmService = rpcAlarmService;
    this.alarmDao = alarmDao;
    this.damTomRepository = damTomRepository;
    this.rpcCommandQueueService = rpcCommandQueueService;
    this.groupRpcRepository = groupRpcRepository;
    this.rpcSettingRepository = rpcSettingRepository;
  }

  @Autowired(required = false)
  public void setSubscriptionManagerService(
      Optional<SubscriptionManagerService> subscriptionManagerService) {
    this.subscriptionManagerService = subscriptionManagerService;
  }

  @Override
  String getExecutorPrefix() {
    return "alarm";
  }

  // huydv alarm custom here
  @Override
  public Alarm createOrUpdateAlarm(Alarm alarm) {
    AlarmOperationResult result = null;
    DeviceProfileEntity deviceProfile =
        rpcAlarmService.findDeviceProfileByDeviceId(alarm.getOriginator().getId());
    DeviceProfileAlarm deviceProfileAlarm =
        rpcAlarmService.getAlarmRuleByAlarmType(alarm.getOriginator().getId(), alarm.getType());
    if (deviceProfileAlarm.getDftAlarmRule().isActive()) {
      if (!deviceProfileAlarm.getDftAlarmRule().isRpcAlarm()) {
        if (alarm.getUuidId() != null) {
          AlarmEntity currentAlarm = alarmRepository.findById(alarm.getUuidId()).get();
          if (currentAlarm.getStatus().isCleared()) {
            alarm.setCreatedTime(alarm.getEndTs());
            result = alarmService.createOrUpdateAlarm(alarm);
            lichSuCanhBaoService.snapShotCanhBao(alarm, EntityConstant.TYPE_EDIT);
            thongBaoService.sendWarning(alarm, 1);
          } else {
            result = alarmService.createOrUpdateAlarm(alarm);
          }
        } else {
          try {
            Alarm existing =
                alarmDao
                    .findLatestByOriginatorAndType(
                        alarm.getTenantId(), alarm.getOriginator(), alarm.getType())
                    .get();
            if (existing == null || existing.getStatus().isCleared()) {
              result = alarmService.createOrUpdateAlarm(alarm);
              lichSuCanhBaoService.snapShotCanhBao(result.getAlarm(), EntityConstant.TYPE_ADD);
              thongBaoService.sendWarning(alarm, 2);
            } else {
              alarm.setCreatedTime(alarm.getEndTs());
              result = alarmService.createOrUpdateAlarm(alarm);
              lichSuCanhBaoService.snapShotCanhBao(alarm, EntityConstant.TYPE_EDIT);
              thongBaoService.sendWarning(alarm, 1);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        if (result.isSuccessful()) {
          onAlarmUpdated(result);
        }
        return result.getAlarm();
      } else {
        if (deviceProfileAlarm
                .getDftAlarmRule()
                .getGatewayIds()
                .contains(alarm.getOriginator().getId())
            || deviceProfileAlarm.getDftAlarmRule().getRpcSettingIds().isEmpty()) {
          if (deviceProfileAlarm.getDftAlarmRule().getGroupRpcIds().get(0) != null) {
            GroupRpcStatus groupRpcStatus =
                rpcCommandQueueService.checkGroupRpcInProcessing(
                    alarm.getTenantId().getId(),
                    deviceProfileAlarm.getDftAlarmRule().getGroupRpcIds().get(0));
            if (groupRpcStatus.getGroupRpcId() == null) {
              GroupRpcEntity groupRpcEntity =
                  groupRpcRepository
                      .findById(deviceProfileAlarm.getDftAlarmRule().getGroupRpcIds().get(0))
                      .get();
              rpcCommandQueueService.saveRpcCommandByAlarmRule(
                  groupRpcEntity.getTenantId(),
                  groupRpcEntity.getDamTomId(),
                  groupRpcEntity.getRpcSettingEntities().stream().collect(Collectors.toList()));
            }
          }
          if (!deviceProfileAlarm.getDftAlarmRule().getRpcSettingIds().isEmpty()) {
            DamTomEntity damTomEntity =
                damTomRepository.findDamTomEntityByTenantIdAndDeviceProfileId(
                    alarm.getTenantId().getId(), deviceProfile.getId());
            List<RpcSettingEntity> rpcSettingEntityList =
                rpcSettingRepository.findByIdIn(
                    deviceProfileAlarm.getDftAlarmRule().getRpcSettingIds());
            for (RpcSettingEntity rpcSettingEntity : rpcSettingEntityList) {
              boolean isProcessingDevice =
                  rpcCommandQueueService.checkDeviceInProcessing(
                      damTomEntity.getId(),
                      rpcSettingEntity.getDeviceId(),
                      rpcSettingEntity.getId());
              if (isProcessingDevice) {
                rpcSettingEntityList.remove(rpcSettingEntity);
              }
            }
            rpcCommandQueueService.saveRpcCommandByAlarmRule(
                damTomEntity.getTenantId(), damTomEntity.getId(), rpcSettingEntityList);
            result = alarmService.createOrUpdateAlarm(alarm);
            if (result.isSuccessful()) {
              onAlarmUpdated(result);
            }
            return result.getAlarm();
          }
        }
      }
    }
    return alarm;
  }

  @Override
  public Boolean deleteAlarm(TenantId tenantId, AlarmId alarmId) {
    AlarmOperationResult result = alarmService.deleteAlarm(tenantId, alarmId);
    onAlarmDeleted(result);
    return result.isSuccessful();
  }

  @Override
  public ListenableFuture<Boolean> ackAlarm(TenantId tenantId, AlarmId alarmId, long ackTs) {
    ListenableFuture<AlarmOperationResult> result = alarmService.ackAlarm(tenantId, alarmId, ackTs);
    Futures.addCallback(result, new AlarmUpdateCallback(), wsCallBackExecutor);
    return Futures.transform(result, AlarmOperationResult::isSuccessful, wsCallBackExecutor);
  }

  @Override
  public ListenableFuture<Boolean> clearAlarm(
      TenantId tenantId, AlarmId alarmId, JsonNode details, long clearTs) {
    ListenableFuture<AlarmOperationResult> result =
        alarmService.clearAlarm(tenantId, alarmId, details, clearTs);
    Futures.addCallback(result, new AlarmUpdateCallback(), wsCallBackExecutor);
    return Futures.transform(result, AlarmOperationResult::isSuccessful, wsCallBackExecutor);
  }

  @Override
  public ListenableFuture<Alarm> findAlarmByIdAsync(TenantId tenantId, AlarmId alarmId) {
    return alarmService.findAlarmByIdAsync(tenantId, alarmId);
  }

  @Override
  public ListenableFuture<AlarmInfo> findAlarmInfoByIdAsync(TenantId tenantId, AlarmId alarmId) {
    return alarmService.findAlarmInfoByIdAsync(tenantId, alarmId);
  }

  @Override
  public ListenableFuture<PageData<AlarmInfo>> findAlarms(TenantId tenantId, AlarmQuery query) {
    return alarmService.findAlarms(tenantId, query);
  }

  @Override
  public AlarmSeverity findHighestAlarmSeverity(
      TenantId tenantId,
      EntityId entityId,
      AlarmSearchStatus alarmSearchStatus,
      AlarmStatus alarmStatus) {
    return alarmService.findHighestAlarmSeverity(
        tenantId, entityId, alarmSearchStatus, alarmStatus);
  }

  @Override
  public PageData<AlarmData> findAlarmDataByQueryForEntities(
      TenantId tenantId,
      CustomerId customerId,
      AlarmDataQuery query,
      Collection<EntityId> orderedEntityIds) {
    return alarmService.findAlarmDataByQueryForEntities(
        tenantId, customerId, query, orderedEntityIds);
  }

  @Override
  public ListenableFuture<Alarm> findLatestByOriginatorAndType(
      TenantId tenantId, EntityId originator, String type) {
    return alarmService.findLatestByOriginatorAndType(tenantId, originator, type);
  }

  private void onAlarmUpdated(AlarmOperationResult result) {
    wsCallBackExecutor.submit(
        () -> {
          Alarm alarm = result.getAlarm();
          TenantId tenantId = result.getAlarm().getTenantId();
          for (EntityId entityId : result.getPropagatedEntitiesList()) {
            TopicPartitionInfo tpi =
                partitionService.resolve(ServiceType.TB_CORE, tenantId, entityId);
            if (currentPartitions.contains(tpi)) {
              if (subscriptionManagerService.isPresent()) {
                subscriptionManagerService
                    .get()
                    .onAlarmUpdate(tenantId, entityId, alarm, TbCallback.EMPTY);
              } else {
                log.warn("Possible misconfiguration because subscriptionManagerService is null!");
              }
            } else {
              TransportProtos.ToCoreMsg toCoreMsg =
                  TbSubscriptionUtils.toAlarmUpdateProto(tenantId, entityId, alarm);
              clusterService.pushMsgToCore(tpi, entityId.getId(), toCoreMsg, null);
            }
          }
        });
  }

  private void onAlarmDeleted(AlarmOperationResult result) {
    wsCallBackExecutor.submit(
        () -> {
          Alarm alarm = result.getAlarm();
          TenantId tenantId = result.getAlarm().getTenantId();
          for (EntityId entityId : result.getPropagatedEntitiesList()) {
            TopicPartitionInfo tpi =
                partitionService.resolve(ServiceType.TB_CORE, tenantId, entityId);
            if (currentPartitions.contains(tpi)) {
              if (subscriptionManagerService.isPresent()) {
                subscriptionManagerService
                    .get()
                    .onAlarmDeleted(tenantId, entityId, alarm, TbCallback.EMPTY);
              } else {
                log.warn("Possible misconfiguration because subscriptionManagerService is null!");
              }
            } else {
              TransportProtos.ToCoreMsg toCoreMsg =
                  TbSubscriptionUtils.toAlarmDeletedProto(tenantId, entityId, alarm);
              clusterService.pushMsgToCore(tpi, entityId.getId(), toCoreMsg, null);
            }
          }
        });
  }

  private class AlarmUpdateCallback implements FutureCallback<AlarmOperationResult> {
    @Override
    public void onSuccess(@Nullable AlarmOperationResult result) {
      onAlarmUpdated(result);
    }

    @Override
    public void onFailure(Throwable t) {
      log.warn("Failed to update alarm", t);
    }
  }
}
