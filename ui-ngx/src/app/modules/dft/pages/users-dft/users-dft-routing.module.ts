import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Authority } from '@app/shared/public-api';
import {UsersDftComponent} from '@modules/dft/pages/users-dft/users-dft.component';

const routes: Routes = [
  {
    path: 'account',
    data: {
      breadcrumb: {
        auth: [Authority.TENANT_ADMIN, Authority.PAGES_USERS],
        label: 'dft.users.manage',
        icon: 'UsersMenu'
      }
    },
    children: [
      {
        path: '',
        component: UsersDftComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_USERS],
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: []
})
export class UsersDftRoutingModule { }
