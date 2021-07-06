package org.thingsboard.server.dft.entities;

import java.io.Serializable;
import java.util.UUID;
import org.thingsboard.server.dao.model.sql.UserEntity;

public class ReportScheduleUserEntityKey implements Serializable {

    private UUID scheduleId;

    private UserEntity user;
}
