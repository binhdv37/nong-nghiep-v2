export enum DialogAction {
  CREATE,
  EDIT
}
export interface SuKien {
  duLieuCamBien: string; // key
  kieuDuLieu: KieuDuLieuSuKien;
  camBien: string; // id cua thiet bi cam bien
  toanTu: ToanTu;
  nguongGiaTri: string;
  gatewayId: string;
}

export enum KieuDuLieuSuKien {
  BAT_KY = 'BAT_KY',
  CU_THE = 'CU_THE',
}

export enum ToanTu{
  GREATER_OR_EQUAL = 'GREATER_OR_EQUAL',
  LESS_OR_EQUAL = 'LESS_OR_EQUAL'
}

export interface DieuKhien {
  id?: string;
  deviceId?: string;
  deviceName?: string;
  label?: string;
  valueControl?: number; // bat(1) or tat(0)
  delayTime?: number;
  callbackOption?: boolean; // Thực hiện trong
  timeCallback?: number; // Th gian thực hiện trong
  loopOption?: boolean;
  loopCount?: number;
  loopTimeStep?: number;
  commandId?: number;
  groupRpcId?: string;
  typeRpc?: TypeRpc; // custom
  groupRpcName?: string; // custom
}

export enum TypeRpc{
  GROUP_RPC = 'GROUP-RPC',
  RPC = 'RPC'
}

export interface DamtomDto {
  id?: string;
  tenantId?: string;
  asset?: any;
  deviceProfile?: any;
  cameras?: any;
  name?: string;
  address?: string;
  note?: string;
  searchText?: string;
  images?: any;
  staffs?: any;
  gateways: GatewayDto[];
  createdBy?: string;
  createdTime?: number;
  active?: boolean;
}

export interface GatewayDto {
  id?: string;
  tenantId?: string;
  damtomId?: string;
  device?: DeviceDto;
  createdBy?: string;
  createdTime?: number;
  active?: boolean;
}

export interface DeviceDto {
  id?: string;
  createdTime?: number;
  tenantId?: string;
  customerId?: string;
  type?: string;
  name?: string;
  label?: string;
  searchText?: string;
  additionalInfo?: any;
  deviceProfileId?: string;
  deviceData?: any;
  searchTextSource?: string;
  uuid?: string;
  telemetry?: string[]; // custom
  gatewayId?: string; // custom
}

export interface GetGatewayResponse {
  credentialsId?: string;
  listDevices?: DeviceDto[];
  gateway?: GatewayDto;
}

export interface RpcAlarm {
  id?: string;
  alarmType?: string;
  createRules: {
    MAJOR: {
      condition: {
        condition: Condition[];
        spec: {
          type: string;
          unit?: string;
          value?: number;
        };
      };
      schedule?: {
        type: string;
        timezone: string;
        daysOfWeek: number[];
        startsOn: number;
        endsOn: number;
      }
    }
  };
  propagate: boolean;
  dftAlarmRule: {
    rpcAlarm: boolean;
    active: boolean;
    viaEmail?: boolean;
    viaSms?: boolean;
    viaNotification?: boolean;
    gatewayIds: string[];
    groupRpcIds: string[];
    rpcSettingIds: string[];
    createdTime?: number;
  };
}

export interface Condition {
  key: {
    type: string;
    key: string;
  };
  valueType: string;
  predicate: {
    type: string;
    operation: string;
    predicates?: any[];
    value?: {
      defaultValue: number;
    };
  };
}
