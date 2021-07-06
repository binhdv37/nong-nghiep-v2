package org.thingsboard.server.dft.controllers.web.rpc.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ThietBiDieuKhien {
  private String tenThietBi;
  private UUID damTomId;
  private String label;
  private UUID deviceId;
  //    private String getValueMethod;
  private String setValueMethod;
  private String statusDevice;
  private long statusTime;
}
