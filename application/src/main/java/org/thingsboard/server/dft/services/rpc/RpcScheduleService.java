package org.thingsboard.server.dft.services.rpc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.RpcScheduleDto;
import org.thingsboard.server.dft.entities.RpcScheduleEntity;
import org.thingsboard.server.dft.entities.RpcSettingEntity;
import org.thingsboard.server.dft.repositories.DftDeviceRepository;
import org.thingsboard.server.dft.repositories.RpcScheduleRepository;
import org.thingsboard.server.dft.repositories.RpcSettingRepository;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class RpcScheduleService {
  private Map<String, ScheduledFuture<?>> schedules;

  private final ThreadPoolTaskScheduler taskScheduler;

  private final RpcScheduleRepository rpcScheduleRepository;
  private final RpcCommandQueueService rpcCommandQueueService;
  private final DftDeviceRepository dftDeviceRepository;
  private final RpcSettingRepository rpcSettingRepository;

  @Autowired
  public RpcScheduleService(
      ThreadPoolTaskScheduler taskScheduler,
      RpcScheduleRepository rpcScheduleRepository,
      RpcCommandQueueService rpcCommandQueueService,
      DftDeviceRepository dftDeviceRepository,
      RpcSettingRepository rpcSettingRepository) {
    this.taskScheduler = taskScheduler;
    this.rpcScheduleRepository = rpcScheduleRepository;
    this.rpcCommandQueueService = rpcCommandQueueService;
    this.dftDeviceRepository = dftDeviceRepository;
    this.rpcSettingRepository = rpcSettingRepository;
  }

  @PostConstruct
  public void scheduleRunnableWithCronTrigger() throws ThingsboardException {
    this.schedules = new HashMap<>();

    List<RpcScheduleEntity> schedules = rpcScheduleRepository.findAllByActive(true);
    for (RpcScheduleEntity schedule : schedules) {
      try {
        this.addTask(schedule.getId().toString(), schedule.getCron(), createScheduleTask(schedule));
      } catch (Exception e) {
        throw new ThingsboardException(e.getMessage(), ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }
    }
  }

  private RpcScheduleService.RunnableTask createScheduleTask(RpcScheduleEntity schedule) {
    return new RpcScheduleService.RunnableTask(schedule);
  }

  private void addTask(String scheduleId, String cron, RpcScheduleService.RunnableTask task) {
    ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(cron));
    this.schedules.put(scheduleId, future);
  }

  private void removeTask(String scheduleId) {
    ScheduledFuture<?> future = this.schedules.get(scheduleId);
    if (future == null) {
      return;
    }
    future.cancel(true);
    this.schedules.remove(scheduleId);
  }

  private void updateTask(String scheduleId, String cron, RpcScheduleService.RunnableTask task) {
    this.removeTask(scheduleId);
    this.addTask(scheduleId, cron, task);
  }

  public RpcScheduleDto createRpcSchedule(RpcScheduleDto rpcScheduleDto, SecurityUser securityUser)
      throws ThingsboardException {
    RpcScheduleEntity entity = new RpcScheduleEntity();

    entity.setId(UUID.randomUUID());
    entity.setTenantId(securityUser.getTenantId().getId());
    entity.setDamTomId(rpcScheduleDto.getDamTomId());
    entity.setName(rpcScheduleDto.getName());
    entity.setCron(rpcScheduleDto.getCron());
    entity.setActive(rpcScheduleDto.isActive());
    entity.setCreatedBy(securityUser.getId().getId());
    entity.setCreatedTime(new Date().getTime());

    if (rpcScheduleDto.getGroupRpcId() != null) {
      entity.setRpcGroupId(rpcScheduleDto.getGroupRpcId());
      entity.setRpcSettingId(null);
    } else {
      RpcSettingEntity rpcSettingEntity = new RpcSettingEntity();
      rpcSettingEntity.setId(UUID.randomUUID());
      DeviceEntity deviceEntity = dftDeviceRepository.findById(rpcScheduleDto.getDeviceId()).get();
      rpcSettingEntity.setDeviceId(deviceEntity.getId());
      rpcSettingEntity.setDeviceName(deviceEntity.getName());
      rpcSettingEntity.setValueControl(rpcScheduleDto.getValueControl());
      rpcSettingEntity.setDelayTime(0);
      rpcSettingEntity.setCallbackOption(rpcScheduleDto.isCallbackOption());
      rpcSettingEntity.setTimeCallback(rpcScheduleDto.getTimeCallback());
      rpcSettingEntity.setLoopOption(rpcScheduleDto.isLoopOption());
      rpcSettingEntity.setLoopCount(rpcScheduleDto.getLoopCount());
      rpcSettingEntity.setLoopTimeStep(rpcScheduleDto.getLoopTimeStep());
      rpcSettingEntity.setCreatedTime(new Date().getTime());
      RpcSettingEntity rpcSettingSaved = rpcSettingRepository.save(rpcSettingEntity);

      entity.setRpcSettingId(rpcSettingSaved.getId());
      entity.setRpcGroupId(null);
    }

    RpcScheduleEntity result = rpcScheduleRepository.save(entity);
    if (result.isActive()) {
      try {
        this.addTask(result.getId().toString(), result.getCron(), createScheduleTask(entity));
      } catch (Exception e) {
        throw new ThingsboardException(e.getMessage(), ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }
    }
    return new RpcScheduleDto(result);
  }

  public RpcScheduleDto updateRpcSchedule(
      UUID id, RpcScheduleDto rpcScheduleDto, SecurityUser securityUser)
      throws ThingsboardException {
    if (rpcScheduleDto.getId() == null) {
      throw new ThingsboardException(
          "Rpc Schedule ID is NULL", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }

    RpcScheduleEntity entity = rpcScheduleRepository.findById(id).get();

    entity.setName(rpcScheduleDto.getName());
    entity.setCron(rpcScheduleDto.getCron());
    entity.setActive(rpcScheduleDto.isActive());

    if (rpcScheduleDto.getGroupRpcId() != null) {
      entity.setRpcGroupId(rpcScheduleDto.getGroupRpcId());
      entity.setRpcSettingId(null);
    } else {
      RpcSettingEntity rpcSettingEntity = new RpcSettingEntity();
      if (rpcScheduleDto.getRpcSettingId() != null) {
        rpcSettingEntity = rpcSettingRepository.findById(entity.getRpcSettingId()).get();
      } else {
        rpcSettingEntity.setId(UUID.randomUUID());
      }
      DeviceEntity deviceEntity =
          dftDeviceRepository.findById(rpcScheduleDto.getDeviceId()).get();
      rpcSettingEntity.setDeviceId(deviceEntity.getId());
      rpcSettingEntity.setDeviceName(deviceEntity.getName());
      rpcSettingEntity.setValueControl(rpcScheduleDto.getValueControl());
      rpcSettingEntity.setDelayTime(0);
      rpcSettingEntity.setCallbackOption(rpcScheduleDto.isCallbackOption());
      rpcSettingEntity.setTimeCallback(rpcScheduleDto.getTimeCallback());
      rpcSettingEntity.setLoopOption(rpcScheduleDto.isLoopOption());
      rpcSettingEntity.setLoopCount(rpcScheduleDto.getLoopCount());
      rpcSettingEntity.setLoopTimeStep(rpcScheduleDto.getLoopTimeStep());
      rpcSettingEntity.setCreatedTime(new Date().getTime());
      RpcSettingEntity rpcSettingSaved = rpcSettingRepository.save(rpcSettingEntity);

      entity.setRpcSettingId(rpcSettingSaved.getId());
      entity.setRpcGroupId(null);
    }

    RpcScheduleEntity result = rpcScheduleRepository.save(entity);

    if (result.isActive()) {
      try {
        this.updateTask(result.getId().toString(), result.getCron(), createScheduleTask(result));
      } catch (Exception e) {
        throw new ThingsboardException(e.getMessage(), ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }
    } else {
      this.removeTask(result.getId().toString());
    }

    return new RpcScheduleDto(result);
  }

  public void deleteSchedule(UUID id) {
    rpcScheduleRepository.deleteById(id);
    this.removeTask(id.toString());
  }

  class RunnableTask implements Runnable {

    private final RpcScheduleEntity scheduleEntity;

    public RunnableTask(RpcScheduleEntity scheduleEntity) {
      this.scheduleEntity = scheduleEntity;
    }

    @SneakyThrows
    @Override
    public void run() {
      log.info("Excute runable schedule set schedule for schedule: " + scheduleEntity.getName());
      rpcCommandQueueService.saveRpcCommandFromSchedule(scheduleEntity);
    }
  }

  public RpcScheduleDto getRpcScheduleById(UUID tenantId, UUID id) {
    RpcScheduleEntity rpcScheduleEntity = rpcScheduleRepository.findByTenantIdAndId(tenantId, id);
    if (rpcScheduleEntity.getRpcSettingId() != null) {
      RpcSettingEntity rpcSettingEntity =
          rpcSettingRepository.findById(rpcScheduleEntity.getRpcSettingId()).get();
      return new RpcScheduleDto(rpcScheduleEntity, rpcSettingEntity);
    } else if (rpcScheduleEntity.getRpcGroupId() != null) {
      return new RpcScheduleDto(rpcScheduleEntity);
    } else {
      return null;
    }
  }

  public List<RpcScheduleDto> getListRpcScheduleByDamTomId(UUID tenantId, UUID damTomId) {
    List<RpcScheduleEntity> rpcScheduleEntities =
        rpcScheduleRepository.findAllByTenantIdAndDamTomIdOrderByCreatedTimeDesc(tenantId, damTomId);
    List<RpcScheduleDto> rpcScheduleDtos = new ArrayList<>();
    for (RpcScheduleEntity rpcScheduleEntity : rpcScheduleEntities) {
      if (rpcScheduleEntity.getRpcSettingId() != null) {
        RpcSettingEntity rpcSettingEntity =
            rpcSettingRepository.findById(rpcScheduleEntity.getRpcSettingId()).get();
        RpcScheduleDto rpcScheduleDto = new RpcScheduleDto(rpcScheduleEntity, rpcSettingEntity);
        rpcScheduleDtos.add(rpcScheduleDto);
      } else if (rpcScheduleEntity.getRpcGroupId() != null) {
        RpcScheduleDto rpcScheduleDto = new RpcScheduleDto(rpcScheduleEntity);
        rpcScheduleDtos.add(rpcScheduleDto);
      }
    }
    return rpcScheduleDtos;
  }

  public boolean existsByDamTomIdAndName(UUID damTomId, String name) {
    return rpcScheduleRepository.existsByDamTomIdAndName(damTomId, name);
  }

  public boolean existsByDamTomIdAndNameAndIdNot(UUID damTomId, String name, UUID id) {
    return rpcScheduleRepository.existsByDamTomIdAndNameAndIdNot(damTomId, name, id);
  }
}
