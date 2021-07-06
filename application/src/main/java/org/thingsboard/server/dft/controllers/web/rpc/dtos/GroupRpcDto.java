package org.thingsboard.server.dft.controllers.web.rpc.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.common.validator.bodieukhien.UniqueGroupRpcName;
import org.thingsboard.server.dft.entities.GroupRpcEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@UniqueGroupRpcName
public class GroupRpcDto {
  private UUID groupRpcId;

  private UUID damTomId;

  @NotBlank
  @Size(max = 255)
  private String name;

  //    @Size(max = 4000)
  //    private String ghiChu;

  private List<DeviceSetting> rpcSettingList;
  private long createdTime;

  public GroupRpcDto(GroupRpcEntity groupRpcEntity) {
    this.groupRpcId = groupRpcEntity.getId();
    this.damTomId = groupRpcEntity.getDamTomId();
    this.name = groupRpcEntity.getTen();
    this.rpcSettingList = new ArrayList<>();
    this.createdTime = groupRpcEntity.getCreatedTime();
    groupRpcEntity
        .getRpcSettingEntities()
        .forEach(
            data -> {
              DeviceSetting deviceSetting = new DeviceSetting(data);
              this.rpcSettingList.add(deviceSetting);
            });
  }
}
