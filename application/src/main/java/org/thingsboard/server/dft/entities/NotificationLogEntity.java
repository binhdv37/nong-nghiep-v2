package org.thingsboard.server.dft.entities;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "damtom_notification_log")
public class NotificationLogEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "email")
    private String email;       // dùng khi gửi email

    @Column(name = "phone")
    private String phone;       // dùng khi gửi sms

    @Column(name = "user_id")
    private UUID userId;        // dùng khi gửi notification

    @Column(name = "type")
    private int type;    // org.thingsboard.server.dft.common.constants.NotiticationTypeConstant

    @Column(name = "created_time")
    private long createdTime;

    @Column(name = "status")
    private int status;     // org.thingsboard.server.dft.common.constants.NotiticationStatusConstant
}
