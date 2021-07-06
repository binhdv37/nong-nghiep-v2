package org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaoCaoKetNoi {
    private String deviceType;
    private long countValue;

    public BaoCaoKetNoi(String deviceType, long countValue) {
        this.deviceType = deviceType;
        this.countValue = countValue;
    }
}
