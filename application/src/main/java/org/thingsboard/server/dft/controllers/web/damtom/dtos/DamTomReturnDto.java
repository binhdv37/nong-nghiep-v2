package org.thingsboard.server.dft.controllers.web.damtom.dtos;

import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dft.entities.DamTomGatewayEntity;

import java.util.List;

public class DamTomReturnDto {
    DamTomGatewayEntity Gateway;
    List<DeviceEntity> ListDevices;
    String credentialsId;

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public DamTomReturnDto() {
    }

    public DamTomGatewayEntity getGateway() {
        return Gateway;
    }

    public void setGateway(DamTomGatewayEntity gateway) {
        Gateway = gateway;
    }

    public List<DeviceEntity> getListDevices() {
        return ListDevices;
    }

    public void setListDevices(List<DeviceEntity> listDevices) {
        ListDevices = listDevices;
    }
}
