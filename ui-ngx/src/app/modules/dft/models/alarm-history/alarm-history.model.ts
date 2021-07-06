export interface AlarmHistory {
  thoiGian: string;
  id: string;
  alarmId: string;
  tenCanhBao: string;
  gatewayId: string;
  gatewayName: string;
  clear: boolean;
  span: number;
  display: boolean;
  data?: any;
  alarmGateway: boolean;
  alarmKeys: string[];
}
