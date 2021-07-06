package org.thingsboard.server.dft.services.dlcambien.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class DftTelemetry {
    private long ts;
    private double value;
    private boolean alarm;
    private UUID deviceId;
}
