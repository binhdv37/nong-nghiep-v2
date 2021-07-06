package org.thingsboard.server.dft.common.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.id.UUIDBased;

import java.util.UUID;

public class DamTomId extends UUIDBased {
    private static final long serialVersionUID = 1L;

    @JsonCreator
    public DamTomId(@JsonProperty("id") UUID id) {
        super(id);
    }
}
