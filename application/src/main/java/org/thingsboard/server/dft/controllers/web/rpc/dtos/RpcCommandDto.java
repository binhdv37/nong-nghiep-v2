package org.thingsboard.server.dft.controllers.web.rpc.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class RpcCommandDto {
    private UUID damTomId;
    private UUID deviceId;
    private String deviceName;
    private String setValueMethod;
    private double valueControl;
    private boolean callbackOption;
    private long timeCallback;
    private boolean loopOption;
    private int loopCount;
    private long loopTimeStep;
}
