import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Authority } from '@app/shared/public-api';
import {AccessHistoryComponent} from '@modules/dft/pages/access-history/access-history/access-history.component';

const routes: Routes = [
  {
    path: 'logs',
    data: {
      breadcrumb: {
        label: 'Lịch sử truy cập',
        icon: 'ClockCounterClockwiseMenu',
        auth: [Authority.TENANT_ADMIN, Authority.PAGES_USERS_ACCESS_HISTORY],
      }
    },
    children: [
      {
        path: '',
        component: AccessHistoryComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_USERS_ACCESS_HISTORY],
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccessHistoryRoutingModule { }
