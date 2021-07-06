package org.thingsboard.server.dft.controllers.web.lscanhbao.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DamTomSnapshot {
  private UUID id;
  private UUID damTomId;
  private String tenDamTom;
  private long thoiGian;
  private UUID alarmId;
  private String tenCanhBao;
  private UUID gatewayId;
  private String gatewayName;
  private UUID deviceId;
  private String deviceName;
  private Map<String, Double> data;
  private boolean clear;
  private int span;
  private boolean display;
  private boolean isAlarmGateway;
  private Set<String> alarmKeys;
}
