import {BaseData, CustomerId, TenantId, UserId} from '@app/shared/public-api';
import {UsersDftId} from '@modules/dft/models/usersDft/usersDftId';
import {Role} from '@modules/dft/models/role.model';

export interface UsersDft extends BaseData<UsersDftId>{
  tenantId?: TenantId;
  userId?: UserId;
  customerId?: CustomerId;
  firstName: string;
  email: string;
  lastName: string;
  password: string;
  additionalInfo: AdditionalInfo;
  roleEntity?: Role[];
  roleId?: string[];
  enabled: boolean;
  createdTime?: number;
  responseCode?: number;
  responseMessage?: string;
}

export interface AdditionalInfo {
  description: string;
  defaultDashboardId?: string;
  defaultDashboardFullscreen?: boolean;
  lastLoginTs?: number;
  failedLoginAttempts?: number;
  userPasswordHistory?: any;
  userCredentialsEnabled?: boolean;
  lang?: string;
}
