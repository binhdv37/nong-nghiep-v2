package org.thingsboard.server.dft.controllers.web.rpc.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class GroupRpcStatus {
    private UUID groupRpcId;
    private boolean isLoading;
    private long timeEndLoading;
}
