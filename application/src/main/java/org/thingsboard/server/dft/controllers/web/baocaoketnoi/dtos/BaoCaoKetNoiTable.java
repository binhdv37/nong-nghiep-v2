package org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class BaoCaoKetNoiTable {
    private String thoiGian;
    private Map<String, Long> data;
}
