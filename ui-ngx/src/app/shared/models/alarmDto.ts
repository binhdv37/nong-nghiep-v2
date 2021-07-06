export interface AlarmDto {
  alarmId: string;
  profile_Data: string;
  damtomId: string;
  name: string;
  note: string;
  viaSms: boolean;
  viaEmail: boolean;
  viaNotification: boolean;
  thucHienDieuKhien: string;
}
export interface AlarmDeleteDto {
  alarmId: string;
  profile_Data: string;
}

export class AppOperatorAndOr {
  static AND = 1;
  static OR = 2;
}

export class AppKeyFilterPredicateOperation {
  static EQUAL = 'EQUAL';
  static NOT_EQUAL = 'NOT_EQUAL';
  static GREATER = 'GREATER';
  static LESS = 'LESS';
  static GREATER_OR_EQUAL = 'GREATER_OR_EQUAL';
  static LESS_OR_EQUAL = 'LESS_OR_EQUAL';
  static STARTS_WITH = 'STARTS_WITH';
  static ENDS_WITH = 'ENDS_WITH';
  static CONTAINS = 'CONTAINS';
  static NOT_CONTAINS = 'NOT_CONTAINS';
}

export class Telemetry {
  static TEMPERATURE = 'Temperature';
  static DO = 'DO';
  static ORP = 'ORP';
  static PH = 'pH';
  static SALINITY = 'Salinity';
}

export interface DataAlarm {
  alarm: [IAlarm];
  configuration: { type: 'DEFAULT' };
  provisionConfiguration: { type: 'DISABLED', provisionDeviceSecret: null };
  transportConfiguration: { type: 'DEFAULT' };
}

export interface IAlarm {
  id: string;
  alarmType: string;
  createRules: {
    CRITICAL: IServerity;
  };
  clearRule: null;
  propagate: false;
}

export interface IServerity {
  schedule: null;
  condition: ICondition;
  alarmDetails: null;
}

export interface ICondition {
  spec: ISpec;
  condition: [];
}

export interface ISpec {
  type: 'SIMPLE';
}

export interface IKeyFilter {
  key: IEntityKey;
  predicate: IKeyFilterPredicate;
  valueType: 'NUMERIC';
}

export interface IEntityKey {
  key: string; // input text, tên của telemetry
  type: 'TIME_SERIES';
}

export interface IKeyFilterPredicate {
  type: 'NUMERIC';
  value?: IValue;
  predicates?: [Ipredicates];
  operation: string;
}

export interface IValue {
  userValue: null;
  defaultValue: number;
  dynamicValue: null;
}

export interface Ipredicates{
  type: string; // lay theo kieu cua KeyFilter.valueType
  value: IValue;
  operation: string;
  ignoreCase?: boolean;
}


