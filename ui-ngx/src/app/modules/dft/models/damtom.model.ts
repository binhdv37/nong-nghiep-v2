import {BaseData, TenantId} from '@app/shared/public-api';
import {DamTomId} from './id/damto-id';

export interface DamTom extends BaseData<DamTomId> {
  tenantId?: TenantId;
  name: string;
  address: string;
  note: string;
  searchText?: string;
  images: string;
  createBy?: string;
  createdTime?: number;
  active: boolean;
}

export interface ThietBi extends BaseData<DamTomId> {
  tenantId?: TenantId;
  name: string;
  note: string;
  createBy?: string;
  createdTime?: number;
  active: boolean;
  damtomId?: string;
}
