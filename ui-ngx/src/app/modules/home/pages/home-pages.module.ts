///
/// Copyright © 2016-2021 The Thingsboard Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import {NgModule} from '@angular/core';

import {AdminModule} from './admin/admin.module';
import {HomeLinksModule} from './home-links/home-links.module';
import {ProfileModule} from './profile/profile.module';
import {TenantModule} from '@modules/home/pages/tenant/tenant.module';
import {CustomerModule} from '@modules/home/pages/customer/customer.module';
import {AuditLogModule} from '@modules/home/pages/audit-log/audit-log.module';
import {UserModule} from '@modules/home/pages/user/user.module';
import {DeviceModule} from '@modules/home/pages/device/device.module';
import {AssetModule} from '@modules/home/pages/asset/asset.module';
import {EntityViewModule} from '@modules/home/pages/entity-view/entity-view.module';
import {RuleChainModule} from '@modules/home/pages/rulechain/rulechain.module';
import {WidgetLibraryModule} from '@modules/home/pages/widget/widget-library.module';
import {DashboardModule} from '@modules/home/pages/dashboard/dashboard.module';
import {TenantProfileModule} from './tenant-profile/tenant-profile.module';
import {MODULES_MAP} from '@shared/public-api';
import {modulesMap} from '../../common/modules-map';
import {DeviceProfileModule} from './device-profile/device-profile.module';
import {ApiUsageModule} from '@home/pages/api-usage/api-usage.module';
import {DamTomModule} from '@app/modules/dft/pages/damtom/damtom.module';
import {UsersDftModule} from '@modules/dft/pages/users-dft/users-dft.module';
import {KhachHangModule} from '@app/modules/dft/pages/admin/khachhang/khachhang.module';
import {RoleModule} from '@modules/dft/pages/role/role.module';
import {AccessHistoryModule} from '@app/modules/dft/pages/access-history/access-history.module';
import {BaocaoModule} from '@modules/dft/pages/baocao/baocao.module';
import {LapLichBaoCaoModule} from '@modules/dft/pages/lap-lich-bao-cao/lap-lich-bao-cao.module';

@NgModule({
  exports: [
    AdminModule,
    HomeLinksModule,
    ProfileModule,
    TenantProfileModule,
    TenantModule,
    DeviceProfileModule,
    DeviceModule,
    AssetModule,
    EntityViewModule,
    CustomerModule,
    RuleChainModule,
    WidgetLibraryModule,
    DashboardModule,
    AuditLogModule,
    ApiUsageModule,
    UserModule,
    // dft custom
    DamTomModule,
    KhachHangModule,
    UsersDftModule,
    RoleModule,
    AccessHistoryModule,
    BaocaoModule,
    LapLichBaoCaoModule
  ],
  providers: [
    {
      provide: MODULES_MAP,
      useValue: modulesMap
    }
  ]
})
export class HomePagesModule {
}
