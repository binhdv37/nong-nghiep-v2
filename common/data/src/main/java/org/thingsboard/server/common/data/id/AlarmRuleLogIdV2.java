package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class AlarmRuleLogIdV2 extends UUIDBased implements EntityId {
    @JsonCreator
    public AlarmRuleLogIdV2(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static AlarmRuleLogIdV2 fromString(String alarmRuleLogIdV2) {
        return new AlarmRuleLogIdV2(UUID.fromString(alarmRuleLogIdV2));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.ALARM_RULE;
    }
}
