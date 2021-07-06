package org.thingsboard.server.dft.controllers.web.users.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.audit.AuditLog;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;

@Data
@NoArgsConstructor
public class ExcelDto {

    private TenantId tenantId;
    private CustomerId customerId;
    private EntityId entityId;
    private UserId userId;
    private long createdTime;
    private String entityName;
    private String actionType;
    private String actionStatus;
    private String entityType;
    private String userName;

    public ExcelDto(AuditLog auditLog) {
        this.tenantId = auditLog.getTenantId();
        this.customerId = auditLog.getCustomerId();
        this.entityId = auditLog.getEntityId();
        this.userId = auditLog.getUserId();
        this.createdTime = auditLog.getCreatedTime();
        this.entityType = convertEntityType(auditLog.getEntityId().getEntityType().toString());
        this.entityName = auditLog.getEntityName();
        this.userName = auditLog.getUserName();
        this.actionType = convertActionType(auditLog.getActionType().toString());
        this.actionStatus = convertActionStatus(auditLog.getActionStatus().toString());
    }

    // binhdv
    private String convertEntityType(String entityType){
        switch (entityType) {
            case "USER" :
                return "Tài khoản";
            case "ROLE" :
                return "Vai trò";
            case "DAM_TOM" :
                return "Nhà vườn";
            case "DEVICE" :
                return "Thiết bị";
            case "GATEWAY" :
                return "Bộ thiết bị";
            case "ALARM" :
                return "Cảnh báo";
            case "CAMERA" :
                return "Camera";
            case "ALARM_RULE" :
                return "Luật cảnh báo";
            case "GROUP_RPC" :
                return "Bộ điều khiển";
            case "REPORT_SCHEDULE" :
                return "Lịch xuất báo cáo";
            default :
                return entityType;
        }
    }

    private String convertActionType(String actionType){
        switch (actionType) {
            case "ADDED" :
                return "Thêm mới";
            case "DELETED" :
                return "Xóa";
            case "UPDATED" :
                return "Cập nhật";
            case "LOGIN" :
                return "Đăng nhập";
            case "LOGOUT" :
                return "Đăng xuất";
            case "RPC_CALL" :
                return "Điều khiển thiết bị";
            case "ALARM_CLEAR" :
                return "Xử lí cảnh báo";
            default:
                return actionType;
        }
    }

    private String convertActionStatus(String actionStatus){
        switch (actionStatus){
            case "SUCCESS" :
                return "Thành công";
            case "FAILURE" :
                return "Thất bại";
            default :
                return actionStatus;
        }
    }
}
