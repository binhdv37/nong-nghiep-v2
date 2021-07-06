package org.thingsboard.server.dft.controllers.web.baocaoketnoi.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class SaveBaoCaoKetNoi {
    private UUID deviceId;
    private UUID tenantId;
    private UUID damTomId;
    private long lastActiveTime;
}
