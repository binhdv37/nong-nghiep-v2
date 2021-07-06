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
            String response = "<h2>Báo cáo tổng hợp của đầm tôm từ "
                    + convertTs(startTs) + " đến " + convertTs(endTs) + "</h2>";
            response += "<p>Hệ thống xảy ra sự cố trong quá trình tổng hợp dữ liệu!</p>";
            response += "<p>Chi tiết : " + e.getMessage() + "</p>";
            return response;
        }
    }

    // get tenant bc tong hop mail content
    private String getTenantMailContentData(UUID tenantId, long startTs, long endTs) throws ExecutionException, InterruptedException {

        String title = "Báo cáo tổng hợp của tất cả các đầm tôm từ "
                + convertTs(startTs) + " đến " + convertTs(endTs);

        String canhbaoData = "", phData = "", domanData = "",
                nhietdoData = "", oxyhoatanData = "";

        // get all active dam tom :
        List<DamTomEntity> activeDamtomList =
                damTomService.getDamTomActiveByTenantIdAndSearchTextMobi(tenantId, "");

        List<UUID> activeDamtomIdList = new ArrayList<>();
        activeDamtomList.forEach(x -> activeDamtomIdList.add(x.getId()));

        if(activeDamtomList.size() == 0){
            canhbaoData = phData = domanData = nhietdoData = oxyhoatanData =
                            "<tr>" +
                            "   <td colspan=\"2\" style=\"text-align:center\">Không tìm thấy đầm tôm nào</td>" +
                            "</tr>";
            return getMailTemplate(title, canhbaoData, phData,
                    domanData, nhietdoData, oxyhoatanData);
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

            phData +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" +
                            df.format(bcDlGiamSatService.getTenantActiveDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    TelemetryConstant.pH, x.getStartTs(), x.getEndTs())) +
                    "   </td>" +
                    "</tr>" ;

            domanData +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" +
                            df.format(bcDlGiamSatService.getTenantActiveDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    TelemetryConstant.Salinity, x.getStartTs(), x.getEndTs())) +
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

            oxyhoatanData +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" +
                            df.format(bcDlGiamSatService.getTenantActiveDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    TelemetryConstant.DO, x.getStartTs(), x.getEndTs())) +
                    "   </td>" +
                    "</tr>" ;

//            oxyhoakhuData +=
//                    "<tr>" +
//                    "   <td>" + x.getName() + "</td>" +
//                    "   <td>" +
//                            df.format(bcDlGiamSatService.getTenantActiveDamtomKeyAvgToDouble(new TenantId(tenantId),
//                                    TelemetryConstant.ORP, x.getStartTs(), x.getEndTs())) +
//                    "   </td>" +
//                    "</tr>" ;
        }

        return getMailTemplate(title, canhbaoData, phData,
                domanData, nhietdoData, oxyhoatanData);
    }

    // get damtom bc tong hop mail content
    private String getDamtomMailContentData(UUID tenantId, UUID damtomId, long startTs, long endTs) throws ExecutionException, InterruptedException {

        String title = "";

        String canhbaoData = "", phData = "", domanData = "",
                nhietdoData = "", oxyhoatanData = "";

        // get damtom :
        DamTomEntity damTomEntity = damTomService.getDamTomById(tenantId, damtomId);

        if(damTomEntity == null){
            title = "Báo cáo tổng hợp của đầm tôm từ " + convertTs(startTs) +
                    " đến " + convertTs(endTs);
            canhbaoData = phData = domanData = nhietdoData = oxyhoatanData =
                    "<tr>" +
                    "   <td colspan=\"2\" style=\"text-align:center\">Đầm tôm không tồn tại</td>" +
                    "</tr>";
            return getMailTemplate(title, canhbaoData, phData, domanData,
                    nhietdoData, oxyhoatanData);
        }

        if(!damTomEntity.isActive()){
            title = "Báo cáo tổng hợp của " + damTomEntity.getName() +
                    " từ " + convertTs(startTs) + " đến " + convertTs(endTs);
            canhbaoData = phData = domanData = nhietdoData = oxyhoatanData =
                    "<tr>" +
                    "   <td colspan=\"2\" style=\"text-align:center\">Đầm tôm đang bị vô hiệu hóa</td>" +
                    "</tr>";
            return getMailTemplate(title, canhbaoData, phData, domanData,
                    nhietdoData, oxyhoatanData);
        }

        List<TimeRangeNameDto> timeRangeNameDtoList = timeRangeNameService.convertTimeRange(startTs, endTs);

        title = "Báo cáo tổng hợp của " + damTomEntity.getName() +
                " từ " + convertTs(startTs) + " đến " + convertTs(endTs);

        for(TimeRangeNameDto x : timeRangeNameDtoList){
            canhbaoData +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" +
                            countDamtomCanhbao(damtomId, x.getStartTs(), x.getEndTs()) +
                    "   </td>" +
                    "</tr>" ;

            phData +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" +
                            df.format(bcDlGiamSatService.getDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    damtomId, TelemetryConstant.pH, x.getStartTs(), x.getEndTs())) +
                    "   </td>" +
                    "</tr>" ;

            domanData +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" +
                            df.format(bcDlGiamSatService.getDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    damtomId, TelemetryConstant.Salinity, x.getStartTs(), x.getEndTs())) +
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

            oxyhoatanData +=
                    "<tr>" +
                    "   <td>" + x.getName() + "</td>" +
                    "   <td>" +
                            df.format(bcDlGiamSatService.getDamtomKeyAvgToDouble(new TenantId(tenantId),
                                    damtomId, TelemetryConstant.DO, x.getStartTs(), x.getEndTs())) +
                    "   </td>" +
                    "</tr>" ;

//            oxyhoakhuData +=
//                    "<tr>" +
//                    "   <td>" + x.getName() + "</td>" +
//                    "   <td>" +
//                            df.format(bcDlGiamSatService.getDamtomKeyAvgToDouble(new TenantId(tenantId),
//                                    damtomId, TelemetryConstant.ORP, x.getStartTs(), x.getEndTs())) +
//                    "   </td>" +
//                    "</tr>" ;
        }

        return getMailTemplate(title, canhbaoData, phData, domanData,
                nhietdoData, oxyhoatanData);
    }

    private String getMailTemplate(String title, String canhbaoData, String phData, String domanData,
                                   String nhietdoData, String oxyhoatanData){
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
                        "    <h3>Tổng số lần cảnh báo</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th>Thời gian</th>" +
                        "            <th>Số lượng</th>" +
                        "        </tr>" +
                                 canhbaoData +
                        "    </table>" +
                        "" +
                        "    <div style=\"min-height: 50px;\"></div>" +
                        "" +
                        "    <h3>Độ ph trung bình</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th>Thời gian</th>" +
                        "            <th>Giá trị</th>" +
                        "        </tr>" +
                                 phData +
                        "    </table>" +
                        "" +
                        "    <div style=\"min-height: 50px;\"></div>" +
                        "" +
                        "    <h3>Độ mặn trung bình</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th>Thời gian</th>" +
                        "            <th>Giá trị</th>" +
                        "        </tr>" +
                                 domanData +
                        "    </table>" +
                        "" +
                        "    <div style=\"min-height: 50px;\"></div>" +
                        "" +
                        "    <h3>Nhiệt độ trung bình</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th>Thời gian</th>" +
                        "            <th>Giá trị</th>" +
                        "        </tr>" +
                                 nhietdoData +
                        "    </table>" +
                        "" +
                        "    <div style=\"min-height: 50px;\"></div>" +
                        "" +
                        "    <h3>Độ oxy hòa tan trung bình</h3>" +
                        "    <table>" +
                        "        <tr>" +
                        "            <th>Thời gian</th>" +
                        "            <th>Giá trị</th>" +
                        "        </tr>" +
                                 oxyhoatanData +
                        "    </table>" +
//                        "" +
//                        "    <div style=\"min-height: 50px;\"></div>" +
//                        "" +
//                        "    <h3>Độ oxy hóa khử trung bình</h3>" +
//                        "    <table>" +
//                        "        <tr>" +
//                        "            <th>Thời gian</th>" +
//                        "            <th>Giá trị</th>" +
//                        "        </tr>" +
//                                 oxyhoakhuData +
//                        "    </table>" +
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
