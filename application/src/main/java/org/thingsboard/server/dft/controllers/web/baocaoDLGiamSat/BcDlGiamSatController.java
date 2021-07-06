package org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.common.constants.PermissionConstants;
import org.thingsboard.server.dft.common.dtos.TimeRangeNameDto;
import org.thingsboard.server.dft.common.service.TimeRangeNameService;
import org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto.BcMultiDataDto;
import org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto.SeriesDto;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.baoCaoDLGiamSat.BcDlGiamSatService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class BcDlGiamSatController extends BaseController {

    @Autowired
    BcDlGiamSatService bcDlGiamSatService;

    @Autowired
    TimeRangeNameService timeRangeNameService;

    @Autowired
    DamTomService damTomService;

    private final DecimalFormat df = new DecimalFormat("#.##");


    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_DLGIAMSAT + "\")")
    @GetMapping("/bc-dlgiamsat")
    public ResponseEntity<List<BcMultiDataDto>> getGatewayKeyAvg(
            @RequestParam(name = "key")String key,
            @RequestParam(name = "startTs")long startTs,
            @RequestParam(name = "endTs")long endTs
    ) throws ThingsboardException {
        try{
            List<BcMultiDataDto> result = new ArrayList<>();

            // convert time range
            List<TimeRangeNameDto> timeRangeNameDtos = timeRangeNameService.convertTimeRange(startTs, endTs);

            // get all tenant active damtom
            List<DamTomEntity> damTomEntities = damTomService.getDamTomActiveByTenantIdAndSearchTextMobi(getTenantId().getId(), "");

            // for loop all damtom
            for (DamTomEntity x : damTomEntities) {
                // list series data of 1 damtom
                List<SeriesDto> seriesDtos = new ArrayList<>();

                // for loop all time range
                for (TimeRangeNameDto t : timeRangeNameDtos) {
                    // data of 1 time range
                    double value = bcDlGiamSatService.getDamtomKeyAvgToDouble(getTenantId(),
                            x.getId(), key, t.getStartTs(), t.getEndTs());

                    value = Double.parseDouble(df.format(value));

                    seriesDtos.add(new SeriesDto(t.getName(), value));
                }

                result.add(new BcMultiDataDto(x.getName(), seriesDtos));
            }

            return ResponseEntity.ok(result);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_DLGIAMSAT + "\")")
    @GetMapping("/bc-dlgiamsat/mail")
    public ResponseEntity<String> getGatewayKeyAvg(
            @RequestParam(name = "startTs")long startTs,
            @RequestParam(name = "endTs")long endTs
    ) throws ThingsboardException {
        try{
            UUID tenantId = getCurrentUser().getTenantId().getId();
            String mailContent = bcDlGiamSatService.getMailContentData(tenantId, startTs, endTs);
            return ResponseEntity.ok(mailContent);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }

}
