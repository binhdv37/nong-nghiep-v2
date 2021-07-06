package org.thingsboard.server.dft.entities;

import org.thingsboard.server.dao.model.sql.UserEntity;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "damtom_user_avatar")
public class UserAvatarEntity {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "avatar")
    private String avatar;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
