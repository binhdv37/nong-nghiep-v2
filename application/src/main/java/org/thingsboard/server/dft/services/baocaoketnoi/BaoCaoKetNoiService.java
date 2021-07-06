package org.thingsboard.server.dft.services.baocaoketnoi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dao.model.sql.AssetEntity;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.RelationEntity;
import org.thingsboard.server.dao.sql.asset.AssetRepository;
import org.thingsboard.server.dft.common.constants.DeviceTypeConstant;
import org.thingsboard.server.dft.common.dtos.TimeRange;
import org.thingsboard.server.dft.common.service.TimeQueryService;
import org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos.BaoCaoKetNoi;
import org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos.BaoCaoKetNoiChart;
import org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos.BaoCaoKetNoiTable;
import org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos.SaveBaoCaoKetNoi;
import org.thingsboard.server.dft.entities.BaoCaoKetNoiEntity;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.repositories.BaoCaoKetNoiRepository;
import org.thingsboard.server.dft.repositories.DamTomRepository;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;
import org.thingsboard.server.dft.repositories.DftRelationRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BaoCaoKetNoiService {

    private final BaoCaoKetNoiRepository baoCaoKetNoiRepository;
    private final DftRelationRepository dftRelationRepository;
    private final DamTomRepository damTomRepository;
    private final AssetRepository assetRepository;
    private final DftDeviceRepository deviceRepository;
    private final TimeQueryService timeQueryService;

    @Autowired
    public BaoCaoKetNoiService(
            BaoCaoKetNoiRepository baoCaoKetNoiRepository,
            DftRelationRepository dftRelationRepository,
            DamTomRepository damTomRepository,
            AssetRepository assetRepository,
            DftDeviceRepository deviceRepository,
            TimeQueryService timeQueryService) {
        this.baoCaoKetNoiRepository = baoCaoKetNoiRepository;
        this.dftRelationRepository = dftRelationRepository;
        this.damTomRepository = damTomRepository;
        this.assetRepository = assetRepository;
        this.deviceRepository = deviceRepository;
        this.timeQueryService = timeQueryService;
    }

    public DamTomEntity getDamTomByDeviceId(UUID deviceId) {
        DeviceEntity deviceGateway = getGatewayByDeviceId(deviceId);
        if (deviceGateway != null) {
            RelationEntity relationEntity =
                    dftRelationRepository.findRelationEntityByToId(deviceGateway.getId(), "ASSET", "DEVICE");
            if (relationEntity != null) {
                AssetEntity assetEntity = assetRepository.findById(relationEntity.getFromId()).get();
                DamTomEntity damTomEntity = damTomRepository.findDamTomEntityByAssetId(assetEntity.getId());
                return damTomEntity;
            } else {
                return null;
            }
        }
        return null;
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

    public BaoCaoKetNoiEntity save(SaveBaoCaoKetNoi saveBaoCaoKetNoi) {
        BaoCaoKetNoiEntity baoCaoKetNoiEntity = new BaoCaoKetNoiEntity();
        baoCaoKetNoiEntity.setId(UUID.randomUUID());
        baoCaoKetNoiEntity.setTenantId(saveBaoCaoKetNoi.getTenantId());
        baoCaoKetNoiEntity.setDamtomId(saveBaoCaoKetNoi.getDamTomId());
        baoCaoKetNoiEntity.setLastActiveTime(saveBaoCaoKetNoi.getLastActiveTime());
        baoCaoKetNoiEntity.setDeviceId(saveBaoCaoKetNoi.getDeviceId());
        DeviceEntity deviceEntity = deviceRepository.findById(saveBaoCaoKetNoi.getDeviceId()).get();
        if (deviceEntity
                .getName()
                .toLowerCase()
                .contains(DeviceTypeConstant.Temperature.toLowerCase())) {
            baoCaoKetNoiEntity.setDeviceType(DeviceTypeConstant.Temperature);
        }
        if (deviceEntity.getName().toLowerCase().contains(DeviceTypeConstant.DO.toLowerCase())) {
            baoCaoKetNoiEntity.setDeviceType(DeviceTypeConstant.DO);
        }
        if (deviceEntity.getName().toLowerCase().contains(DeviceTypeConstant.pH.toLowerCase())) {
            baoCaoKetNoiEntity.setDeviceType(DeviceTypeConstant.pH);
        }
//        if (deviceEntity.getName().toLowerCase().contains(DeviceTypeConstant.ORP.toLowerCase())) {
//            baoCaoKetNoiEntity.setDeviceType(DeviceTypeConstant.ORP);
//        }
        if (deviceEntity.getName().toLowerCase().contains(DeviceTypeConstant.Salinity.toLowerCase())) {
            baoCaoKetNoiEntity.setDeviceType(DeviceTypeConstant.Salinity);
        }
        if (baoCaoKetNoiEntity.getDeviceType() == null) {
            baoCaoKetNoiEntity.setDeviceType(DeviceTypeConstant.Gateway);
        }
        baoCaoKetNoiEntity.setCreateTime(new Date().getTime());
        baoCaoKetNoiEntity = baoCaoKetNoiRepository.save(baoCaoKetNoiEntity);
        return baoCaoKetNoiEntity;
    }

    public List<BaoCaoKetNoiChart> getChartData(
            UUID damTomId, UUID tenantId, long startTime, long endTime) {
        List<BaoCaoKetNoi> baoCaoKetNoiList;
        if (damTomId != null) {
            baoCaoKetNoiList =
                    baoCaoKetNoiRepository.countMatKetNoiByDamtomId(
                            damTomId, tenantId, startTime, endTime);
        } else {
            baoCaoKetNoiList =
                    baoCaoKetNoiRepository.countMatKetNoi(
                            tenantId, startTime, endTime);
        }
        List<BaoCaoKetNoiChart> listBaoCaoKetNoiChart =
                baoCaoKetNoiList.stream().map(BaoCaoKetNoiChart::new).collect(Collectors.toList());
        Collections.sort(listBaoCaoKetNoiChart, (p1, p2) -> Integer.valueOf(p1.getSort()).compareTo(p2.getSort()));
        return listBaoCaoKetNoiChart;
    }

    public List<BaoCaoKetNoiTable> getTableData(
            UUID damTomId, UUID tenantId, long startTime, long endTime) {
        List<TimeRange> timeRangeList = timeQueryService.getDayRangeQuery(startTime, endTime);
        List<BaoCaoKetNoiTable> baoCaoKetNoiTables = new ArrayList<>();
        for (TimeRange timeRange : timeRangeList) {
            List<BaoCaoKetNoi> baoCaoKetNoiList;
            if (damTomId != null) {
                baoCaoKetNoiList =
                        baoCaoKetNoiRepository.countMatKetNoiByDamtomId(
                                damTomId, tenantId, timeRange.getStartTime(), timeRange.getEndTime());
            } else {
                baoCaoKetNoiList =
                        baoCaoKetNoiRepository.countMatKetNoi(
                                tenantId, timeRange.getStartTime(), timeRange.getEndTime());
            }

            BaoCaoKetNoiTable baoCaoKetNoiTable = new BaoCaoKetNoiTable();
            baoCaoKetNoiTable.setThoiGian(timeQueryService.getDateByTimestamp(timeRange.getEndTime()));
            Map<String, Long> mapData = new HashMap<>();
            for (BaoCaoKetNoi baoCaoKetNoi : baoCaoKetNoiList) {
                mapData.put(baoCaoKetNoi.getDeviceType(), baoCaoKetNoi.getCountValue());
            }
            baoCaoKetNoiTable.setData(mapData);
            baoCaoKetNoiTables.add(baoCaoKetNoiTable);
        }
        List<BaoCaoKetNoi> baoCaoKetNoiList;
        if (damTomId != null) {
            baoCaoKetNoiList =
                    baoCaoKetNoiRepository.countMatKetNoiByDamtomId(
                            damTomId, tenantId, startTime, endTime);
        } else {
            baoCaoKetNoiList =
                    baoCaoKetNoiRepository.countMatKetNoi(
                            tenantId, startTime, endTime);
        }
        BaoCaoKetNoiTable baoCaoKetNoiTable = new BaoCaoKetNoiTable();
        baoCaoKetNoiTable.setThoiGian("Tổng");
        Map<String, Long> mapData = new HashMap<>();
        for (BaoCaoKetNoi baoCaoKetNoi : baoCaoKetNoiList) {
            mapData.put(baoCaoKetNoi.getDeviceType(), baoCaoKetNoi.getCountValue());
        }
        baoCaoKetNoiTable.setData(mapData);
        baoCaoKetNoiTables.add(baoCaoKetNoiTable);
        return baoCaoKetNoiTables;
    }

    public String getMailContentData(UUID tenantId, UUID damTomId, long startTime, long endTime) {
        List<BaoCaoKetNoiTable> baoCaoKetNoiTables;
        try {
            if (damTomId != null) {
                baoCaoKetNoiTables = getTableData(damTomId, tenantId, startTime, endTime);
            } else {
                baoCaoKetNoiTables = getTableData(null, tenantId, startTime, endTime);
            }
            StringBuilder htmlContentBuilder = new StringBuilder();
            htmlContentBuilder.append("<html> <head> <style> table, td, th { border: 1px solid black; } table { width: 100%; border-collapse: collapse; } </style> </head> <body>");
            htmlContentBuilder.append("<h2>Báo cáo kết nối cảm biến</h2>");
            htmlContentBuilder.append("<table style=\"text-align: center;\">");
            htmlContentBuilder.append("<tr>");
            htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Thời gian</th>");
            htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Cảm biến nhiệt độ</th>");
            htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Cảm biến độ pH</th>");
            htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Cảm biến độ mặn</th>");
//            htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Cảm biến Oxy hóa khử</th>");
            htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Cảm biến Oxy hòa tan</th>");
            htmlContentBuilder.append("</tr>");

            for (BaoCaoKetNoiTable baoCaoKetNoiTable : baoCaoKetNoiTables) {
                htmlContentBuilder.append("<tr>");
                htmlContentBuilder.append("<td>" + baoCaoKetNoiTable.getThoiGian() + "</td>");
                if (baoCaoKetNoiTable.getData().containsKey(DeviceTypeConstant.Temperature)) {
                    htmlContentBuilder.append("<td>" + baoCaoKetNoiTable.getData().get(DeviceTypeConstant.Temperature) + "</td>");
                } else {
                    htmlContentBuilder.append("<td>" + 0 + "</td>");
                }

                if (baoCaoKetNoiTable.getData().containsKey(DeviceTypeConstant.pH)) {
                    htmlContentBuilder.append("<td>" + baoCaoKetNoiTable.getData().get(DeviceTypeConstant.pH) + "</td>");
                } else {
                    htmlContentBuilder.append("<td>" + 0 + "</td>");
                }

                if (baoCaoKetNoiTable.getData().containsKey(DeviceTypeConstant.Salinity)) {
                    htmlContentBuilder.append("<td>" + baoCaoKetNoiTable.getData().get(DeviceTypeConstant.Salinity) + "</td>");
                } else {
                    htmlContentBuilder.append("<td>" + 0 + "</td>");
                }

//                if (baoCaoKetNoiTable.getData().containsKey(DeviceTypeConstant.ORP)) {
//                    htmlContentBuilder.append("<td>" + baoCaoKetNoiTable.getData().get(DeviceTypeConstant.ORP) + "</td>");
//                } else {
//                    htmlContentBuilder.append("<td>" + 0 + "</td>");
//                }

                if (baoCaoKetNoiTable.getData().containsKey(DeviceTypeConstant.DO)) {
                    htmlContentBuilder.append("<td>" + baoCaoKetNoiTable.getData().get(DeviceTypeConstant.DO) + "</td>");
                } else {
                    htmlContentBuilder.append("<td>" + 0 + "</td>");
                }
                htmlContentBuilder.append("</tr>");
            }

            htmlContentBuilder.append("</table></body></html>");
            return htmlContentBuilder.toString();
        } catch (Exception e) {
            String response =
                    "<h2>Báo cáo kết nối cảm biến từ "
                            + timeQueryService.getDateByTimestamp(startTime)
                            + " đến "
                            + timeQueryService.getDateByTimestamp(endTime)
                            + "</h2>";
            response += "<p>Hệ thống xảy ra sự cố trong quá trình tổng hợp dữ liệu!</p>";
            response += "<p>Chi tiết : " + e.getMessage() + "</p>";
            return response;
        }
    }
}
