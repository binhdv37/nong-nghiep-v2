package org.thingsboard.server.dft.controllers.web.lscanhbao.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DataSnapshot {
    private String tenDamTom;
    private UUID gatewayId;
    private String gatewayName;
    private UUID deviceId;
    private String deviceName;
    private Map<String, Double> data;
    private boolean isAlarmGateway;
    private Set<String> alarmKey;
}
