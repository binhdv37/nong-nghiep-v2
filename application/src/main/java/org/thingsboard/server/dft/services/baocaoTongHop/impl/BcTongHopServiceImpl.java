package org.thingsboard.server.dft.services.baocaoTongHop.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dft.common.constants.TelemetryConstant;
import org.thingsboard.server.dft.common.dtos.TimeRangeNameDto;
import org.thingsboard.server.dft.common.service.TimeRangeNameService;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.repositories.DamTomSnapshotRepository;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.baoCaoDLGiamSat.BcDlGiamSatService;
import org.thingsboard.server.dft.services.baocaoCanhBao.BcCanhBaoService;
import org.thingsboard.server.dft.services.baocaoTongHop.BcTongHopService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class BcTongHopServiceImpl implements BcTongHopService {

    @Autowired
    DamTomSnapshotRepository damTomSnapshotRepository;

    @Autowired
    TimeRangeNameService timeRangeNameService;

    @Autowired
    BcCanhBaoService bcCanhBaoService;

    @Autowired
    DamTomService damTomService;

    @Autowired
    BcDlGiamSatService bcDlGiamSatService;

    private static DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public long countDamtomCanhbao(UUID damtomId, long startTs, long endTs) {
        return damTomSnapshotRepository.countAllByDamtomIdAndTimeRange(damtomId, startTs, endTs);
    }

    @Override
    public long countTenantActiveDamtomCanhBao(UUID tenantId, List<UUID> activeDamtomIdList, long startTs, long endTs) {
        return damTomSnapshotRepository.countAllByTenantIDAndDamtomIdInAndTimeRange(tenantId, activeDamtomIdList, startTs, endTs);
    }

    @Override
    public String getMailContentData(UUID tenantId, UUID damtomId, long startTs, long endTs) {
        try{
            if(damtomId == null) return getTenantMailContentData(tenantId, startTs, endTs);
            return getDamtomMailContentData(tenantId, damtomId, startTs, endTs);
        }
        catch (Exception e){
            String response = "<h2>B??o c??o t???ng h???p c???a nh?? v?????n t??? "
                    + convertTs(startTs) + " ?????n " + convertTs(endTs) + "</h2>";
            response += "<p>H??? th???ng x???y ra s??? c??? trong qu?? tr??nh t???ng h???p d??? li???u!</p>";
            response += "<p>Chi ti???t : " + e.getMessage() + "</p>";
            return response;
        }
    }

    // get tenant bc tong hop mail content
    private String getTenantMailContentData(UUID tenantId, long startTs, long endTs) throws ExecutionException, InterruptedException {

        String title = "B??o c??o t???ng h???p c???a t???t c??? c??c nh?? v?????n t??? "
                + convertTs(startTs) + " ?????n " + convertTs(endTs);

        String canhbaoData = "", nhietdoData = "", doamData = "", dosangData = "";

        // get all active dam tom :
        List<DamTomEntity> activeDamtomList =
                damTomService.getDamTomActiveByTenantIdAndSearchTextMobi(tenantId, "");

        List<UUID> activeDamtomIdList = new ArrayList<>();
        activeDamtomList.forEach(x -> activeDamtomIdList.add(x.getId()));

        if(activeDamtomList.size() == 0){
            canhbaoData = nhietdoData = doamData = dosangData =
                    "<tr>" +
                            "   <td colspan=\"2\" style=\"text-align:center\">Kh??ng t??m th???y nh?? v?????n n??o</td>" +
                            "</tr>";
            return getMailTemplate(title, canhbaoData, nhietdoData,
                    doamData, dosangData);
        }

        List<TimeRangeNameDto> timeRangeNameDtoList = timeRangeNameService.convertTimeRange(startTs, endTs);

        for(TimeRangeNameDto x : timeRangeNameDtoList){
            canhbaoData +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" +
                            countTenantActiveDamtomCanhBao(tenantId, activeDamtomIdList, x.getStartTs(), x.getEndTs()) +
                    "   </td>" +
                    "</tr>" ;

            nhietdoData +=
                    "<tr>" +
                            "   <td>" + x.getName() + "</td>" +
                            "   <td>" +
                            df.format(bcDlGiamSatService.getTenantActiveDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    TelemetryConstant.Temperature, x.getStartTs(), x.getEndTs())) +
                            "   </td>" +
                            "</tr>" ;

            doamData +=
                    "<tr>" +
                            "   <td>" + x.getName() + "</td>" +
                            "   <td>" +
                            df.format(bcDlGiamSatService.getTenantActiveDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    TelemetryConstant.Humidity, x.getStartTs(), x.getEndTs())) +
                            "   </td>" +
                            "</tr>" ;

            dosangData +=
                    "<tr>" +
                            "   <td>" + x.getName() + "</td>" +
                            "   <td>" +
                            df.format(bcDlGiamSatService.getTenantActiveDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    TelemetryConstant.Luminosity, x.getStartTs(), x.getEndTs())) +
                            "   </td>" +
                            "</tr>" ;

        }

        return getMailTemplate(title, canhbaoData, nhietdoData,
                doamData, dosangData);
    }

    // get damtom bc tong hop mail content
    private String getDamtomMailContentData(UUID tenantId, UUID damtomId, long startTs, long endTs) throws ExecutionException, InterruptedException {

        String title = "";

        String canhbaoData = "", nhietdoData = "", doamData = "", dosangData = "";

        // get damtom :
        DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damtomId);

        if(damTomEntity == null){
            title = "B??o c??o t???ng h???p c???a nh?? v?????n t??? " + convertTs(startTs) +
                    " ?????n " + convertTs(endTs);
            canhbaoData = nhietdoData  = doamData = dosangData =
                    "<tr>" +
                            "   <td colspan=\"2\" style=\"text-align:center\">Nh?? v?????n kh??ng t???n t???i</td>" +
                            "</tr>";
            return getMailTemplate(title, canhbaoData, nhietdoData, doamData, dosangData);
        }

        if(!damTomEntity.isActive()){
            title = "B??o c??o t???ng h???p c???a " + damTomEntity.getName() +
                    " t??? " + convertTs(startTs) + " ?????n " + convertTs(endTs);
            canhbaoData = nhietdoData  = doamData = dosangData =
                    "<tr>" +
                            "   <td colspan=\"2\" style=\"text-align:center\">Nh?? v?????n ??ang b??? v?? hi???u h??a</td>" +
                            "</tr>";
            return getMailTemplate(title, canhbaoData, nhietdoData, doamData, dosangData);
        }

        List<TimeRangeNameDto> timeRangeNameDtoList = timeRangeNameService.convertTimeRange(startTs, endTs);

        title = "B??o c??o t???ng h???p c???a " + damTomEntity.getName() +
                " t??? " + convertTs(startTs) + " ?????n " + convertTs(endTs);

        for(TimeRangeNameDto x : timeRangeNameDtoList){
            canhbaoData +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" +
                            countDamtomCanhbao(damtomId, x.getStartTs(), x.getEndTs()) +
                    "   </td>" +
                    "</tr>" ;

            nhietdoData +=
                    "<tr>" +
                            "   <td>" + x.getName() + "</td>" +
                            "   <td>" +
                            df.format(bcDlGiamSatService.getDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    damtomId, TelemetryConstant.Temperature, x.getStartTs(), x.getEndTs())) +
                            "   </td>" +
                            "</tr>" ;

            doamData +=
                    "<tr>" +
                            "   <td>" + x.getName() + "</td>" +
                            "   <td>" +
                            df.format(bcDlGiamSatService.getDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    damtomId, TelemetryConstant.Humidity, x.getStartTs(), x.getEndTs())) +
                            "   </td>" +
                            "</tr>" ;

            dosangData +=
                    "<tr>" +
                            "   <td>" + x.getName() + "</td>" +
                            "   <td>" +
                            df.format(bcDlGiamSatService.getDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    damtomId, TelemetryConstant.Luminosity, x.getStartTs(), x.getEndTs())) +
                            "   </td>" +
                            "</tr>" ;

        }

        return getMailTemplate(title, canhbaoData, nhietdoData, doamData, dosangData);
    }

    private String getMailTemplate(String title, String canhbaoData,
                                   String nhietdoData, String doamData, String dosangData){
        String result =
                "<html>" +
                        "<head>" +
                        "    <style>" +
                        "        table {" +
                        "            font-family: Arial, Helvetica, sans-serif;" +
                        "            border-collapse: collapse;" +
                        "            width: 60%;" +
                        "        }" +
                        "" +
                        "        table td," +
                        "        table th {" +
                        "            border: 1px solid #ddd;" +
                        "            padding: 10px;" +
                        "        }" +
                        "" +
                        "        th {" +
                        "            width: 50%;" +
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
                        "<body>" +
                        "    <h2 style=\"text-align: center\">" + title + "</h2>" +
                        "" +
                        "    <h3>T???ng s??? l???n c???nh b??o</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th>Th???i gian</th>" +
                        "            <th>S??? l?????ng</th>" +
                        "        </tr>" +
                                 canhbaoData +
                        "    </table>" +
                        "" +
                        "    <div style=\"min-height: 50px;\"></div>" +
                        "" +
                        "    <h3>Nhi???t ????? trung b??nh</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th>Th???i gian</th>" +
                        "            <th>Gi?? tr???</th>" +
                        "        </tr>" +
                                 nhietdoData +
                        "    </table>" +
                        "" +
                        "    <div style=\"min-height: 50px;\"></div>" +
                        "" +
                        "    <h3>????? ???m trung b??nh</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th>Th???i gian</th>" +
                        "            <th>Gi?? tr???</th>" +
                        "        </tr>" +
                                 doamData +
                        "    </table>" +
                        "" +
                        "    <div style=\"min-height: 50px;\"></div>" +
                        "" +
                        "    <h3>????? s??ng trung b??nh</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th>Th???i gian</th>" +
                        "            <th>Gi?? tr???</th>" +
                        "        </tr>" +
                                 dosangData +
                        "    </table>" +
                        "</body>" +
                        "</html>";
        return result;
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
