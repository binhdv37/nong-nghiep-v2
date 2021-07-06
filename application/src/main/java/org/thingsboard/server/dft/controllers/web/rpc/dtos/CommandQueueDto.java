package org.thingsboard.server.dft.controllers.web.rpc.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.entities.RpcCommandQueueEntity;
import org.thingsboard.server.dft.services.rpc.dtos.RpcRequest;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CommandQueueDto {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    protected final ObjectMapper jsonMapper = new ObjectMapper();


    private UUID id;
    private UUID deviceId;
    private String deviceName;
    private String label;
    private double valueControl;
    private String status;
    private int origin;
    private long commandTime;

    public CommandQueueDto(RpcCommandQueueEntity rpcCommandQueueEntity) {
        this.id = rpcCommandQueueEntity.getId();
        this.deviceId = rpcCommandQueueEntity.getDeviceId();
        this.deviceName = rpcCommandQueueEntity.getDeviceName();
        this.status = rpcCommandQueueEntity.getCommandStatus();
        this.origin = rpcCommandQueueEntity.getOrigin();
        this.commandTime = rpcCommandQueueEntity.getCommandTime();
        try {
            RpcRequest request =
                    jsonMapper.readValue(rpcCommandQueueEntity.getCommand(), RpcRequest.class);
            this.valueControl = (double) request.getParams().get("value");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
