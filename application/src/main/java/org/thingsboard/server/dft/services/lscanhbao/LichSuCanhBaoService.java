package org.thingsboard.server.dft.services.lscanhbao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.alarm.AlarmSeverity;
import org.thingsboard.server.common.data.device.profile.AlarmRule;
import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;
import org.thingsboard.server.common.data.device.profile.DeviceProfileData;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.DataType;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.query.KeyFilter;
import org.thingsboard.server.dao.model.sql.AssetEntity;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;
import org.thingsboard.server.dao.model.sql.RelationEntity;
import org.thingsboard.server.dao.sql.asset.AssetRepository;
import org.thingsboard.server.dao.timeseries.TimeseriesService;
import org.thingsboard.server.dft.common.constants.AlarmNamePrefixConstant;
import org.thingsboard.server.dft.common.constants.EntityConstant;
import org.thingsboard.server.dft.controllers.web.lscanhbao.dtos.DamTomSnapshot;
import org.thingsboard.server.dft.controllers.web.lscanhbao.dtos.DataSnapshot;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomSnapshotEntity;
import org.thingsboard.server.dft.repositories.*;
import org.thingsboard.server.dft.services.dlcambien.DuLieuCamBienService;
import org.thingsboard.server.dft.services.lscanhbao.imp.LichSuCanhBaoServiceImp;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class LichSuCanhBaoService implements LichSuCanhBaoServiceImp {

  private final DftRelationRepository dftRelationRepository;
  private final AssetRepository assetRepository;
  private final DamTomRepository damTomRepository;
  private final TimeseriesService timeseriesService;
  private final DamTomSnapshotRepository damTomSnapshotRepository;
  private final DftDeviceProfileRepository deviceProfileRepository;
  private final DuLieuCamBienService duLieuCamBienService;
  private final DftDeviceRepository deviceRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  public LichSuCanhBaoService(
      DftRelationRepository dftRelationRepository,
      AssetRepository assetRepository,
      DamTomRepository damTomRepository,
      TimeseriesService timeseriesService,
      DamTomSnapshotRepository damTomSnapshotRepository,
      DftDeviceProfileRepository deviceProfileRepository,
      DuLieuCamBienService duLieuCamBienService,
      DftDeviceRepository deviceRepository) {
    this.dftRelationRepository = dftRelationRepository;
    this.assetRepository = assetRepository;
    this.damTomRepository = damTomRepository;
    this.timeseriesService = timeseriesService;
    this.damTomSnapshotRepository = damTomSnapshotRepository;
    this.deviceProfileRepository = deviceProfileRepository;
    this.duLieuCamBienService = duLieuCamBienService;
    this.deviceRepository = deviceRepository;
  }

  public AssetEntity getAssetFromGatewayId(UUID gatewayId) {
    RelationEntity relationEntity =
        dftRelationRepository.findRelationEntityByToId(gatewayId, "ASSET", "DEVICE");
    AssetEntity assetEntity = assetRepository.findById(relationEntity.getFromId()).get();
    return assetEntity;
  }

  public List<DeviceEntity> getAllGatewayFromDamTom(UUID assetId) {
    DamTomEntity damTomEntity = damTomRepository.findDamTomEntityByAssetId(assetId);
    List<DeviceEntity> deviceEntities =
        duLieuCamBienService.getAllGatewayFromDamTom(
            damTomEntity.getTenantId(), damTomEntity.getId());
    return deviceEntities;
  }

  public DeviceProfileEntity getDeviceProfileById(UUID tenantId, UUID id) {
    return deviceProfileRepository.findDeviceProfileEntityByTenantIdAndId(tenantId, id);
  }

  public Set<String> getListAlarmKey(DeviceProfileEntity deviceProfileEntity, String alarmType) {
    Set<String> keySet = new HashSet<>();
    try {
      DeviceProfileData deviceProfileData =
          objectMapper.treeToValue(deviceProfileEntity.getProfileData(), DeviceProfileData.class);
      if (deviceProfileData.getAlarms() != null) {
        List<DeviceProfileAlarm> alarmList =
            deviceProfileData.getAlarms().stream()
                .filter(
                    deviceProfileAlarm ->
                        deviceProfileAlarm.getDftAlarmRule().isActive()
                            && !deviceProfileAlarm.getDftAlarmRule().isRpcAlarm())
                .collect(Collectors.toList());
        for (DeviceProfileAlarm deviceProfileAlarm : alarmList) {
          if (deviceProfileAlarm.getAlarmType().equals(alarmType)) {
            TreeMap<AlarmSeverity, AlarmRule> alarmRuleMap = deviceProfileAlarm.getCreateRules();
            AlarmRule alarmRule = alarmRuleMap.get(AlarmSeverity.CRITICAL);
            List<KeyFilter> keyFilterList = alarmRule.getCondition().getCondition();
            for (KeyFilter keyFilter : keyFilterList) {
              keySet.add(keyFilter.getKey().getKey());
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return keySet;
  }

  public List<TsKvEntry> getAllLatestTsKvInGateway(TenantId tenantId, UUID gatewayId)
      throws ExecutionException, InterruptedException {
    ListenableFuture<List<TsKvEntry>> listenableFuture =
        timeseriesService.findAllLatest(tenantId, new DeviceId(gatewayId));
    return listenableFuture.get();
  }

  public Alarm snapShotCanhBao(Alarm alarm, String type) {
    String realAlarmType = alarm.getType();
    alarm.setType(alarm.getType().replaceFirst(AlarmNamePrefixConstant.ALARM_PREFIX, ""));
    DamTomSnapshotEntity damTomSnapshotEntity = new DamTomSnapshotEntity();
    damTomSnapshotEntity.setId(UUID.randomUUID());
    damTomSnapshotEntity.setAlarmId(alarm.getUuidId());
    damTomSnapshotEntity.setAlarmName(alarm.getType());
    damTomSnapshotEntity.setClear(false);
    damTomSnapshotEntity.setTenantId(alarm.getTenantId().getId());
    if (type.equals(EntityConstant.TYPE_ADD)) {
      damTomSnapshotEntity.setTimeSnapshot(alarm.getStartTs());
    }
    if (type.equals(EntityConstant.TYPE_EDIT)) {
      damTomSnapshotEntity.setTimeSnapshot(alarm.getEndTs());
    }
    AssetEntity damTomAsset = getAssetFromGatewayId(alarm.getOriginator().getId());
    DamTomEntity damTomEntity = damTomRepository.findDamTomEntityByAssetId(damTomAsset.getId());
    DeviceProfileEntity deviceProfileEntity =
        getDeviceProfileById(damTomEntity.getTenantId(), damTomEntity.getDeviceProfile().getId());
    Set<String> keyList = getListAlarmKey(deviceProfileEntity, realAlarmType);

    damTomSnapshotEntity.setDamtomId(damTomEntity.getId());
    List<DeviceEntity> gatewayList = getAllGatewayFromDamTom(damTomAsset.getId());
    List<DataSnapshot> listDataSnapshot = new ArrayList<>();
    for (DeviceEntity gatewayEntity : gatewayList) {
      try {
        DataSnapshot dataSnapshot = new DataSnapshot();
        dataSnapshot.setGatewayId(gatewayEntity.getId());
        dataSnapshot.setGatewayName(gatewayEntity.getName());
        if (gatewayEntity.getId().compareTo(alarm.getOriginator().getId()) == 0) {
          dataSnapshot.setAlarmGateway(true);
          dataSnapshot.setAlarmKey(keyList);
          List<DeviceEntity> deviceEntities =
              duLieuCamBienService.getAllDeviceFromGateway(gatewayEntity.getId());
          for (DeviceEntity deviceEntity : deviceEntities) {
            if (deviceEntity
                .getName()
                .toLowerCase()
                .contains(keyList.iterator().next().toLowerCase())) {
              dataSnapshot.setDeviceId(deviceEntity.getId());
              if (deviceEntity.getLabel() != null) {
                dataSnapshot.setDeviceName(deviceEntity.getLabel());
              } else {
                dataSnapshot.setDeviceName(deviceEntity.getName());
              }
            }
          }
        } else {
          dataSnapshot.setAlarmGateway(false);
          dataSnapshot.setAlarmKey(Collections.EMPTY_SET);
        }
        List<TsKvEntry> latestTsKvList =
            getAllLatestTsKvInGateway(
                new TenantId(damTomAsset.getTenantId()), gatewayEntity.getId());
        Map<String, Double> data = new HashMap<>();
        if (!latestTsKvList.isEmpty() || latestTsKvList != null) {
          for (TsKvEntry latestTsKv : latestTsKvList) {
            if (latestTsKv.getTs() > damTomSnapshotEntity.getTimeSnapshot() - 6000000) {
              if (latestTsKv.getDataType() == DataType.LONG) {
                data.put(latestTsKv.getKey(), (double) latestTsKv.getLongValue().get());
              } else if (latestTsKv.getDataType() == DataType.DOUBLE) {
                data.put(latestTsKv.getKey(), latestTsKv.getDoubleValue().get());
              }
            }
          }
        }
        dataSnapshot.setData(data);
        dataSnapshot.setTenDamTom(damTomEntity.getName());
        listDataSnapshot.add(dataSnapshot);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      String jsonDataSnapshot = objectMapper.writeValueAsString(listDataSnapshot);
      damTomSnapshotEntity.setDataSnapshot(jsonDataSnapshot);
    } catch (Exception e) {
      e.printStackTrace();
    }
    damTomSnapshotRepository.save(damTomSnapshotEntity);
    return alarm;
  }

  private DeviceEntity getGatewayByDeviceId(UUID deviceId) {
    RelationEntity relationEntity =
        dftRelationRepository.findRelationEntityByToId(deviceId, "DEVICE", "DEVICE");
    if (relationEntity != null) {
      DeviceEntity deviceEntity = deviceRepository.findById(relationEntity.getFromId()).get();
      return deviceEntity;
    } else {
      return null;
    }
  }

  public void saveAlarmDisconnectDevice(
      DamTomEntity damTomEntity, UUID deviceId, long timeDisconnect) {
    DamTomSnapshotEntity damTomSnapshotEntity = new DamTomSnapshotEntity();
    damTomSnapshotEntity.setId(UUID.randomUUID());
    damTomSnapshotEntity.setAlarmId(UUID.randomUUID());
    damTomSnapshotEntity.setAlarmName("Mất kết nối thiết bị");
    damTomSnapshotEntity.setClear(false);
    damTomSnapshotEntity.setTenantId(damTomEntity.getTenantId());
    damTomSnapshotEntity.setDamtomId(damTomEntity.getId());
    damTomSnapshotEntity.setTimeSnapshot(timeDisconnect);
    DataSnapshot dataSnapshot = new DataSnapshot();

    DeviceEntity gatewayEntity = getGatewayByDeviceId(deviceId);
    dataSnapshot.setGatewayId(gatewayEntity.getId());
    dataSnapshot.setGatewayName(gatewayEntity.getName());

    DeviceEntity deviceEntity = deviceRepository.findDeviceEntityById(deviceId);
    dataSnapshot.setDeviceId(deviceId);
    //        dataSnapshot.setDeviceName(deviceEntity.getLabel());
    //        dataSnapshot.setGatewayId(deviceId);
    if (deviceEntity.getLabel() != null) {
      dataSnapshot.setDeviceName(deviceEntity.getLabel());
    } else {
      dataSnapshot.setDeviceName(deviceEntity.getName());
    }

    dataSnapshot.setTenDamTom(damTomEntity.getName());
    dataSnapshot.setAlarmGateway(true);
    dataSnapshot.setAlarmKey(Collections.EMPTY_SET);
    dataSnapshot.setData(Collections.EMPTY_MAP);
    dataSnapshot.setTenDamTom(damTomEntity.getName());

    String jsonDataSnapshot = "";
    try {
      jsonDataSnapshot = objectMapper.writeValueAsString(Arrays.asList(dataSnapshot));
    } catch (Exception e) {
      e.printStackTrace();
    }
    damTomSnapshotEntity.setDataSnapshot(jsonDataSnapshot);
    damTomSnapshotRepository.save(damTomSnapshotEntity);
  }

  public PageData<DamTomSnapshot> getAllDamTomSnap(
      UUID tenantId,
      UUID damtomId,
      Pageable pageable,
      Long startTime,
      Long endTime,
      String searchText) {
    if (searchText == null || searchText.isEmpty()) {
      searchText = "";
    }
    searchText = searchText.trim();
    List<DamTomSnapshot> damTomSnapshotList = new ArrayList<>();
    Page<DamTomSnapshotEntity> damTomSnapshotEntityPage;
    if (damtomId == null) {
      damTomSnapshotEntityPage =
          damTomSnapshotRepository.findAll(tenantId, startTime, endTime, searchText, pageable);
    } else {
      damTomSnapshotEntityPage =
          damTomSnapshotRepository.findAll(
              tenantId, damtomId, startTime, endTime, searchText, pageable);
    }
    List<DamTomSnapshotEntity> damTomSnapshotEntityList = damTomSnapshotEntityPage.getContent();
    for (DamTomSnapshotEntity damTomSnapshotEntity : damTomSnapshotEntityList) {
      try {
        List<DataSnapshot> dataSnapshotList;
        dataSnapshotList =
            Arrays.asList(
                objectMapper.readValue(
                    damTomSnapshotEntity.getDataSnapshot(), DataSnapshot[].class));
        for (DataSnapshot dataSnapshot : dataSnapshotList) {
          DamTomSnapshot damTomSnapshot = new DamTomSnapshot();
          damTomSnapshot.setId(damTomSnapshotEntity.getId());
          damTomSnapshot.setDamTomId(damTomSnapshotEntity.getDamtomId());
          damTomSnapshot.setThoiGian(damTomSnapshotEntity.getTimeSnapshot());
          damTomSnapshot.setGatewayId(dataSnapshot.getGatewayId());
          damTomSnapshot.setGatewayName(dataSnapshot.getGatewayName());
          damTomSnapshot.setAlarmId(damTomSnapshotEntity.getAlarmId());
          damTomSnapshot.setAlarmGateway(dataSnapshot.isAlarmGateway());
          damTomSnapshot.setTenCanhBao(damTomSnapshotEntity.getAlarmName());
          damTomSnapshot.setData(dataSnapshot.getData());
          damTomSnapshot.setSpan(dataSnapshotList.size());
          damTomSnapshot.setDisplay(
              dataSnapshotList.get(0).getGatewayId().compareTo(damTomSnapshot.getGatewayId()) == 0);
          damTomSnapshot.setClear(damTomSnapshotEntity.isClear());
          damTomSnapshot.setTenDamTom(dataSnapshot.getTenDamTom());
          damTomSnapshot.setAlarmKeys(dataSnapshot.getAlarmKey());

          damTomSnapshot.setDeviceId(dataSnapshot.getDeviceId());
          damTomSnapshot.setDeviceName(dataSnapshot.getDeviceName());

          damTomSnapshotList.add(damTomSnapshot);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    PageData<DamTomSnapshot> pageDataSnapshots =
        new PageData<>(
            damTomSnapshotList,
            damTomSnapshotEntityPage.getTotalPages(),
            damTomSnapshotEntityPage.getTotalElements(),
            damTomSnapshotEntityPage.hasNext());
    return pageDataSnapshots;
  }

  public PageData<DamTomSnapshot> getAllDamTomSnapForMobile(
      UUID tenantId,
      UUID damtomId,
      Pageable pageable,
      Long startTime,
      Long endTime,
      String searchText) {
    if (searchText == null || searchText.isEmpty()) {
      searchText = "";
    }
    searchText = searchText.trim();
    List<DamTomSnapshot> damTomSnapshotList = new ArrayList<>();
    Page<DamTomSnapshotEntity> damTomSnapshotEntityPage;
    if (damtomId == null) {
      damTomSnapshotEntityPage =
          damTomSnapshotRepository.findAll(tenantId, startTime, endTime, searchText, pageable);
    } else {
      damTomSnapshotEntityPage =
          damTomSnapshotRepository.findAll(
              tenantId, damtomId, startTime, endTime, searchText, pageable);
    }
    List<DamTomSnapshotEntity> damTomSnapshotEntityList = damTomSnapshotEntityPage.getContent();
    for (DamTomSnapshotEntity damTomSnapshotEntity : damTomSnapshotEntityList) {
      try {
        List<DataSnapshot> dataSnapshotList;
        dataSnapshotList =
            Arrays.asList(
                objectMapper.readValue(
                    damTomSnapshotEntity.getDataSnapshot(), DataSnapshot[].class));
        for (DataSnapshot dataSnapshot : dataSnapshotList) {
          if (dataSnapshot.isAlarmGateway()) {
            DamTomSnapshot damTomSnapshot = new DamTomSnapshot();
            damTomSnapshot.setId(damTomSnapshotEntity.getId());
            damTomSnapshot.setDamTomId(damTomSnapshotEntity.getDamtomId());
            damTomSnapshot.setThoiGian(damTomSnapshotEntity.getTimeSnapshot());
            damTomSnapshot.setGatewayId(dataSnapshot.getGatewayId());
            damTomSnapshot.setGatewayName(dataSnapshot.getGatewayName());
            damTomSnapshot.setAlarmId(damTomSnapshotEntity.getAlarmId());
            damTomSnapshot.setAlarmGateway(dataSnapshot.isAlarmGateway());
            damTomSnapshot.setTenCanhBao(damTomSnapshotEntity.getAlarmName());
            damTomSnapshot.setData(dataSnapshot.getData());
            damTomSnapshot.setSpan(dataSnapshotList.size());
            damTomSnapshot.setDisplay(true);
            damTomSnapshot.setClear(damTomSnapshotEntity.isClear());
            damTomSnapshot.setTenDamTom(dataSnapshot.getTenDamTom());
            damTomSnapshot.setAlarmKeys(dataSnapshot.getAlarmKey());

            damTomSnapshot.setDeviceId(dataSnapshot.getDeviceId());
            damTomSnapshot.setDeviceName(dataSnapshot.getDeviceName());

            damTomSnapshotList.add(damTomSnapshot);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    PageData<DamTomSnapshot> pageDataSnapshots =
        new PageData<>(
            damTomSnapshotList,
            damTomSnapshotEntityPage.getTotalPages(),
            damTomSnapshotEntityPage.getTotalElements(),
            damTomSnapshotEntityPage.hasNext());
    return pageDataSnapshots;
  }

  public void clearAlarm(UUID snapShotId) {
    DamTomSnapshotEntity damTomSnapshotEntity = damTomSnapshotRepository.findById(snapShotId).get();

    damTomSnapshotEntity.setClear(true);
    damTomSnapshotRepository.save(damTomSnapshotEntity);
  }

  public DamTomSnapshot getLastestAlarmSnapshot(List<UUID> damTomIds, UUID tenantId) {
    DamTomSnapshot damTomSnapshot = new DamTomSnapshot();
    DamTomSnapshotEntity damTomSnapshotEntity;
    if (damTomIds == null) {
      damTomSnapshotEntity = damTomSnapshotRepository.findLastestDamTomSnapshot(tenantId);
    } else if (damTomIds.isEmpty()) {
      return damTomSnapshot;
    } else {
      damTomSnapshotEntity =
          damTomSnapshotRepository.findFirstByTenantIdAndClearAndDamtomIdInOrderByTimeSnapshot(
              tenantId, false, damTomIds);
    }
    if (damTomSnapshotEntity == null) {
      return damTomSnapshot;
    }
    try {
      List<DataSnapshot> dataSnapshotList;
      dataSnapshotList =
          Arrays.asList(
              objectMapper.readValue(damTomSnapshotEntity.getDataSnapshot(), DataSnapshot[].class));
      for (DataSnapshot dataSnapshot : dataSnapshotList) {
        damTomSnapshot.setId(damTomSnapshotEntity.getId());
        damTomSnapshot.setDamTomId(damTomSnapshotEntity.getDamtomId());
        damTomSnapshot.setThoiGian(damTomSnapshotEntity.getTimeSnapshot());
        damTomSnapshot.setGatewayId(dataSnapshot.getGatewayId());
        damTomSnapshot.setGatewayName(dataSnapshot.getGatewayName());
        damTomSnapshot.setAlarmId(damTomSnapshotEntity.getAlarmId());
        damTomSnapshot.setAlarmGateway(dataSnapshot.isAlarmGateway());
        damTomSnapshot.setTenCanhBao(damTomSnapshotEntity.getAlarmName());
        damTomSnapshot.setData(dataSnapshot.getData());
        damTomSnapshot.setSpan(dataSnapshotList.size());
        damTomSnapshot.setDisplay(
            dataSnapshotList.get(0).getGatewayId().compareTo(damTomSnapshot.getGatewayId()) == 0);
        damTomSnapshot.setClear(damTomSnapshotEntity.isClear());
        damTomSnapshot.setTenDamTom(dataSnapshot.getTenDamTom());
        damTomSnapshot.setAlarmKeys(dataSnapshot.getAlarmKey());
        damTomSnapshot.setDeviceId(dataSnapshot.getDeviceId());
        damTomSnapshot.setDeviceName(dataSnapshot.getDeviceName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return damTomSnapshot;
  }

  public List<DamTomSnapshot> getAllActiveAlarm(UUID damTomId, UUID tenantId) {
    List<DamTomSnapshot> damTomSnapshotList = new ArrayList<>();
    List<DamTomSnapshotEntity> damTomSnapshotEntityList =
        damTomSnapshotRepository.findAllDamTomSnapshotClearFalseByDamTomId(tenantId, damTomId);
    for (DamTomSnapshotEntity damTomSnapshotEntity : damTomSnapshotEntityList) {
      try {
        List<DataSnapshot> dataSnapshotList;
        dataSnapshotList =
            Arrays.asList(
                objectMapper.readValue(
                    damTomSnapshotEntity.getDataSnapshot(), DataSnapshot[].class));
        for (DataSnapshot dataSnapshot : dataSnapshotList) {
          DamTomSnapshot damTomSnapshot = new DamTomSnapshot();
          damTomSnapshot.setId(damTomSnapshotEntity.getId());
          damTomSnapshot.setDamTomId(damTomSnapshotEntity.getDamtomId());
          damTomSnapshot.setThoiGian(damTomSnapshotEntity.getTimeSnapshot());
          damTomSnapshot.setGatewayId(dataSnapshot.getGatewayId());
          damTomSnapshot.setGatewayName(dataSnapshot.getGatewayName());
          damTomSnapshot.setAlarmId(damTomSnapshotEntity.getAlarmId());
          damTomSnapshot.setAlarmGateway(dataSnapshot.isAlarmGateway());
          damTomSnapshot.setTenCanhBao(damTomSnapshotEntity.getAlarmName());
          damTomSnapshot.setData(dataSnapshot.getData());
          damTomSnapshot.setSpan(dataSnapshotList.size());
          damTomSnapshot.setDisplay(
              dataSnapshotList.get(0).getGatewayId().compareTo(damTomSnapshot.getGatewayId()) == 0);
          damTomSnapshot.setClear(damTomSnapshotEntity.isClear());
          damTomSnapshot.setTenDamTom(dataSnapshot.getTenDamTom());
          damTomSnapshot.setAlarmKeys(dataSnapshot.getAlarmKey());
          damTomSnapshot.setDeviceId(dataSnapshot.getDeviceId());
          damTomSnapshot.setDeviceName(dataSnapshot.getDeviceName());

          damTomSnapshotList.add(damTomSnapshot);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return damTomSnapshotList;
  }

  public int getTotalActiveAlarm(UUID damTomId, List<UUID> damTomIds, UUID tenantId) {
    if (damTomId == null) {
      return damTomSnapshotRepository
          .findAllByTenantIdAndClearAndDamtomIdInOrderByTimeSnapshot(tenantId, false, damTomIds)
          .size();
    } else {
      return damTomSnapshotRepository
          .findAllByTenantIdAndClearAndDamtomIdOrderByTimeSnapshot(tenantId, false, damTomId)
          .size();
    }
  }
}
