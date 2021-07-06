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
@Table(name = "damtom_rpc_command_queue")
public class RpcCommandQueueEntity {
  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "uuid")
  private UUID tenantId;

  @Column(name = "damtom_id", columnDefinition = "uuid")
  private UUID damTomId;

  @Column(name = "device_id", columnDefinition = "uuid")
  private UUID deviceId;

  @Column(name = "device_name")
  private String deviceName;

  @Column(name = "command")
  private String command;

  @Column(name = "command_status")
  private String commandStatus;

  @Column(name = "group_rpc_id", columnDefinition = "uuid")
  private UUID groupRpcId;

  @Column(name = "rpc_setting_id", columnDefinition = "uuid")
  private UUID rpcSettingId;

  @Column(name = "origin")
  private int origin;

  @Column(name = "command_time")
  private long commandTime;

  @Column(name = "created_time")
  private long createdTime;

  @Column(name = "updated_time")
  private long updatedTime;

  @Column(name = "exception")
  private String exception;

  @Column(name = "viewed")
  private boolean viewed;
}
