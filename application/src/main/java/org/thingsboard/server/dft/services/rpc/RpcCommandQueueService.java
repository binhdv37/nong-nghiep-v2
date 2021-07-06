package org.thingsboard.server.dft.services.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thingsboard.rule.engine.api.RpcError;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.rpc.RpcRequest;
import org.thingsboard.server.common.data.rpc.ToDeviceRpcRequestBody;
import org.thingsboard.server.common.msg.rpc.ToDeviceRpcRequest;
import org.thingsboard.server.dft.common.constants.command_rpc.CommandOrigin;
import org.thingsboard.server.dft.common.constants.command_rpc.CommandStatus;
import org.thingsboard.server.dft.common.service.TimestampService;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.CommandQueueDto;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.GroupRpcStatus;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.RpcCommandDto;
import org.thingsboard.server.dft.controllers.web.rpc.dtos.ThietBiDieuKhien;
import org.thingsboard.server.dft.entities.GroupRpcEntity;
import org.thingsboard.server.dft.entities.RpcCommandQueueEntity;
import org.thingsboard.server.dft.entities.RpcScheduleEntity;
import org.thingsboard.server.dft.entities.RpcSettingEntity;
import org.thingsboard.server.dft.repositories.GroupRpcRepository;
import org.thingsboard.server.dft.repositories.RpcCommandQueueRepository;
import org.thingsboard.server.dft.repositories.RpcSettingRepository;
import org.thingsboard.server.service.rpc.FromDeviceRpcResponse;
import org.thingsboard.server.service.rpc.TbCoreDeviceRpcService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RpcCommandQueueService {

  protected final ObjectMapper jsonMapper = new ObjectMapper();

  @Autowired private TbCoreDeviceRpcService deviceRpcService;

  @Value("${server.rest.server_side_rpc.min_timeout:5000}")
  private long minTimeout;

  @Value("${server.rest.server_side_rpc.default_timeout:10000}")
  private long defaultTimeout;

  private TaskExecutor taskExecutor;

  private final RpcCommandQueueRepository rpcCommandQueueRepository;
  private final ThietBiDieuKhienService thietBiDieuKhienService;
  private final GroupRpcRepository groupRpcRepository;
  private final RpcSettingRepository rpcSettingRepository;
  private final TimestampService timestampService;

  @Autowired
  public RpcCommandQueueService(
      TaskExecutor taskExecutor,
      RpcCommandQueueRepository rpcCommandQueueRepository,
      ThietBiDieuKhienService thietBiDieuKhienService,
      GroupRpcRepository groupRpcRepository,
      RpcSettingRepository rpcSettingRepository,
      TimestampService timestampService) {
    this.taskExecutor = taskExecutor;
    this.rpcCommandQueueRepository = rpcCommandQueueRepository;
    this.thietBiDieuKhienService = thietBiDieuKhienService;
    this.groupRpcRepository = groupRpcRepository;
    this.rpcSettingRepository = rpcSettingRepository;
    this.timestampService = timestampService;
  }

  @Transactional(rollbackFor = Exception.class)
  public void constructorAddThreadCommandRpc() {
    List<RpcCommandQueueEntity> rpcCommandQueues =
        rpcCommandQueueRepository.findAllByCommandStatusAndCommandTime(
            CommandStatus.NEW, new Date().getTime());
    for (RpcCommandQueueEntity rpcCommandQueue : rpcCommandQueues) {
      rpcCommandQueue.setCommandStatus(CommandStatus.EXECUTING);
      rpcCommandQueue.setUpdatedTime(new Date().getTime());
      rpcCommandQueueRepository.save(rpcCommandQueue);
      taskExecutor.execute(
          new RunnableTask(
              rpcCommandQueue.getId(),
              rpcCommandQueue.getDeviceId(),
              rpcCommandQueue.getTenantId(),
              rpcCommandQueue.getDeviceName(),
              rpcCommandQueue.getCommand()));
    }
  }

  @PostConstruct
  public void runableRpcCommand() {
    constructorAddThreadCommandRpc();
  }

  @Scheduled(fixedRate = 3000)
  public void scheduleScanCommandQueue() {
    constructorAddThreadCommandRpc();
  }

  class RunnableTask implements Runnable {

    private final UUID id;
    private final UUID deviceId;
    private final UUID tenantId;
    private final String deviceName;
    private final String command;

    public RunnableTask(UUID id, UUID deviceId, UUID tenantId, String deviceName, String command) {
      this.id = id;
      this.deviceId = deviceId;
      this.tenantId = tenantId;
      this.deviceName = deviceName;
      this.command = command;
    }

    @SneakyThrows
    @Override
    public void run() {
      log.info(
          "command id: "
              + id
              + " - device id: "
              + deviceId
              + " - deviceName: "
              + deviceName
              + " - command: "
              + command);
      handleDeviceRPCRequest(id, new TenantId(tenantId), true, new DeviceId(deviceId), command);
    }
  }

  public void handleDeviceRPCRequest(
      UUID rpcCommandQueueId,
      TenantId tenantId,
      boolean oneWay,
      DeviceId deviceId,
      String requestBody) {
    RpcCommandQueueEntity rpcCommandQueue =
        rpcCommandQueueRepository.findById(rpcCommandQueueId).get();
    try {
      JsonNode rpcRequestBody = jsonMapper.readTree(requestBody);
      org.thingsboard.server.common.data.rpc.RpcRequest cmd =
          new RpcRequest(
              rpcRequestBody.get("method").asText(),
              jsonMapper.writeValueAsString(rpcRequestBody.get("params")));

      if (rpcRequestBody.has("timeout")) {
        cmd.setTimeout(rpcRequestBody.get("timeout").asLong());
      }
      long timeout = cmd.getTimeout() != null ? cmd.getTimeout() : defaultTimeout;
      long expTime = System.currentTimeMillis() + Math.max(minTimeout, timeout);
      ToDeviceRpcRequestBody body =
          new ToDeviceRpcRequestBody(cmd.getMethodName(), cmd.getRequestData());
      ToDeviceRpcRequest rpcRequest =
          new ToDeviceRpcRequest(UUID.randomUUID(), tenantId, deviceId, oneWay, expTime, body);
      deviceRpcService.processRestApiRpcRequest(
          rpcRequest, fromDeviceRpcResponse -> reply(rpcCommandQueue, fromDeviceRpcResponse));
    } catch (IOException ioe) {
      rpcCommandQueue.setCommandStatus(CommandStatus.EXCEPTION);
      rpcCommandQueue.setException(ioe.getMessage());
      rpcCommandQueueRepository.save(rpcCommandQueue);
    }
  }

  private void reply(RpcCommandQueueEntity rpcCommandQueue, FromDeviceRpcResponse response) {
    Optional<RpcError> rpcError = response.getError();
    if (rpcError.isPresent()) {
      RpcError error = rpcError.get();
      switch (error) {
        case TIMEOUT:
          log.error("TIMEOUT");
          rpcCommandQueue.setCommandStatus(CommandStatus.TIMEOUT);
          break;
        case NO_ACTIVE_CONNECTION:
          log.error("NO_ACTIVE_CONNECTION");
          rpcCommandQueue.setCommandStatus(CommandStatus.NO_ACTIVE_CONNECTION);
          break;
        default:
          log.error("REQUEST_TIMEOUT");
          rpcCommandQueue.setCommandStatus(CommandStatus.REQUEST_TIMEOUT);
          break;
      }
    } else {
      Optional<String> responseData = response.getResponse();
      if (responseData.isPresent() && !StringUtils.isEmpty(responseData.get())) {
        String data = responseData.get();
        try {
          rpcCommandQueue.setCommandStatus(CommandStatus.SUCCESS);
          log.error("AUTO RPC " + jsonMapper.readTree(data) + " - status: OK");
        } catch (IOException e) {
          rpcCommandQueue.setCommandStatus(CommandStatus.EXCEPTION);
          rpcCommandQueue.setException(e.getMessage());
          log.debug("AUTO RPC - Failed to decode device response: {}", data, e);
        }
      } else {
        rpcCommandQueue.setCommandStatus(CommandStatus.SUCCESS);
        log.error("AUTO RPC - status: OK");
      }
    }
    rpcCommandQueueRepository.save(rpcCommandQueue);
  }

  public RpcCommandDto saveManualCommand(RpcCommandDto rpcCommandDto, UUID tenantId)
      throws JsonProcessingException {
    RpcSettingEntity rpcSettingEntity = new RpcSettingEntity();
    rpcSettingEntity.setDeviceId(rpcCommandDto.getDeviceId());
    rpcSettingEntity.setDeviceName(rpcCommandDto.getDeviceName());
    rpcSettingEntity.setCallbackOption(rpcCommandDto.isCallbackOption());
    rpcSettingEntity.setValueControl(rpcCommandDto.getValueControl());

    if (rpcSettingEntity.isCallbackOption()) {
      rpcSettingEntity.setTimeCallback(rpcCommandDto.getTimeCallback());
    }
    rpcSettingEntity.setLoopOption(rpcCommandDto.isLoopOption());

    if (rpcSettingEntity.isLoopOption()) {
      rpcSettingEntity.setLoopTimeStep(rpcCommandDto.getLoopTimeStep());
      rpcSettingEntity.setLoopCount(rpcCommandDto.getLoopCount());
    }

    saveListCommandToRpcQueue(
        tenantId,
        rpcCommandDto.getDamTomId(),
        Arrays.asList(rpcSettingEntity),
        CommandOrigin.MANUAL);

    return rpcCommandDto;
  }

  public void saveGroupRpcCommand(GroupRpcEntity groupRpcEntity) throws JsonProcessingException {
    log.info("Start set command in manual group rpc mode----------------");
    Collection<RpcSettingEntity> rpcSettingEntities = groupRpcEntity.getRpcSettingEntities();
    saveListCommandToRpcQueue(
        groupRpcEntity.getTenantId(),
        groupRpcEntity.getDamTomId(),
        rpcSettingEntities.stream().collect(Collectors.toList()),
        CommandOrigin.MANUAL_GROUP_RPC);
  }

  public void saveRpcCommandByAlarmRule(
      UUID tenantId, UUID damTomId, List<RpcSettingEntity> rpcSettingEntityList) {
    log.info("Start set command in manual alarm rpc mode----------------");
    try {
      saveListCommandToRpcQueue(tenantId, damTomId, rpcSettingEntityList, CommandOrigin.RULE_AUTO);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void saveRpcCommandFromSchedule(RpcScheduleEntity rpcScheduleEntity)
      throws JsonProcessingException {
    log.info("Start set command in manual schedule rpc mode----------------");
    if (rpcScheduleEntity.getRpcSettingId() != null) {

      RpcSettingEntity rpcSettingEntity =
          rpcSettingRepository.findById(rpcScheduleEntity.getRpcSettingId()).get();
      saveListCommandToRpcQueue(
          rpcScheduleEntity.getTenantId(),
          rpcScheduleEntity.getDamTomId(),
          Arrays.asList(rpcSettingEntity),
          CommandOrigin.SCHEDULE);

    } else if (rpcScheduleEntity.getRpcGroupId() != null) {
      GroupRpcEntity groupRpcEntity =
          groupRpcRepository.findByTenantIdAndId(
              rpcScheduleEntity.getTenantId(), rpcScheduleEntity.getRpcGroupId());
      Collection<RpcSettingEntity> rpcSettingEntities = groupRpcEntity.getRpcSettingEntities();
      saveListCommandToRpcQueue(
          rpcScheduleEntity.getTenantId(),
          rpcScheduleEntity.getDamTomId(),
          rpcSettingEntities.stream().collect(Collectors.toList()),
          CommandOrigin.SCHEDULE);
    }
  }

  private RpcCommandQueueEntity generateCommand(
      UUID tenantId,
      UUID damTomId,
      RpcSettingEntity rpcSettingEntity,
      int commandOrigin,
      String setValueMethod,
      double valueControl,
      long timeCommand)
      throws JsonProcessingException {
    RpcCommandQueueEntity rpcCommandQueueEntity = new RpcCommandQueueEntity();
    rpcCommandQueueEntity.setId(UUID.randomUUID());
    rpcCommandQueueEntity.setTenantId(tenantId);
    rpcCommandQueueEntity.setDamTomId(damTomId);
    rpcCommandQueueEntity.setDeviceId(rpcSettingEntity.getDeviceId());
    rpcCommandQueueEntity.setRpcSettingId(rpcSettingEntity.getId());
    rpcCommandQueueEntity.setGroupRpcId(rpcSettingEntity.getGroupRpcId());
    rpcCommandQueueEntity.setDeviceName(rpcSettingEntity.getDeviceName());
    rpcCommandQueueEntity.setOrigin(commandOrigin);

    org.thingsboard.server.dft.services.rpc.dtos.RpcRequest request =
        new org.thingsboard.server.dft.services.rpc.dtos.RpcRequest();
    request.setMethod(setValueMethod);
    Map<String, Integer> params = new HashMap<>();
    params.put("value", (int) valueControl);
    request.setParams(params);
    String command = jsonMapper.writeValueAsString(request);
    rpcCommandQueueEntity.setCommand(command);
    rpcCommandQueueEntity.setCommandStatus(CommandStatus.NEW);

    rpcCommandQueueEntity.setCommandTime(timeCommand);
    rpcCommandQueueEntity.setCreatedTime(new Date().getTime());
    return rpcCommandQueueEntity;
  }

  private void saveListCommandToRpcQueue(
      UUID tenantId, UUID damTomId, List<RpcSettingEntity> rpcSettingEntities, int commandOrigin)
      throws JsonProcessingException {
    long originCommandTime = new Date().getTime();
    if (!rpcSettingEntities.isEmpty()) {
      for (RpcSettingEntity rpcSettingEntity : rpcSettingEntities) {
        ThietBiDieuKhien thietBiDieuKhien =
            thietBiDieuKhienService.getThietBiDieuKhienById(
                new TenantId(tenantId), rpcSettingEntity.getDeviceId(), damTomId);
        long commandTimeWithDelay = originCommandTime + rpcSettingEntity.getDelayTime();
        save(
            generateCommand(
                tenantId,
                damTomId,
                rpcSettingEntity,
                commandOrigin,
                thietBiDieuKhien.getSetValueMethod(),
                rpcSettingEntity.getValueControl(),
                commandTimeWithDelay));

        if (rpcSettingEntity.isCallbackOption()) {
          log.info("Set command callback----------------");
          long commandTimeWithDelayAndCallback =
              commandTimeWithDelay + rpcSettingEntity.getTimeCallback();
          double valueCallback = (int) rpcSettingEntity.getValueControl() == 1 ? 0 : 1;
          save(
              generateCommand(
                  tenantId,
                  damTomId,
                  rpcSettingEntity,
                  commandOrigin,
                  thietBiDieuKhien.getSetValueMethod(),
                  valueCallback,
                  commandTimeWithDelayAndCallback));
          if (rpcSettingEntity.isLoopOption()) {
            log.info("Set command loop----------------");
            for (int i = 1; i <= rpcSettingEntity.getLoopCount(); i++) {
              log.info("Loop count: " + i);
              long commandTimeForLoop =
                  commandTimeWithDelayAndCallback
                      + rpcSettingEntity.getTimeCallback() * (i - 1)
                      + rpcSettingEntity.getLoopTimeStep() * i;
              save(
                  generateCommand(
                      tenantId,
                      damTomId,
                      rpcSettingEntity,
                      commandOrigin,
                      thietBiDieuKhien.getSetValueMethod(),
                      rpcSettingEntity.getValueControl(),
                      commandTimeForLoop));

              long commandTimeForLoopCallback =
                  commandTimeForLoop + rpcSettingEntity.getTimeCallback();
              save(
                  generateCommand(
                      tenantId,
                      damTomId,
                      rpcSettingEntity,
                      commandOrigin,
                      thietBiDieuKhien.getSetValueMethod(),
                      valueCallback,
                      commandTimeForLoopCallback));
            }
          }
        }
      }
    }
  }

  private RpcCommandQueueEntity save(RpcCommandQueueEntity rpcCommandQueueEntity) {
    log.info(
        "Save command --- Device name: "
            + rpcCommandQueueEntity.getDeviceName()
            + " - command: "
            + rpcCommandQueueEntity.getCommand()
            + " - origin: "
            + rpcCommandQueueEntity.getOrigin()
            + " - timeCommand: "
            + new Date(rpcCommandQueueEntity.getCommandTime()));
    rpcCommandQueueEntity.setViewed(false);
    return rpcCommandQueueRepository.save(rpcCommandQueueEntity);
  }

  public Page<RpcCommandQueueEntity> getPageRpcCommandByStatus(
      Pageable pageable,
      UUID tenantId,
      UUID damTomId,
      UUID deviceId,
      Long startTime,
      Long endTime,
      String statusCommand) {
    Page<RpcCommandQueueEntity> rpcCommandQueueEntityPages;
    if (deviceId != null) {
      rpcCommandQueueEntityPages = rpcCommandQueueRepository.findAllByStatusAndDeviceId(
              pageable, tenantId, damTomId, startTime,
              endTime, statusCommand, deviceId);
    } else {
      rpcCommandQueueEntityPages = rpcCommandQueueRepository.findAllByStatus(
              pageable, tenantId, damTomId, startTime,
              endTime, statusCommand);
    }

    for (RpcCommandQueueEntity rpcCommandQueueEntity : rpcCommandQueueEntityPages.getContent()) {
      rpcCommandQueueEntity.setViewed(true);
      rpcCommandQueueRepository.save(rpcCommandQueueEntity);
    }
    return rpcCommandQueueEntityPages;
  }

  public Map<Long, List<CommandQueueDto>> getRpcCommandQueueGroupByDay(
      List<CommandQueueDto> commandQueueDtos) {
    Map<Long, List<CommandQueueDto>> mapRpcCommandDto = new HashMap<>();
    for (CommandQueueDto rpcCommanQueuedDto : commandQueueDtos) {
      Long dayOfRpcCommand =
          timestampService.getFirstDayOfQuarter(new Date(rpcCommanQueuedDto.getCommandTime()));
      if (mapRpcCommandDto.containsKey(dayOfRpcCommand)) {
        mapRpcCommandDto.get(dayOfRpcCommand).add(rpcCommanQueuedDto);
      } else {
        List<CommandQueueDto> commandQueueDtoList = new ArrayList<>();
        commandQueueDtoList.add(rpcCommanQueuedDto);
        mapRpcCommandDto.put(dayOfRpcCommand, commandQueueDtoList);
      }
    }
    return mapRpcCommandDto;
  }

  public int getCommandStatusNotViewed(UUID tenantId, UUID damTomId) {
    return rpcCommandQueueRepository.countByTenantIdAndDamTomIdAndCommandStatusAndViewed(
        tenantId, damTomId, CommandStatus.SUCCESS, false);
  }

  @Transactional(rollbackFor = Exception.class)
  public void cancelAllRpcCommandById(UUID tenantId, UUID groupRpcId) {
    List<RpcCommandQueueEntity> rpcCommandQueueEntities =
        rpcCommandQueueRepository.findAllByTenantIdAndGroupRpcIdAndCommandStatus(
            tenantId, groupRpcId, CommandStatus.NEW);
    for (RpcCommandQueueEntity rpcCommandQueueEntity : rpcCommandQueueEntities) {
      rpcCommandQueueEntity.setUpdatedTime(new Date().getTime());
      rpcCommandQueueEntity.setCommandStatus(CommandStatus.CANCEL);
      save(rpcCommandQueueEntity);
    }
  }

  public GroupRpcStatus checkGroupRpcInProcessing(UUID tenantId, UUID groupRpcId) {
    GroupRpcStatus groupRpcStatus = new GroupRpcStatus();
    groupRpcStatus.setGroupRpcId(groupRpcId);
    RpcCommandQueueEntity commandQueueEntity =
        rpcCommandQueueRepository.findLastestCommandByGroupRpcAndStatus(
            tenantId, groupRpcId, CommandStatus.NEW);
    if (commandQueueEntity != null) {
      groupRpcStatus.setLoading(true);
      groupRpcStatus.setTimeEndLoading(commandQueueEntity.getCommandTime());
    }
    return groupRpcStatus;
  }

  public boolean checkDeviceInProcessing(UUID tenantId, UUID deviceId, UUID rpcSettingId) {
    List<String> rpcCommandStatusList = new ArrayList<>();
    rpcCommandStatusList.add(CommandStatus.NEW);
    rpcCommandStatusList.add(CommandStatus.EXECUTING);
    return rpcCommandQueueRepository.existsByTenantIdAndDeviceIdAndRpcSettingIdAndCommandStatusIn(
        tenantId, deviceId, rpcSettingId, rpcCommandStatusList);
  }
}
