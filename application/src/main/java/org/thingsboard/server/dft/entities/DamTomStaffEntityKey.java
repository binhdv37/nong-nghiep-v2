package org.thingsboard.server.dft.entities;

import java.io.Serializable;
import java.util.UUID;
import org.thingsboard.server.dao.model.sql.UserEntity;

public class DamTomStaffEntityKey implements Serializable {

    private UUID damtomId;

    private UserEntity staff;
}
