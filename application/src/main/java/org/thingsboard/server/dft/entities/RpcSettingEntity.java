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
@Table(name = "damtom_rpc_setting")
public class RpcSettingEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "device_id", columnDefinition = "uuid")
    private UUID deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "value_control")
    private double valueControl;

    @Column(name = "delay_time")
    private long delayTime;

    @Column(name = "callback_option")
    private boolean callbackOption;

    @Column(name = "time_callback")
    private long timeCallback;

    @Column(name = "loop_option")
    private boolean loopOption;

    @Column(name = "loop_count")
    private int loopCount;

    @Column(name = "loop_time_step")
    private long loopTimeStep;

    @Column(name = "group_rpc_id", columnDefinition = "uuid")
    private UUID groupRpcId;

    @Column(name = "command_id")
    private int commandId;

    @Column(name = "created_time")
    private long createdTime;
}
