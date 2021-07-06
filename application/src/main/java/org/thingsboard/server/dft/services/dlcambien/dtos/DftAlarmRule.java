package org.thingsboard.server.dft.services.dlcambien.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DftAlarmRule {
    private String key;
    private double value;
    private String operation;
}
