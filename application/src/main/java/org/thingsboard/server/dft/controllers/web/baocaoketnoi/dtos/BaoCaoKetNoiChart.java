package org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.common.constants.DeviceTypeConstant;
import org.thingsboard.server.dft.common.constants.DeviceTypeNameConstant;

@Data
@NoArgsConstructor
public class BaoCaoKetNoiChart {
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private int sort;

  private String name;
  private long value;

  public BaoCaoKetNoiChart(BaoCaoKetNoi baoCaoKetNoi) {
    if (baoCaoKetNoi.getDeviceType().equals(DeviceTypeConstant.Temperature)) {
      this.sort = 1;
      this.name = DeviceTypeNameConstant.Temperature;
    } else if (baoCaoKetNoi.getDeviceType().equals(DeviceTypeConstant.pH)) {
      this.sort = 2;
      this.name = DeviceTypeNameConstant.pH;
    } else if (baoCaoKetNoi.getDeviceType().equals(DeviceTypeConstant.Salinity)) {
      this.sort = 3;
      this.name = DeviceTypeNameConstant.Salinity;
//    } else if (baoCaoKetNoi.getDeviceType().equals(DeviceTypeConstant.ORP)) {
//      this.sort = 5;
//      this.name = DeviceTypeNameConstant.ORP;
    } else if (baoCaoKetNoi.getDeviceType().equals(DeviceTypeConstant.DO)) {
      this.sort = 4;
      this.name = DeviceTypeNameConstant.DO;
    }
    this.value = baoCaoKetNoi.getCountValue();
  }
}
