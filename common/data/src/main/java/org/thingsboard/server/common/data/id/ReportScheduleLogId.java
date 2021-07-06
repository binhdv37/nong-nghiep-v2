package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class ReportScheduleLogId extends UUIDBased implements EntityId{
    @JsonCreator
    public ReportScheduleLogId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static ReportScheduleLogId fromString(String id) {
        return new ReportScheduleLogId(UUID.fromString(id));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.REPORT_SCHEDULE;
    }
}
