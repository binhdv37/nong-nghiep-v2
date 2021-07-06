package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class RoleId extends UUIDBased implements EntityId{

    @JsonCreator
    public RoleId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static RoleId fromString(String roleId) {
        return new RoleId(UUID.fromString(roleId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.ROLE;
    }
}
