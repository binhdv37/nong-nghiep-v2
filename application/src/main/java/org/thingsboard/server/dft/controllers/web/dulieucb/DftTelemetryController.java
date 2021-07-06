package org.thingsboard.server.dft.controllers.web.dulieucb;


import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.timeseries.TimeseriesService;
import org.thingsboard.server.dft.services.dlcambien.DftTelemetryService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@TbCoreComponent
@RequestMapping("/api/telemetry")
@Slf4j
public class DftTelemetryController extends BaseController {

    private final DftTelemetryService dftTelemetryService;
    private final TimeseriesService tsService;

    @Autowired
    public DftTelemetryController(DftTelemetryService dftTelemetryService,
                                  TimeseriesService tsService) {
        this.dftTelemetryService = dftTelemetryService;
        this.tsService = tsService;
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/{entityId}/lastest/timeseries", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getLatestTimeseries2(
            @PathVariable("entityId") UUID entityId,
            @RequestParam(name = "keys", required = false) String keys)
            throws ThingsboardException, ExecutionException, InterruptedException {
        SecurityUser user = getCurrentUser();

        ListenableFuture<List<TsKvEntry>> future;
        if (StringUtils.isEmpty(keys)) {
            future = tsService.findAllLatest(user.getTenantId(), new DeviceId(entityId));
        } else {
            future = tsService.findLatest(user.getTenantId(), new DeviceId(entityId), toKeysList(keys));
        }
        return new ResponseEntity<>(dftTelemetryService.toDftTelemetry(user.getTenantId(),
                entityId, future.get()), HttpStatus.OK);
    }


    private List<String> toKeysList(String keys) {
        List<String> keyList = null;
        if (!StringUtils.isEmpty(keys)) {
            keyList = Arrays.asList(keys.split(","));
        }
        return keyList;
    }


}
