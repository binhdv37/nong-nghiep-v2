package org.thingsboard.server.dft.services.baoCaoDLGiamSat.impl;

import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.Aggregation;
import org.thingsboard.server.common.data.kv.BaseReadTsKvQuery;
import org.thingsboard.server.common.data.kv.ReadTsKvQuery;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.RelationEntity;
import org.thingsboard.server.dao.timeseries.TimeseriesService;
import org.thingsboard.server.dft.common.constants.TelemetryConstant;
import org.thingsboard.server.dft.common.dtos.TimeRangeNameDto;
import org.thingsboard.server.dft.common.service.TimeQueryService;
import org.thingsboard.server.dft.common.service.TimeRangeNameService;
import org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto.BcDlGiamSatTable;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomGatewayEntity;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;
import org.thingsboard.server.dft.repositories.DftRelationRepository;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.baoCaoDLGiamSat.BcDlGiamSatService;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class BcDlGiamSatServiceImpl implements BcDlGiamSatService {

  @Autowired TimeseriesService timeseriesService;

  @Autowired DamTomService damTomService;

  @Autowired TimeRangeNameService timeRangeNameService;

  @Autowired DftRelationRepository relationRepository;

  @Autowired DftDeviceRepository dftDeviceRepository;

  @Autowired TimeQueryService timeQueryService;

  private static DecimalFormat df = new DecimalFormat("0.00");

  // get tb du lieu cua 1 key, 1 dam tom, trong 1 khoang time
  public double getDamtomKeyAvgToDouble(
      TenantId tenantId, UUID damtomId, String key, long startTs, long endTs)
      throws ExecutionException, InterruptedException {
    // get all gateway from damtom
    List<DeviceEntity> damtomGateways = getAllGatewayFromDamTom(tenantId.getId(), damtomId);

    // for loop gateway :
    List<Double> listData = new ArrayList<>();
    for (DeviceEntity gateway : damtomGateways) {
      listData.add(getGatewayKeyAvgToDouble(tenantId, gateway.getId(), key, startTs, endTs));
    }
    return listData.stream().mapToDouble(x -> x).average().orElse(0);
  }

  // get tb du lieu cua 1 key, các active damtom của 1 tenant,  1 khoang time
  @Override
  public double getTenantActiveDamtomKeyAvgToDouble(
      TenantId tenantId, String key, long startTs, long endTs)
      throws ExecutionException, InterruptedException {
    // get all active  damtom :
    List<DamTomEntity> activeDamtomEntities =
        damTomService.getDamTomActiveByTenantIdAndSearchTextMobi(tenantId.getId(), "");

    if (activeDamtomEntities.size() == 0) return 0;

    double sumValue = 0;

    for (DamTomEntity d : activeDamtomEntities) {
      sumValue += getDamtomKeyAvgToDouble(tenantId, d.getId(), key, startTs, endTs);
    }

    return sumValue / (activeDamtomEntities.size());
  }

  @Override
  public String getMailContentData(UUID tenantId, long startTime, long endTime)
      throws ExecutionException, InterruptedException {
    try {
      List<BcDlGiamSatTable> bcDlGiamSatTables = dataTableConstructor(tenantId, startTime, endTime);

      StringBuilder htmlContentBuilder = new StringBuilder();
      htmlContentBuilder.append(
          "<html> <head> <style> table, td, th { border: 1px solid black; } table { width: 100%; border-collapse: collapse; } </style> </head> <body>");
      htmlContentBuilder.append("<h2>Báo cáo dữ liệu giám sát</h2>");
      htmlContentBuilder.append("<table style=\"text-align: center;\">");
      htmlContentBuilder.append("<tr>");
      htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Thời gian</th>");
      htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Nhà vườn</th>");
      htmlContentBuilder.append("<th style=\"width: 15%; background: yellow\">Nhiệt độ</th>");
      htmlContentBuilder.append("<th style=\"width: 15%; background: yellow\">Độ ẩm</th>");
      htmlContentBuilder.append("<th style=\"width: 15%; background: yellow\">Độ sáng</th>");
//      htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Oxy hóa khử</th>");
//      htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\">Oxy hòa tan</th>");
      htmlContentBuilder.append("</tr>");

      for (BcDlGiamSatTable bcDlGiamSatTable : bcDlGiamSatTables) {
        htmlContentBuilder.append("<tr>");
        if (bcDlGiamSatTable.getSpan() > 1) {
          if (!bcDlGiamSatTable.isHidden()) {
            htmlContentBuilder.append(
                "<td rowspan=\""
                    + bcDlGiamSatTable.getSpan()
                    + "\">"
                    + bcDlGiamSatTable.getDate()
                    + "</td>");
          }
        } else {
          htmlContentBuilder.append("<td>" + bcDlGiamSatTable.getDate() + "</td>");
        }
        htmlContentBuilder.append("<td>" + bcDlGiamSatTable.getTenDam() + "</td>");

        if (bcDlGiamSatTable.getData().containsKey(TelemetryConstant.Temperature)) {
          htmlContentBuilder.append(
              "<td>" + df.format(bcDlGiamSatTable.getData().get(TelemetryConstant.Temperature)) + "</td>");
        } else {
          htmlContentBuilder.append("<td></td>");
        }

        if (bcDlGiamSatTable.getData().containsKey(TelemetryConstant.Humidity)) {
          htmlContentBuilder.append(
              "<td>"
                  + df.format(bcDlGiamSatTable.getData().get(TelemetryConstant.Humidity))
                  + "</td>");
        } else {
          htmlContentBuilder.append("<td></td>");
        }

        if (bcDlGiamSatTable.getData().containsKey(TelemetryConstant.Luminosity)) {
          htmlContentBuilder.append(
                  "<td>"
                          + df.format(bcDlGiamSatTable.getData().get(TelemetryConstant.Luminosity))
                          + "</td>");
        } else {
          htmlContentBuilder.append("<td></td>");
        }

//        if (bcDlGiamSatTable.getData().containsKey(TelemetryConstant.ORP)) {
//          htmlContentBuilder.append(
//              "<td>" + df.format(bcDlGiamSatTable.getData().get(TelemetryConstant.ORP)) + "</td>");
//        } else {
//          htmlContentBuilder.append("<td></td>");
//        }
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

  // avg cua 1 key, 1 gateway, 1 khoang time, to double
  private double getGatewayKeyAvgToDouble(
      TenantId tenantId, UUID gatewayId, String key, long startTs, long endTs)
      throws ExecutionException, InterruptedException {

    List<TsKvEntry> tsKvEntries = this.getGatewayKeyAvg(tenantId, gatewayId, key, startTs, endTs);

    // no data
    if (tsKvEntries.size() == 0) return 0.0;

    // get data
    TsKvEntry entry = tsKvEntries.get(0);

    // get avg value in double
    return entry.getDoubleValue().orElse(0.0);
  }

  // get all gateway from dam tom :
  private List<DeviceEntity> getAllGatewayFromDamTom(UUID tenantId, UUID damTomId) {
    DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damTomId);

    // dam tom k ton tai
    if (damTomEntity == null) return Collections.EMPTY_LIST;

    List<DeviceEntity> deviceEntities = getAllGatewayFromAssert(damTomEntity.getAsset().getId());
    Collection<DamTomGatewayEntity> damTomGatewayEntities = damTomEntity.getGateways();
    if (damTomGatewayEntities == null || damTomGatewayEntities.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    // remove cac gateway chua active
    damTomGatewayEntities.forEach(
        damTomGatewayEntity -> {
          if (!damTomGatewayEntity.isActive()) {
            deviceEntities.remove(damTomGatewayEntity.getDevice());
          }
        });
    return deviceEntities;
  }

  // get all gateway from asset
  private List<DeviceEntity> getAllGatewayFromAssert(UUID assetId) {
    List<DeviceEntity> listGateway = new ArrayList<>();
    List<RelationEntity> listRelationAssert =
        relationRepository.findAllByFromIdAndFromTypeAndToType(assetId, "ASSET", "DEVICE");
    listRelationAssert.forEach(
        relationEntity -> {
          listGateway.add(dftDeviceRepository.findDeviceEntityById(relationEntity.getToId()));
        });
    return listGateway;
  }

  /*
     - response :
        + no data : []
        + co data
             [
                 {
                     "ts": 1618462800000,
                     "value": 30.0,
                     "key": "do am",
                     "valueAsString": "30.0",
                     "booleanValue": null,
                     "doubleValue": 30.0,
                     "longValue": null,
                     "dataType": "DOUBLE",
                     "jsonValue": null,
                     "strValue": null
                 }
              ]
  */
  // avg cua 1 key, 1 gateway, 1 khoang time
  private List<TsKvEntry> getGatewayKeyAvg(
      TenantId tenantId, UUID gatewayId, String key, long startTs, long endTs)
      throws ExecutionException, InterruptedException {

    List<ReadTsKvQuery> queries = new ArrayList<>();

    long interval = endTs - startTs;

    queries.add(new BaseReadTsKvQuery(key, startTs, endTs, interval, 0, Aggregation.AVG, "DESC"));

    DeviceId deviceId = new DeviceId(gatewayId);
    ListenableFuture<List<TsKvEntry>> listenableFuture =
        timeseriesService.findAll(tenantId, deviceId, queries);
    return listenableFuture.get();
  }

  private List<BcDlGiamSatTable> dataTableConstructor(UUID tenantId, long startTs, long endTs)
      throws ExecutionException, InterruptedException {

    List<BcDlGiamSatTable> bcDlGiamSatTables = new ArrayList<>();

    List<TimeRangeNameDto> timeRangeNameDtos =
        timeRangeNameService.convertTimeRange(startTs, endTs);

    List<DamTomEntity> damTomEntities =
        damTomService.getDamTomActiveByTenantIdAndSearchTextMobi(tenantId, "");

    for (TimeRangeNameDto t : timeRangeNameDtos) {
      for (DamTomEntity x : damTomEntities) {
        BcDlGiamSatTable bcDlGiamSatTable = new BcDlGiamSatTable();

        bcDlGiamSatTable.setHidden(!damTomEntities.get(0).equals(x));

        bcDlGiamSatTable.setDate(t.getName());
        bcDlGiamSatTable.setSpan(damTomEntities.size());
        bcDlGiamSatTable.setTenDam(x.getName());

        Map<String, Double> data = new HashMap<>();
        double value1 =
            getDamtomKeyAvgToDouble(
                new TenantId(tenantId),
                x.getId(),
                TelemetryConstant.Temperature,
                t.getStartTs(),
                t.getEndTs());
        data.put(TelemetryConstant.Temperature, value1);
        double value2 =
            getDamtomKeyAvgToDouble(
                new TenantId(tenantId),
                x.getId(),
                TelemetryConstant.Humidity,
                t.getStartTs(),
                t.getEndTs());
        data.put(TelemetryConstant.Humidity, value2);
        double value3 =
            getDamtomKeyAvgToDouble(
                new TenantId(tenantId),
                x.getId(),
                TelemetryConstant.Luminosity,
                t.getStartTs(),
                t.getEndTs());
        data.put(TelemetryConstant.Luminosity, value3);

        bcDlGiamSatTable.setData(data);
        bcDlGiamSatTables.add(bcDlGiamSatTable);
      }
    }

    return bcDlGiamSatTables;
  }
}
