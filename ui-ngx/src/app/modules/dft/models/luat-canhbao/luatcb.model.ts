export interface AlarmDtoV2 {
  damtomId: string;
  deviceProfileAlarm: DeviceProfileAlarm;
}

export interface CheckAlarmNameExistDto {
  damtomId: string;
  alarmTypes: string[];
}

export interface DeviceProfileAlarm {
  id?: string;
  alarmType: string;
  createRules: any;
  propagate: boolean;
  dftAlarmRule: DftAlarmRule;
}

export interface DftAlarmRule {
  viaSms: boolean;
  viaEmail: boolean;
  viaNotification: boolean;
  rpcAlarm: boolean;
  active: boolean;
  createdTime?: number;
}

export interface CreateAlarmFormData {
  alarmType: string;
  nguongTren: string;
  nguongDuoi: string;
  hour: number;
  minute: number;
  second: number;
  viaNotification: boolean;
  viaSms: boolean;
  viaEmail: boolean;
  active: boolean;
  // is valid
  valid: boolean;
  key?: string;
}
