package org.thingsboard.server.dft.services.rpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.GroupRpcDto;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.GroupRpcStatus;
import org.thingsboard.server.dft.entities.GroupRpcEntity;
import org.thingsboard.server.dft.entities.RpcSettingEntity;
import org.thingsboard.server.dft.repositories.GroupRpcRepository;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BoDieuKhienService {

  private final GroupRpcRepository groupRpcRepository;

  private final RpcCommandQueueService rpcCommandQueueService;

  @Autowired
  public BoDieuKhienService(
      GroupRpcRepository groupRpcRepository, RpcCommandQueueService rpcCommandQueueService) {
    this.groupRpcRepository = groupRpcRepository;
    this.rpcCommandQueueService = rpcCommandQueueService;
  }

  public List<GroupRpcDto> findAllByTenantId(UUID tenantId, UUID damTomId) {
    List<GroupRpcDto> groupRpcDtoList =
        groupRpcRepository.findAllByTenantIdAndDamTomId(tenantId, damTomId).stream()
            .map(GroupRpcDto::new)
            .collect(Collectors.toList());
    return groupRpcDtoList;
  }

  public GroupRpcDto findByTenantIdAndId(UUID tenantId, UUID damTomId, UUID id) {
    GroupRpcEntity groupRpcEntity = groupRpcRepository.findByTenantIdAndId(tenantId, id);
    return new GroupRpcDto(groupRpcEntity);
  }

  public GroupRpcDto findByTenantIdAndId(UUID tenantId, UUID id) {
    GroupRpcEntity groupRpcEntity = groupRpcRepository.findByTenantIdAndId(tenantId, id);
    return new GroupRpcDto(groupRpcEntity);
  }

  public GroupRpcDto saveGroupRpc(SecurityUser securityUser, GroupRpcDto groupRpcDto) {
    GroupRpcEntity groupRpcEntity;
    if (groupRpcDto.getGroupRpcId() == null) {
      groupRpcEntity = new GroupRpcEntity();
      groupRpcDto.setGroupRpcId(UUID.randomUUID());
      groupRpcEntity.setCreatedBy(securityUser.getUuidId());
      groupRpcEntity.setCreatedTime(new Date().getTime());
    } else {
      groupRpcEntity = groupRpcRepository.findById(groupRpcDto.getGroupRpcId()).get();
    }
    groupRpcEntity.setId(groupRpcDto.getGroupRpcId());
    groupRpcEntity.setTenantId(securityUser.getTenantId().getId());
    groupRpcEntity.setDamTomId(groupRpcDto.getDamTomId());
    groupRpcEntity.setTen(groupRpcDto.getName().trim());

    Collection<RpcSettingEntity> groupRpcEntitySet = new HashSet<>();
    groupRpcDto
        .getRpcSettingList()
        .forEach(
            deviceSetting -> {
              RpcSettingEntity rpcSettingEntity = new RpcSettingEntity();
              if (deviceSetting.getId() == null) {
                rpcSettingEntity.setId(UUID.randomUUID());
              } else {
                rpcSettingEntity.setId(deviceSetting.getId());
              }
              rpcSettingEntity.setDeviceId(deviceSetting.getDeviceId());
              rpcSettingEntity.setDeviceName(deviceSetting.getDeviceName());
              rpcSettingEntity.setValueControl(deviceSetting.getValueControl());
              rpcSettingEntity.setDelayTime(deviceSetting.getDelayTime());
              rpcSettingEntity.setCreatedTime(new Date().getTime());

              rpcSettingEntity.setCallbackOption(deviceSetting.isCallbackOption());
              if (rpcSettingEntity.isCallbackOption()) {
                rpcSettingEntity.setTimeCallback(deviceSetting.getTimeCallback());
              }

              rpcSettingEntity.setLoopOption(deviceSetting.isLoopOption());
              if (rpcSettingEntity.isCallbackOption()) {
                rpcSettingEntity.setLoopCount(deviceSetting.getLoopCount());
                rpcSettingEntity.setLoopTimeStep(deviceSetting.getLoopTimeStep());
              }
              rpcSettingEntity.setGroupRpcId(groupRpcEntity.getId());
              rpcSettingEntity.setCommandId(groupRpcDto.getRpcSettingList().indexOf(deviceSetting));
              rpcSettingEntity.setCreatedTime(new Date().getTime());
              groupRpcEntitySet.add(rpcSettingEntity);
            });

    groupRpcEntity.setRpcSettingEntities(groupRpcEntitySet);
    GroupRpcEntity savedGroupRpcEntity = groupRpcRepository.save(groupRpcEntity);
    return new GroupRpcDto(savedGroupRpcEntity);
  }

  public void delete(UUID tenantId, UUID id) {
    groupRpcRepository.deleteByTenantIdAndId(tenantId, id);
  }

  public void startGroupRpc(UUID tenantId, UUID groupId) {
    GroupRpcEntity groupRpcEntity = groupRpcRepository.findByTenantIdAndId(tenantId, groupId);
    try {
      rpcCommandQueueService.saveGroupRpcCommand(groupRpcEntity);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  public void stopGroupRpc(UUID tenantId, UUID groupId) {
    rpcCommandQueueService.cancelAllRpcCommandById(tenantId, groupId);
  }

  public GroupRpcStatus checkStatusGroupRpcById(UUID tenantId, UUID groupId) {
    return rpcCommandQueueService.checkGroupRpcInProcessing(tenantId, groupId);
  }
}
