package org.thingsboard.server.dft.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dao.model.sql.DeviceProfileEntity;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "damtom_alarm")
public class DamTomAlarmEntity {
  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id")
  private UUID tenantId;

  @OneToOne
  @JoinColumn(name = "device_profile_id", referencedColumnName = "id")
  private DeviceProfileEntity deviceProfile;

  @Column(name = "damtom_id")
  private UUID damtomId;

  @Column(name = "name")
  private String name;

  @Column(name = "note")
  private String note;

  @Column(name = "via_sms")
  private boolean viaSms;

  @Column(name = "via_email")
  private boolean viaEmail;

  @Column(name = "via_notification")
  private boolean viaNotification;

  // sẽ là id của bảng damtom_rpc_setting hoặc group_rpc_id
  @Column(name = "rpc_setting_id")
  private UUID rpcSettingId;

  // 2 chế độ SINGLE_MODE: chọn bộ thiết bị cụ thể, GROUP_RPC_MODE: chọn nhóm điều khiển
  @Column(name = "rpc_mode")
  private String rpc_mode;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "created_time")
  private long createdTime;

  @Column(name = "alarm_id")
  private String alarmId;
}
