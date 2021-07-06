package org.thingsboard.server.dft.controllers.web.baocaoCanhBao;

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
import org.thingsboard.server.dft.controllers.web.baocaoCanhBao.dto.BcSingleDataDto;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.services.DamTomService;
import org.thingsboard.server.dft.services.baocaoCanhBao.BcCanhBaoService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class BcCanhBaoController extends BaseController {

    @Autowired
    BcCanhBaoService bcCanhBaoService;

    @Autowired
    DamTomService damTomService;

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_CANHBAO + "\")")
    @GetMapping("/bc-canhbao/dam-tom")
    public ResponseEntity<List<BcSingleDataDto>> getDamtomBcCanhBaoData(
            @RequestParam(name = "damtomId") UUID damtomId,
            @RequestParam(name = "startTs") long startTs,
            @RequestParam(name = "endTs") long endTs
    )
    throws ThingsboardException {
        try{
            List<BcSingleDataDto> datas = bcCanhBaoService.getBcCanhBaoDataByDamtomId(damtomId, startTs, endTs);
            return ResponseEntity.ok(checkNotNull(datas));
        }
        catch (Exception e){
            throw handleException(e);
        }
    }

    // tong so canh bao cac dam dang kich hoat trong tenant
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_CANHBAO + "\")")
    @GetMapping("/bc-canhbao/tenant")
    public ResponseEntity<List<BcSingleDataDto>> getTenantBcCanhbaoData(
            @RequestParam(name = "startTs") long startTs,
            @RequestParam(name = "endTs") long endTs
    ) throws ThingsboardException{
        try{
            List<BcSingleDataDto> result = new ArrayList<>();

            // get all active damtom :
            List<DamTomEntity> activeDamtomEntities = damTomService
                    .getDamTomActiveByTenantIdAndSearchTextMobi(getTenantId().getId(), "");

            for(DamTomEntity x : activeDamtomEntities){
                BcSingleDataDto b = new BcSingleDataDto(x.getName(),
                        bcCanhBaoService.countDamtomCanhBao(x.getId(), startTs, endTs));
                result.add(b);
            }

            return ResponseEntity.ok(result);
        }
        catch (Exception e){
            throw handleException(e);
        }
    }

    // just for testing purpose
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN',\"" + PermissionConstants.PAGES_BC_CANHBAO + "\")")
    @GetMapping("/bc-canhbao/mail-content")
    public ResponseEntity<?> getMailContent(
            @RequestParam(name = "damtomId") UUID damtomId,
            @RequestParam(name = "startTs") long startTs,
            @RequestParam(name = "endTs") long endTs
    )
            throws ThingsboardException{
        try{
            return ResponseEntity.ok(bcCanhBaoService.getMailContentData(getTenantId().getId(),
                    damtomId, startTs, endTs));
        }
        catch (Exception e){
            throw handleException(e);
        }
    }
}
