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
    } else if (baoCaoKetNoi.getDeviceType().equals(DeviceTypeConstant.Humidity)) {
      this.sort = 2;
      this.name = DeviceTypeNameConstant.Humidity;
    } else if (baoCaoKetNoi.getDeviceType().equals(DeviceTypeConstant.Luminosity)) {
      this.sort = 3;
      this.name = DeviceTypeNameConstant.Luminosity;
    }
    this.value = baoCaoKetNoi.getCountValue();
  }
}
