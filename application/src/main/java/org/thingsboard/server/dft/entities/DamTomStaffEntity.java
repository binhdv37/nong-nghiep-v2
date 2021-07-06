package org.thingsboard.server.dft.entities;

import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.thingsboard.server.dao.model.sql.UserEntity;

@Entity @IdClass(DamTomStaffEntityKey.class)
@Table(name = "damtom_staff")
public class DamTomStaffEntity {
    @Id
    @Column(name = "damtom_id", columnDefinition = "uuid")
    private UUID damtomId;

    @Id
    @ManyToOne
    @JoinColumn(name = "staff_id", referencedColumnName = "id")
    private UserEntity staff;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_time")
    private long createdTime;

    public DamTomStaffEntity() {
    }

    public UUID getDamtomId() {
        return damtomId;
    }

    public void setDamtomId(UUID damtomId) {
        this.damtomId = damtomId;
    }

    public UserEntity getStaff() {
        return staff;
    }

    public void setStaff(UserEntity staff) {
        this.staff = staff;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
