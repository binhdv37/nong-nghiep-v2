package org.thingsboard.server.dft.services.rpc;

import com.google.common.util.concurrent.ListenableFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.DataType;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.dao.attributes.AttributesService;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.timeseries.TimeseriesLatestDao;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.ThietBiDieuKhien;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;
import org.thingsboard.server.dft.repositories.DftTelemetryViewRepository;
import org.thingsboard.server.dft.services.dlcambien.DuLieuCamBienService;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ThietBiDieuKhienService {

  //  private static final String getValueMethod = "getValueMethod";
  private static final String setValueMethod = "setValueMethod";

  public static final String INACTIVITY_ALARM_TIME = "inactivityAlarmTime";
  public static final String LAST_CONNECTION_TIME = "lastConnectTime";
  public static final String ACTIVE = "active";

  @Autowired private TimeseriesLatestDao timeseriesLatestDao;

  @Value("${state.defaultInactivityTimeoutInSec}")
  @Getter
  private long defaultInactivityTimeoutInSec;

  private final DuLieuCamBienService duLieuCamBienService;
  private final AttributesService attributesService;
  private final DftDeviceRepository dftDeviceRepository;
  private final DftTelemetryViewRepository dftTelemetryViewRepository;

  @Autowired
  public ThietBiDieuKhienService(
      DuLieuCamBienService duLieuCamBienService,
      AttributesService attributesService,
      DftDeviceRepository dftDeviceRepository,
      DftTelemetryViewRepository dftTelemetryViewRepository) {
    this.duLieuCamBienService = duLieuCamBienService;
    this.attributesService = attributesService;
    this.dftDeviceRepository = dftDeviceRepository;
    this.dftTelemetryViewRepository = dftTelemetryViewRepository;
  }

  public List<DeviceEntity> getAllGatewayFromDamTom(UUID tenantId, UUID damTomId) {
    return duLieuCamBienService.getAllGatewayFromDamTom(tenantId, damTomId);
  }

  public List<DeviceEntity> getAllRpcDeviceFromGateway(UUID gatewayId) {
    List<DeviceEntity> deviceEntities = duLieuCamBienService.getAllDeviceFromGateway(gatewayId);
    List<DeviceEntity> rpcDeviceList = new ArrayList<>();
    if (!deviceEntities.isEmpty() || deviceEntities == null) {
      for (DeviceEntity deviceEntity : deviceEntities) {
        String rpc = "RPC";
        if (deviceEntity.getName().toLowerCase().contains(rpc.toLowerCase())) {
          rpcDeviceList.add(deviceEntity);
        }
      }
      return rpcDeviceList;
    }
    return Collections.EMPTY_LIST;
  }

  public List<AttributeKvEntry> getAllShareAttributeInDeviceRpc(TenantId tenantId, UUID deviceRpcId)
      throws ExecutionException, InterruptedException {
    DeviceId deviceId = new DeviceId(deviceRpcId);
    ListenableFuture<List<AttributeKvEntry>> listenableFuture =
        attributesService.findAll(tenantId, deviceId, DataConstants.CLIENT_SCOPE);
    return listenableFuture.get();
  }

  public List<AttributeKvEntry> getAllServerAttributeInDeviceRpc(
      TenantId tenantId, UUID deviceRpcId) throws ExecutionException, InterruptedException {
    DeviceId deviceId = new DeviceId(deviceRpcId);
    ListenableFuture<List<AttributeKvEntry>> listenableFuture =
        attributesService.findAll(tenantId, deviceId, DataConstants.SERVER_SCOPE);
    return listenableFuture.get();
  }

  public TsKvEntry getLastestStatus(TenantId tenantId, UUID deviceRpcId, String deviceName)
      throws ExecutionException, InterruptedException {
    return timeseriesLatestDao.findLatest(tenantId, new DeviceId(deviceRpcId), deviceName).get();
  }

  public List<ThietBiDieuKhien> getAllThietBiDieuKhien(SecurityUser securityUser, UUID damtomId) {
    List<ThietBiDieuKhien> thietBiDieuKhienList = new ArrayList<>();
    List<DeviceEntity> listGateway =
        getAllGatewayFromDamTom(securityUser.getTenantId().getId(), damtomId);
    for (DeviceEntity gatewayEntity : listGateway) {
      List<DeviceEntity> listRpcDevice = getAllRpcDeviceFromGateway(gatewayEntity.getId());
      for (DeviceEntity rpcDevice : listRpcDevice) {
        ThietBiDieuKhien thietBiDieuKhien = new ThietBiDieuKhien();
        thietBiDieuKhien =
            getThietBiDieuKhienById(securityUser.getTenantId(), rpcDevice.getId(), damtomId);
        thietBiDieuKhienList.add(thietBiDieuKhien);
      }
    }
    return thietBiDieuKhienList;
  }

  public ThietBiDieuKhien getThietBiDieuKhienById(TenantId tenantId, UUID deviceId, UUID damTomId) {
    ThietBiDieuKhien thietBiDieuKhien = new ThietBiDieuKhien();
    thietBiDieuKhien.setDamTomId(damTomId);
    DeviceEntity rpcDevice = dftDeviceRepository.findById(deviceId).get();
    if (rpcDevice != null) {
      thietBiDieuKhien.setTenThietBi(rpcDevice.getName());
      if (rpcDevice.getLabel() == null) {
        thietBiDieuKhien.setLabel(rpcDevice.getName());
      } else if (rpcDevice.getLabel().isEmpty()) {
        thietBiDieuKhien.setLabel(rpcDevice.getName());
      } else {
        thietBiDieuKhien.setLabel(rpcDevice.getLabel());
      }
      thietBiDieuKhien.setDeviceId(rpcDevice.getId());
      try {
        List<AttributeKvEntry> attributeKvEntryList =
            getAllShareAttributeInDeviceRpc(tenantId, rpcDevice.getId());
        for (AttributeKvEntry attributeKvEntry : attributeKvEntryList) {
          //          if (attributeKvEntry.getKey().equals(getValueMethod)) {
          //            thietBiDieuKhien.setGetValueMethod(attributeKvEntry.getValueAsString());
          //          }
          if (attributeKvEntry.getKey().equals(setValueMethod)) {
            thietBiDieuKhien.setSetValueMethod(attributeKvEntry.getValueAsString());
          }
        }
        TsKvEntry tsKvEntry = getLastestStatus(tenantId, rpcDevice.getId(), rpcDevice.getName());
        if (tsKvEntry == null) {
          thietBiDieuKhien.setStatusTime(0);
        } else {
          List<AttributeKvEntry> serverAttributeKvEntryList =
              getAllServerAttributeInDeviceRpc(tenantId, rpcDevice.getId());
          if (tsKvEntry.getTs() < new Date().getTime() - defaultInactivityTimeoutInSec * 1000) {
            for (AttributeKvEntry serverAttributeKvEntry : serverAttributeKvEntryList) {
              //          if (attributeKvEntry.getKey().equals(getValueMethod)) {
              //            thietBiDieuKhien.setGetValueMethod(attributeKvEntry.getValueAsString());
              //          }
              if (serverAttributeKvEntry.getKey().equals(ACTIVE)) {
                if (serverAttributeKvEntry.getBooleanValue().get()) {
                  thietBiDieuKhien.setStatusTime(tsKvEntry.getTs());
                } else {
                  for (AttributeKvEntry serverAttributeKvEntry2 : serverAttributeKvEntryList) {
                    if (serverAttributeKvEntry2.getKey().equals(INACTIVITY_ALARM_TIME)) {
                      thietBiDieuKhien.setStatusTime(serverAttributeKvEntry2.getLongValue().get());
                    }
                  }
                }
              }
            }
          } else {
            Long timeChangeStatus = (long) 0;
            if (tsKvEntry.getDataType().equals(DataType.DOUBLE)) {
              timeChangeStatus =
                  dftTelemetryViewRepository.getFirstTimeChangeDoubleStatus(
                      rpcDevice.getId(), rpcDevice.getName(), tsKvEntry.getDoubleValue().get());
            } else if (tsKvEntry.getDataType().equals(DataType.LONG)) {
              timeChangeStatus =
                  dftTelemetryViewRepository.getFirstTimeChangeLongStatus(
                      rpcDevice.getId(), rpcDevice.getName(), tsKvEntry.getLongValue().get());
            }
            thietBiDieuKhien.setStatusTime(timeChangeStatus);
            if (thietBiDieuKhien.getStatusTime() == 0) {
              for (AttributeKvEntry serverAttributeKvEntry2 : serverAttributeKvEntryList) {
                if (serverAttributeKvEntry2.getKey().equals(LAST_CONNECTION_TIME)) {
                  thietBiDieuKhien.setStatusTime(serverAttributeKvEntry2.getLongValue().get());
                }
              }
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return thietBiDieuKhien;
  }
}
