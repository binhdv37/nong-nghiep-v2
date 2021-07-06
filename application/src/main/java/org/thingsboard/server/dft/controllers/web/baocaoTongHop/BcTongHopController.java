package org.thingsboard.server.dft.controllers.web.baocaoTongHop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.thingsboard.server.dft.controllers.web.baocaoCanhBao.dto.BcSingleDataDto;
import org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto.BcMultiDataDto;
import org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto.SeriesDto;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.baoCaoDLGiamSat.BcDlGiamSatService;
import org.thingsboard.server.dft.services.baocaoTongHop.BcTongHopService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class BcTongHopController extends BaseController {

    @Autowired
    BcTongHopService bcTongHopService;

    @Autowired
    BcDlGiamSatService bcDlGiamSatService;

    @Autowired
    DamTomService damTomService;

    @Autowired
    TimeRangeNameService timeRangeNameService;

    private final DecimalFormat df = new DecimalFormat("#.##");

    // get bar chart data : tong so lan canh bao
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_TONGHOP + "\")")
    @GetMapping("/bc-tonghop/canh-bao")
    public ResponseEntity<List<BcSingleDataDto>> getDamtomCanhBaoData(
            @RequestParam(name = "damtomId", required = false) UUID damtomId,
            @RequestParam(name = "startTs") long startTs,
            @RequestParam(name = "endTs") long endTs
            ) throws ThingsboardException {
        try{
            List<BcSingleDataDto> result = new ArrayList<>();

            List<TimeRangeNameDto> timeRangeNameDtoList = timeRangeNameService.convertTimeRange(startTs, endTs);

            // damtomId == null => tong hop tat ca cac dam tom
            if(damtomId == null){

                List<UUID> activeDamtomIdList = new ArrayList<>();

                // find  all active damtom
                List<DamTomEntity> activeDamtomEntities =
                        damTomService.getDamTomActiveByTenantIdAndSearchTextMobi(getTenantId().getId(), "");

                activeDamtomEntities.forEach(x -> activeDamtomIdList.add(x.getId()));

                for(TimeRangeNameDto x : timeRangeNameDtoList){
                    BcSingleDataDto b = new BcSingleDataDto(x.getName() ,
                            bcTongHopService.countTenantActiveDamtomCanhBao(getTenantId().getId(),
                                    activeDamtomIdList, x.getStartTs(), x.getEndTs()));
                    result.add(b);
                }

                return ResponseEntity.ok(result);
            }


            for(TimeRangeNameDto x : timeRangeNameDtoList){
                BcSingleDataDto b = new BcSingleDataDto(x.getName(),
                        bcTongHopService.countDamtomCanhbao(damtomId, x.getStartTs(), x.getEndTs()));
                result.add(b);
            }

            return ResponseEntity.ok(result);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }

    // get line chart data : dl cam bien cua 1 dam tom, 1 key
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_TONGHOP + "\")")
    @GetMapping("/bc-tonghop/dl-cambien")
    public ResponseEntity<List<BcMultiDataDto>> getDamtomKeyDlcambienData(
            @RequestParam(name = "damtomId", required = false) UUID damtomId,
            @RequestParam(name = "key")String key,
            @RequestParam(name = "startTs") long startTs,
            @RequestParam(name = "endTs") long endTs
    ) throws ThingsboardException{
        try{
            List<BcMultiDataDto> result = new ArrayList<>();

            List<SeriesDto> seriesDtoList = new ArrayList<>();

            List<TimeRangeNameDto> timeRangeNameDtoList = timeRangeNameService.convertTimeRange(startTs, endTs);


            // damtomId == null => tong hop tat ca cac dam tom
            if(damtomId == null){
                for(TimeRangeNameDto x : timeRangeNameDtoList){
                    double value = bcDlGiamSatService
                            .getTenantActiveDamtomKeyAvgToDouble(getTenantId(), key, x.getStartTs(), x.getEndTs());

                    value = Double.parseDouble(df.format(value));

                    seriesDtoList.add(new SeriesDto(x.getName(), value));
                }

                result.add(new BcMultiDataDto("Tất cả", seriesDtoList));

                return ResponseEntity.ok(result);
            }


            // find damtom by id
            DamTomEntity damTomEntity = damTomService.getDamTomById(getTenantId().getId(), damtomId);

            if(damTomEntity == null){
                return new ResponseEntity("Cannot find damtom", HttpStatus.BAD_REQUEST);
            }

            for(TimeRangeNameDto x : timeRangeNameDtoList){
                double value = bcDlGiamSatService
                        .getDamtomKeyAvgToDouble(getTenantId(), damtomId, key, x.getStartTs(), x.getEndTs());

                value = Double.parseDouble(df.format(value));

                seriesDtoList.add(new SeriesDto(x.getName(), value));
            }

            result.add(new BcMultiDataDto(damTomEntity.getName(), seriesDtoList));

            return ResponseEntity.ok(result);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }

    // get mail content - just for testing purpose
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_TONGHOP + "\")")
    @GetMapping("/bc-tonghop/mail-content")
    public ResponseEntity<?> getMailContent(
            @RequestParam(name = "damtomId", required = false) UUID damtomId,
            @RequestParam("startTs") long startTs,
            @RequestParam("endTs") long endTs
    ) throws ThingsboardException{
        try{
            String mailContent = bcTongHopService.getMailContentData(getTenantId().getId(),
                    damtomId, startTs, endTs);

            return ResponseEntity.ok(mailContent);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }
}
