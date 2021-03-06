package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class CameraLogId extends UUIDBased implements EntityId{
    @JsonCreator
    public CameraLogId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static CameraLogId fromString(String cameraId) {
        return new CameraLogId(UUID.fromString(cameraId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.CAMERA;
    }
}
