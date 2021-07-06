package org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class BcDlGiamSatTable {
    private String date;
    private int span;
    private String tenDam;
    private Map<String, Double> data;
    private boolean hidden;
}
