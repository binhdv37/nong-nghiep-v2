import { Authority } from './../../../../shared/models/authority.enum';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {DamTomComponent} from '@modules/dft/pages/damtom/damtom.component';
import {DamtomviewComponent} from '@modules/dft/pages/damtom/damtomview/damtomview.component';

const routes: Routes = [
  {
    path: 'dam-tom',
    data: {
      breadcrumb: {
        auth: [Authority.TENANT_ADMIN, Authority.PAGES_DAMTOM],
        label: 'dft.damtom.manage',
        icon: 'MapPinMenu'
      }
    },
    children: [
      {
        path: '',
        component: DamTomComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_DAMTOM],
        }
      },
      {
        path: ':id',
        component: DamtomviewComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_DAMTOM],
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
export class DamTomRoutingModule { }
