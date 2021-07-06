package org.thingsboard.server.dft.services.baocaoThongBao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.common.service.TimeQueryService;
import org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto.SeriesDto;
import org.thingsboard.server.dft.entities.NotificationLogEntity;
import org.thingsboard.server.dft.repositories.NotificationLogRepository;
import org.thingsboard.server.dft.services.baocaoThongBao.BcThongBaoService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.thingsboard.server.dft.common.constants.NotiticationStatusConstant.TYPE_ERROR;
import static org.thingsboard.server.dft.common.constants.NotiticationStatusConstant.TYPE_SUCCESS;
import static org.thingsboard.server.dft.common.constants.NotiticationTypeConstant.*;

@Service
public class BcThongBaoServiceImpl implements BcThongBaoService {

  @Autowired private NotificationLogRepository notificationLogRepository;

  @Autowired private TimeQueryService timeQueryService;

  @Override
  public List<NotificationLogEntity> getNotificationByCreatedTimeRange(
      UUID tenantId, long startTs, long endTs) {
    return notificationLogRepository.findNotificationLogByTenantIdAndTimeRange(
        tenantId, startTs, endTs);
  }

  @Override
  public List<SeriesDto> getDataToArraySeriesDto(
      int type, List<NotificationLogEntity> notificationLogEntities) {
    List<SeriesDto> SeriesDtos = new ArrayList<>();

    double success = getValue(type, TYPE_SUCCESS, notificationLogEntities);
    double fail = getValue(type, TYPE_ERROR, notificationLogEntities);
    double total = success + fail;

    SeriesDtos.add(new SeriesDto("Số lần gửi thông tin cảnh báo", total));
    SeriesDtos.add(new SeriesDto("Số lần thành công", success));
    SeriesDtos.add(new SeriesDto("Số lần thất bại", fail));
    return SeriesDtos;
  }

  @Override
  public String getMailContentData(UUID tenantId, long startTs, long endTs) {
    StringBuilder htmlContentBuilder = new StringBuilder();
    try {
      htmlContentBuilder.append(
          "<html> <head> <style> table, td, th { border: 1px solid black; } table { width: 100%; border-collapse: collapse; } </style> </head> <body>");
      htmlContentBuilder.append("<h2>Báo cáo thông báo</h2>");
      htmlContentBuilder.append("<table style=\"text-align: center;\">");
      htmlContentBuilder.append("<tr>");
      htmlContentBuilder.append("<th style=\"width: 20%; background: yellow\"></th>");
      htmlContentBuilder.append(
          "<th style=\"width: 25%; background: yellow\">Số lần gửi thông tin cảnh báo</th>");
      htmlContentBuilder.append(
          "<th style=\"width: 20%; background: yellow\">Số lần gửi thành công</th>");
      htmlContentBuilder.append(
          "<th style=\"width: 20%; background: yellow\">Số lần gửi thất bại</th>");
      htmlContentBuilder.append("</tr>");

      List<NotificationLogEntity> notificationLogEntities =
          getNotificationByCreatedTimeRange(tenantId, startTs, endTs);
      double success1 = getValue(TYPE_NOTIFICATION, TYPE_SUCCESS, notificationLogEntities);
      double fail1 = getValue(TYPE_NOTIFICATION, TYPE_ERROR, notificationLogEntities);
      double total1 = success1 + fail1;
      htmlContentBuilder.append("<tr>");
      htmlContentBuilder.append("<td>Notification</td>");
      htmlContentBuilder.append("<td>" + total1 + "</td>");
      htmlContentBuilder.append("<td>" + success1 + "</td>");
      htmlContentBuilder.append("<td>" + fail1 + "</td>");
      htmlContentBuilder.append("</tr>");

      double success2 = getValue(TYPE_SMS, TYPE_SUCCESS, notificationLogEntities);
      double fail2 = getValue(TYPE_SMS, TYPE_ERROR, notificationLogEntities);
      double total2 = success2 + fail2;
      htmlContentBuilder.append("<tr>");
      htmlContentBuilder.append("<td>Tin nhắn (SMS)</td>");
      htmlContentBuilder.append("<td>" + total2 + "</td>");
      htmlContentBuilder.append("<td>" + success2 + "</td>");
      htmlContentBuilder.append("<td>" + fail2 + "</td>");
      htmlContentBuilder.append("</tr>");

      double success3 = getValue(TYPE_EMAIL, TYPE_SUCCESS, notificationLogEntities);
      double fail3 = getValue(TYPE_EMAIL, TYPE_ERROR, notificationLogEntities);
      double total3 = success3 + fail3;
      htmlContentBuilder.append("<tr>");
      htmlContentBuilder.append("<td>Email</td>");
      htmlContentBuilder.append("<td>" + total3 + "</td>");
      htmlContentBuilder.append("<td>" + success3 + "</td>");
      htmlContentBuilder.append("<td>" + fail3 + "</td>");
      htmlContentBuilder.append("</tr>");

      htmlContentBuilder.append("</table></body></html>");
      return htmlContentBuilder.toString();
    } catch (Exception e) {
      String response =
          "<h2>Báo cáo gửi thông báo từ "
              + timeQueryService.getDateByTimestamp(startTs)
              + " đến "
              + timeQueryService.getDateByTimestamp(endTs)
              + "</h2>";
      response += "<p>Hệ thống xảy ra sự cố trong quá trình tổng hợp dữ liệu!</p>";
      response += "<p>Chi tiết : " + e.getMessage() + "</p>";
      return response;
    }
  }

  public double getValue(
      int type, int status, List<NotificationLogEntity> notificationLogEntities) {
    long result = 0;
    for (NotificationLogEntity n : notificationLogEntities) {
      if (n.getType() == type && n.getStatus() == status) {
        result++;
      }
    }
    return result;
  }
}
