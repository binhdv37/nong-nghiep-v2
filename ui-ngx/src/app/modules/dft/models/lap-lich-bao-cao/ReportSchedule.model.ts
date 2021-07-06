import {User} from '@shared/models/user.model';

export interface ReportScheduleCreateOrUpdate {
  active: boolean;
  createdBy?: string;
  createdTime?: number;
  cron: string;
  damTomId: string;
  note?: string;
  reportName: string;
  scheduleName: string;
  tenantId?: string;
  users: string[];
}

export interface ReportSchedule {
  active: boolean;
  createdBy?: string;
  createdTime?: number;
  cron: string;
  damTomId: string;
  id?: string;
  note?: string;
  reportName: string;
  scheduleName: string;
  tenantId?: string;
  users: ReportScheduleUser[];
}

interface ReportScheduleUser{
  scheduleId: string;
  user: User;
  createdBy?: string;
  createdTime?: number;
}

export interface DamTomSchedule {
  id: { id: string, type?: string };
  tenantId: string;
  name: string;
  address?: string;
  note?: string;
  searchText?: string;
  images?: string;
  createdBy: string;
  createdTime: number;
  active: boolean;
  staffs: any[];
}

export interface DamTom {
  id: string;
  tenantId: string;
  name: string;
  address?: string;
  note?: string;
  searchText?: string;
  images?: string;
  createdBy: string;
  createdTime: number;
  active: boolean;
  staffs: any[];
}

export const ReportNameMap = [
  {key: 'WARNING_REPORT', value: 'Báo cáo cảnh báo'},
  {key: 'MONITORING_DATA_REPORT', value: 'Báo cáo dữ liệu giám sát'},
  {key: 'SENSOR_CONNECTION_REPORT', value: 'Báo cáo kết nối cảm biến'},
  {key: 'NOTIFICATION_DATA_REPORT', value: 'Báo cáo dữ liệu thông báo'},
  {key: 'SYNTHESIS_REPORT', value: 'Báo cáo tổng hợp'}
];
