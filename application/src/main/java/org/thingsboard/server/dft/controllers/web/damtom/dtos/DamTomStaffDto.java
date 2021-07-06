package org.thingsboard.server.dft.controllers.web.damtom.dtos;

import org.thingsboard.server.dao.model.sql.UserEntity;

import java.util.UUID;

public class DamTomStaffDto {
    private UUID damtomId;
    private UserEntity staff;
    private UUID createdBy;
    private long createdTime;
}
