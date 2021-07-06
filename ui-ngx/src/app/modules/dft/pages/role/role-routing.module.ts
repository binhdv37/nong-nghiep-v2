import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Authority } from '@app/shared/public-api';
import {RoleComponent} from '@modules/dft/pages/role/role.component';

const routes: Routes = [
  {
    path: 'role',
    data: {
      breadcrumb: {
        auth: [Authority.TENANT_ADMIN, Authority.PAGES_ROLES],
        label: 'dft.role.manage',
        icon: 'ShieldCheckMenu'
      }
    },
    children: [
      {
        path: '',
        component: RoleComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_ROLES],
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RoleRoutingModule { }
