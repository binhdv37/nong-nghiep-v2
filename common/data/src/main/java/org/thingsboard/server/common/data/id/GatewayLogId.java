package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class GatewayLogId extends UUIDBased implements EntityId{
    @JsonCreator
    public GatewayLogId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static GatewayLogId fromString(String gatewayId) {
        return new GatewayLogId(UUID.fromString(gatewayId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.GATEWAY;
    }
}
