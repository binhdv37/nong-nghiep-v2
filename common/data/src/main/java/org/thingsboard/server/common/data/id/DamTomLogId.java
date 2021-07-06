package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class DamTomLogId extends UUIDBased implements EntityId{

    @JsonCreator
    public DamTomLogId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static DamTomLogId fromString(String damTomLogId) {
        return new DamTomLogId(UUID.fromString(damTomLogId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.DAM_TOM;
    }
}
