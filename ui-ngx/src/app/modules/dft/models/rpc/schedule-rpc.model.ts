export class RpcSchedule {
  id: string;
  damTomId: string;
  name: string;
  rpcSettingId: string;
  deviceId: string;
  groupRpcId: string;
  valueControl: number;
  cron: string;
  active: boolean;
  callbackOption: boolean;
  timeCallback: number;
  loopOption: boolean;
  loopCount: number;
  loopTimeStep: number;
}
