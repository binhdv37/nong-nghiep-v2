package org.thingsboard.server.dft.common.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeRange {
    private long startTime;
    private long endTime;
}
