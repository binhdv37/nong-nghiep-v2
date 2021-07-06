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
@Table(name = "damtom_rpc_scheduler")
public class RpcScheduleEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "damtom_id", columnDefinition = "uuid")
    private UUID damTomId;

    @Column(name = "name")
    private String name;

    @Column(name = "rpc_setting_id", columnDefinition = "uuid")
    private UUID rpcSettingId;

    @Column(name = "rpc_group_id", columnDefinition = "uuid")
    private UUID rpcGroupId;

    @Column(name = "cron")
    private String cron;

    @Column(name = "active")
    private boolean active;


    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "created_time")
    private long createdTime;
}
