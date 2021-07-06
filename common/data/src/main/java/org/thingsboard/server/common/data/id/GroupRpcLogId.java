package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class GroupRpcLogId extends UUIDBased implements EntityId {
    @JsonCreator
    public GroupRpcLogId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static GroupRpcLogId fromString(String groupRpcLogId) {
        return new GroupRpcLogId(UUID.fromString(groupRpcLogId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.GROUP_RPC;
    }
}
