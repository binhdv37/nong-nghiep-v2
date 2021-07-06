package org.thingsboard.server.dft.services.dlcambien;

import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.RelationEntity;
import org.thingsboard.server.dao.timeseries.TimeseriesLatestDao;
import org.thingsboard.server.dao.timeseries.TimeseriesService;
import org.thingsboard.server.dft.common.dtos.TimeRange;
import org.thingsboard.server.dft.common.service.TimeQueryService;
import org.thingsboard.server.dft.controllers.web.dulieucb.dao.BoDuLieuCamBien;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomGatewayEntity;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;
import org.thingsboard.server.dft.repositories.DftRelationRepository;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.dlcambien.imp.DuLieuCamBiemServiceImp;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class DuLieuCamBienService implements DuLieuCamBiemServiceImp {

    private final DftDeviceRepository dftDeviceRepository;

    private final DftRelationRepository relationRepository;

    private final TimeQueryService timeQueryService;

    private final DamTomService damTomService;

    @Autowired
    private TimeseriesLatestDao timeseriesLatestDao;

    @Autowired
    private TimeseriesService timeseriesService;

    @Autowired
    public DuLieuCamBienService(
            DftDeviceRepository dftDeviceRepository,
            DftRelationRepository relationRepository,
            TimeQueryService timeQueryService, DamTomService damTomService) {
        this.dftDeviceRepository = dftDeviceRepository;
        this.relationRepository = relationRepository;
        this.timeQueryService = timeQueryService;
        this.damTomService = damTomService;
    }

    @Override
    public List<DeviceEntity> getAllGatewayFromDamTom(UUID tenantId, UUID damTomId) {
        DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damTomId);
        List<DeviceEntity> deviceEntities = getAllGatewayFromAssert(damTomEntity.getAsset().getId());
        Collection<DamTomGatewayEntity> damTomGatewayEntities = damTomEntity.getGateways();
        if (damTomGatewayEntities.isEmpty() || damTomGatewayEntities == null) {
            return Collections.EMPTY_LIST;
        }
        damTomGatewayEntities.forEach(damTomGatewayEntity -> {
            if (!damTomGatewayEntity.isActive()) {
                deviceEntities.remove(damTomGatewayEntity.getDevice());
            }
        });
        return deviceEntities;
    }

    @Override
    public List<DeviceEntity> getAllGatewayFromAssert(UUID assetId) {
        List<DeviceEntity> listGateway = new ArrayList<>();
        List<RelationEntity> listRelationAssert =
                relationRepository.findAllByFromIdAndFromTypeAndToType(assetId, "ASSET", "DEVICE");
        listRelationAssert.forEach(
                relationEntity -> {
                    listGateway.add(dftDeviceRepository.findDeviceEntityById(relationEntity.getToId()));
                });
        return listGateway;
    }

    @Override
    public List<DeviceEntity> getAllDeviceFromGateway(UUID deviceId) {
        List<DeviceEntity> listDevice = new ArrayList<>();
        List<RelationEntity> listRelationGateway =
                relationRepository.findAllByFromIdAndFromTypeAndToType(deviceId, "DEVICE", "DEVICE");
        listRelationGateway.forEach(
                relationEntity -> {
                    listDevice.add(dftDeviceRepository.findDeviceEntityById(relationEntity.getToId()));
                });
        return listDevice;
    }

    @Override
    public List<String> findAllKeysByEntityIds(TenantId tenantId, List<EntityId> entityIds) {
        return timeseriesLatestDao.findAllKeysByEntityIds(tenantId, entityIds);
    }

    @Override
    public List<TsKvEntry> finAllTsKvByRangeTime(
            TenantId tenantId, UUID deviceId, List<String> keys, long startTs, long endTs)
            throws ExecutionException, InterruptedException {
        List<ReadTsKvQuery> queries =
                keys.stream()
                        .map(
                                key ->
                                        new BaseReadTsKvQuery(
                                                key,
                                                startTs,
                                                endTs,
                                                0,
                                                1,
                                                Aggregation.valueOf(Aggregation.NONE.name()),
                                                "DESC"))
                        .collect(Collectors.toList());
        DeviceId deviceIdE = new DeviceId(deviceId);
        ListenableFuture<List<TsKvEntry>> listenableFuture =
                timeseriesService.findAll(tenantId, deviceIdE, queries);
        return listenableFuture.get();
    }

    @Override
    public PageData<BoDuLieuCamBien> getBoDuLieuCamBien(
            TenantId tenantId, UUID damtomId, long startTime, long endTime, int pageSize, int page, String sort) {

        List<BoDuLieuCamBien> dlcbList = new ArrayList<>();
        List<DeviceEntity> listGateway = getAllGatewayFromDamTom(tenantId.getId(), damtomId);

        if (listGateway == null || listGateway.isEmpty()) {
            return new PageData<>();
        }

        List<TimeRange> timeRangeList = timeQueryService.getTimeRangeQuery(startTime, endTime);
        if (sort.toLowerCase(Locale.ROOT).equals("desc")) {
            Collections.reverse(timeRangeList);
        }
        long totalElement = timeRangeList.size();
        int totalPage = (int) Math.ceil(totalElement / pageSize);
        List<TimeRange> timeRangeListPage = new ArrayList<>();
        if ((page + 1) * pageSize < totalElement) {
            timeRangeListPage = timeRangeList.subList(page * pageSize, (page + 1) * pageSize);
        } else {
            timeRangeListPage = timeRangeList.subList(page * pageSize, (int) totalElement);
        }

        timeRangeListPage.forEach(
                timeRange -> {
                    Map<String, Double> avgData = new HashMap<>();
                    Map<String, Integer> countData = new HashMap<>();
                    listGateway.forEach(
                            deviceGateway -> {
                                BoDuLieuCamBien boDuLieuCamBien = new BoDuLieuCamBien();
                                boDuLieuCamBien.setKhoangThoiGian(timeQueryService.getStringRangeTime(timeRange));
                                boDuLieuCamBien.setGetwayId(deviceGateway.getId());
                                boDuLieuCamBien.setTenGateway(deviceGateway.getName());
                                boDuLieuCamBien.setSpan(listGateway.size() + 1);
                                boDuLieuCamBien.setDisplay(listGateway.get(0) == deviceGateway);
                                DeviceId deviceId = new DeviceId(deviceGateway.getId());
                                List<String> tsKeyList =
                                        findAllKeysByEntityIds(tenantId, Collections.singletonList(deviceId));
                                Map<String, Double> data = new HashMap<>();
                                try {
                                    List<TsKvEntry> tsKvEntryList =
                                            finAllTsKvByRangeTime(
                                                    tenantId,
                                                    deviceGateway.getId(),
                                                    tsKeyList,
                                                    timeRange.getStartTime(),
                                                    timeRange.getEndTime());
                                    if (tsKvEntryList != null) {
                                        for (TsKvEntry tsKvEntry : tsKvEntryList) {
                                            if (tsKvEntry.getDataType() == DataType.LONG) {
                                                data.put(tsKvEntry.getKey(), (double) tsKvEntry.getLongValue().get());
                                                if (avgData.containsKey(tsKvEntry.getKey())) {
                                                    avgData.put(tsKvEntry.getKey(),
                                                            avgData.get(tsKvEntry.getKey()) + (double) tsKvEntry.getLongValue().get());
                                                    countData.put(tsKvEntry.getKey(), countData.get(tsKvEntry.getKey()) + 1);
                                                } else {
                                                    avgData.put(tsKvEntry.getKey(), (double) tsKvEntry.getLongValue().get());
                                                    countData.put(tsKvEntry.getKey(), 1);
                                                }
                                            }
                                            if (tsKvEntry.getDataType() == DataType.DOUBLE) {
                                                data.put(tsKvEntry.getKey(), tsKvEntry.getDoubleValue().get());
                                                if (avgData.containsKey(tsKvEntry.getKey())) {
                                                    avgData.put(tsKvEntry.getKey(), avgData.get(tsKvEntry.getKey())
                                                            + tsKvEntry.getDoubleValue().get());
                                                    countData.put(tsKvEntry.getKey(), countData.get(tsKvEntry.getKey()) + 1);
                                                } else {
                                                    avgData.put(tsKvEntry.getKey(), tsKvEntry.getDoubleValue().get());
                                                    countData.put(tsKvEntry.getKey(), 1);
                                                }
                                            }
                                        }
                                    }
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                boDuLieuCamBien.setData(data);
                                dlcbList.add(boDuLieuCamBien);
                            });
                    if (!avgData.isEmpty()) {
                        avgData.entrySet().forEach(entry -> {
                            double avgTelemetry = entry.getValue() / countData.get(entry.getKey());
                            entry.setValue(Math.round(avgTelemetry * 100.0) / 100.0);
                        });
                    }
                    BoDuLieuCamBien boDuLieuCamBien = new BoDuLieuCamBien(timeQueryService.getStringRangeTime(timeRange), listGateway.size() + 1,
                            false, UUID.randomUUID(), "Trung bình", avgData);
                    dlcbList.add(boDuLieuCamBien);
                });
        System.out.println(dlcbList.get(0));
        boolean hasNext = (page != totalPage);
        PageData<BoDuLieuCamBien> pageData = new PageData<>(dlcbList, totalPage, totalElement, hasNext);
        return pageData;
    }

    private List<String> toKeysList(String keys) {
        List<String> keyList = null;
        if (!StringUtils.isEmpty(keys)) {
            keyList = Arrays.asList(keys.split(","));
        }
        return keyList;
    }
}
