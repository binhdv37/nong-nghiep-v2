package org.thingsboard.server.dft.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "damtom_report_ketnoi")
public class BaoCaoKetNoiEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "damtom_id")
    private UUID damtomId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "last_active_time")
    private long lastActiveTime;

    @Column(name = "created_time")
    private long createTime;

}
