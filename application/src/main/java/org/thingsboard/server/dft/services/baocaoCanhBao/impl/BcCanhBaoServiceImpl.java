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
                    "<h2 style=\"text-align: center\">B??o c??o c???nh b??o c???a nh?? v?????n t??? " + convertTs(startTs) + " ?????n " + convertTs(endTs) +  "</h2>" +
                            "    <h3>Th???ng k?? c???nh b??o c???a nh?? v?????n</h3>" +
                            "    <table>" +
                            "        <tr>" +
                            "            <th style=\"width: 50%;\">T??n lu???t c???nh b??o</th>" +
                            "            <th style=\"width: 50%;\">S??? l?????ng</th>" +
                            "        </tr>" +
                            "        <tr>" +
                            "            <td style=\"text-align: center;\" colspan=\"2\">Nh?? v?????n kh??ng t???n t???i</td>" +
                            "        </tr>";
        }
        // dam tom dang bi vo hieu hoa
        else if(!damTomEntity.isActive()){
            result +=
                    "<h2 style=\"text-align: center\">B??o c??o c???nh b??o c???a " + damTomEntity.getName() + " t??? " + convertTs(startTs) + " ?????n " + convertTs(endTs) +  "</h2>" +
                            "    <h3>Th???ng k?? c???nh b??o c???a " + damTomEntity.getName() +  "</h3>" +
                            "    <table>" +
                            "        <tr>" +
                            "            <th style=\"width: 50%;\">T??n lu???t c???nh b??o</th>" +
                            "            <th style=\"width: 50%;\">S??? l?????ng</th>" +
                            "        </tr>" +
                            "        <tr>" +
                            "            <td style=\"text-align: center;\" colspan=\"2\">Nh?? v?????n ??ang b??? v?? hi???u h??a</td>" +
                            "        </tr>";
        }
        // dam tom dang hd binh thuong
        else {
            result +=
                    "<h2 style=\"text-align: center\">B??o c??o c???nh b??o c???a " + damTomEntity.getName() + " t??? " + convertTs(startTs) + " ?????n " + convertTs(endTs) + "</h2>" +
                            "    <h3>Th???ng k?? c???nh b??o c???a " + damTomEntity.getName() +  "</h3>" +
                            "    <table>" +
                            "        <tr>" +
                            "            <th style=\"width: 50%;\">T??n lu???t c???nh b??o</th>" +
                            "            <th style=\"width: 50%;\">S??? l?????ng</th>" +
                            "        </tr>";

            // k co data
            if(table1Data.size() == 0) {
                result +=
                        "<tr>" +
                        "    <td style=\"text-align: center;\" colspan=\"2\">Kh??ng c?? c???nh b??o</td>" +
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
                        "    <h3>Th???ng k?? c???nh b??o gi???a c??c nh?? v?????n</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th style=\"width: 50%;\">T??n nh?? v?????n</th>" +
                        "            <th style=\"width: 50%;\">S??? l?????ng</th>" +
                        "        </tr>";

        if(table2Data.size() == 0){
            result +=
                    "<tr>" +
                    "   <td style=\"text-align: center\" colspan=\"2\">Kh??ng t??m th???y nh?? v?????n</td>" +
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
