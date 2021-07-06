package org.thingsboard.server.dft.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "damtom_snapshot")
public class DamTomSnapshotEntity implements Serializable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "damtom_id")
    private UUID damtomId;

    @Column(name = "alarm_id")
    private UUID alarmId;

    @Column(name = "alarm_name")
    private String alarmName;

    @Column(name = "data_snapshot")
    private String dataSnapshot;

    @Column(name = "time_snapshot")
    private long timeSnapshot;

    @Column(name = "clear")
    private boolean clear;

}
