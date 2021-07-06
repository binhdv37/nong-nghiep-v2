package org.thingsboard.server.dft.services.baocaoCanhBao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.controllers.web.baocaoCanhBao.dto.BcSingleDataDto;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.repositories.DamTomSnapshotRepository;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.baocaoCanhBao.BcCanhBaoService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@Service
public class BcCanhBaoServiceImpl implements BcCanhBaoService {

    @Autowired
    private DamTomSnapshotRepository damTomSnapshotRepository;

    @Autowired
    private DamTomService damTomService;

    @Override
    public List<BcSingleDataDto> getBcCanhBaoDataByDamtomId(UUID damtomId, long startTs, long endTs) {
        return damTomSnapshotRepository.countCanhBaoByDamtomIdAndAlarmName(damtomId, startTs, endTs);
    }

    @Override
    public long countDamtomCanhBao(UUID damtomId, long startTs, long endTs) {
        return damTomSnapshotRepository.countAllByDamtomIdAndTimeRange(damtomId, startTs, endTs);
    }

    @Override
    public String getMailContentData(UUID tenantId, UUID damtomId, long startTs, long endTs) {

        // get damtom :
        DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damtomId);

        // damtom bao cao canh bao data
        List<BcSingleDataDto> table1Data = getBcCanhBaoDataByDamtomId(damtomId, startTs, endTs);

        // tenant bao cao canh bao data
        List<BcSingleDataDto> table2Data = new ArrayList<>();

        // get all active damtom :
        List<DamTomEntity> activeDamtomEntities = damTomService
                .getDamTomActiveByTenantIdAndSearchTextMobi(tenantId, "");

        for(DamTomEntity x : activeDamtomEntities){
            BcSingleDataDto b = new BcSingleDataDto(x.getName(),
                    countDamtomCanhBao(x.getId(), startTs, endTs));
            table2Data.add(b);
        }

        String result =
                "<html>" +
                        "<head>" +
                        "    <style>" +
                        "        table {" +
                        "            font-family: Arial, Helvetica, sans-serif;" +
                        "            border-collapse: collapse;" +
                        "            width: 100%;" +
                        "        }" +
                        "" +
                        "        table td," +
                        "        table th {" +
                        "            border: 1px solid #ddd;" +
                        "            padding: 10px;" +
                        "        }" +
                        "" +
                        "        table tr:hover {" +
                        "            background-color: #ddd;" +
                        "        }" +
                        "" +
                        "        table th {" +
                        "            padding-top: 12px;" +
                        "            padding-bottom: 12px;" +
                        "            text-align: left;" +
                        "            background-color: #4CAF50;" +
                        "            color: white;" +
                        "        }" +
                        "    </style>" +
                        "</head>" +
                        "<body>";

        // dam tom k ton tai
        if(damTomEntity == null){
            result +=
                    "<h2 style=\"text-align: center\">Báo cáo cảnh báo của đầm tôm từ " + convertTs(startTs) + " đến " + convertTs(endTs) +  "</h2>" +
                            "    <h3>Thống kê cảnh báo của đầm tôm</h3>" +
                            "    <table>" +
                            "        <tr>" +
                            "            <th style=\"width: 50%;\">Tên luật cảnh báo</th>" +
                            "            <th style=\"width: 50%;\">Số lượng</th>" +
                            "        </tr>" +
                            "        <tr>" +
                            "            <td style=\"text-align: center;\" colspan=\"2\">Đầm tôm không tồn tại</td>" +
                            "        </tr>";
        }
        // dam tom dang bi vo hieu hoa
        else if(!damTomEntity.isActive()){
            result +=
                    "<h2 style=\"text-align: center\">Báo cáo cảnh báo của " + damTomEntity.getName() + " từ " + convertTs(startTs) + " đến " + convertTs(endTs) +  "</h2>" +
                            "    <h3>Thống kê cảnh báo của " + damTomEntity.getName() +  "</h3>" +
                            "    <table>" +
                            "        <tr>" +
                            "            <th style=\"width: 50%;\">Tên luật cảnh báo</th>" +
                            "            <th style=\"width: 50%;\">Số lượng</th>" +
                            "        </tr>" +
                            "        <tr>" +
                            "            <td style=\"text-align: center;\" colspan=\"2\">Đầm tôm đang bị vô hiệu hóa</td>" +
                            "        </tr>";
        }
        // dam tom dang hd binh thuong
        else {
            result +=
                    "<h2 style=\"text-align: center\">Báo cáo cảnh báo của " + damTomEntity.getName() + " từ " + convertTs(startTs) + " đến " + convertTs(endTs) + "</h2>" +
                            "    <h3>Thống kê cảnh báo của " + damTomEntity.getName() +  "</h3>" +
                            "    <table>" +
                            "        <tr>" +
                            "            <th style=\"width: 50%;\">Tên luật cảnh báo</th>" +
                            "            <th style=\"width: 50%;\">Số lượng</th>" +
                            "        </tr>";

            // k co data
            if(table1Data.size() == 0) {
                result +=
                        "<tr>" +
                        "    <td style=\"text-align: center;\" colspan=\"2\">Không có cảnh báo</td>" +
                        "</tr>";
            }

            for(BcSingleDataDto x : table1Data){
                result +=
                        "<tr>" +
                        "     <td>" + x.getName() + "</td>" +
                        "     <td>" + x.getValue() + "</td>" +
                        "</tr>";
            }

        }

        // add data of table 2 :
        result +=
                        "    </table>" +
                        "" +
                        "    <div style=\"min-height: 50px;  \"></div>" +
                        "" +
                        "    <h3>Thống kê cảnh báo giữa các đầm</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th style=\"width: 50%;\">Tên đầm tôm</th>" +
                        "            <th style=\"width: 50%;\">Số lượng</th>" +
                        "        </tr>";

        if(table2Data.size() == 0){
            result +=
                    "<tr>" +
                    "   <td style=\"text-align: center\" colspan=\"2\">Không tìm thấy đầm tôm</td>" +
                    "</tr>";
        }

        for(BcSingleDataDto x : table2Data){
            result +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" + x.getValue() + "</td>" +
                    "</tr>";
        }

        result +=
                "   </table>" +
                "</body>" +
                "</html>";

        return  result;
    }

    // convert timestamp (milliseconds) to day ( 7/11/1997 )
    private String convertTs(long timestamp){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        return String.format("%d/%d/%d", mDay, mMonth + 1, mYear);
    }



}
