package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class AlarmRuleLogId extends UUIDBased implements EntityId{
    @JsonCreator
    public AlarmRuleLogId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static AlarmRuleLogId fromString(String alarmRuleLogId) {
        return new AlarmRuleLogId(UUID.fromString(alarmRuleLogId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.ALARM_RULE;
    }
}
