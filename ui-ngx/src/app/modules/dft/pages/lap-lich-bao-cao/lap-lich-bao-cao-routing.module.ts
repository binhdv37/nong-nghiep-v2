import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {Authority} from '@app/shared/public-api';
import {LapLichBaoCaoComponent} from '@modules/dft/pages/lap-lich-bao-cao/lap-lich-bao-cao.component';

const routes: Routes = [
  {
    path: 'report-schedule',
    data: {
      breadcrumb: {
        auth: [Authority.TENANT_ADMIN, Authority.PAGES_DATLICH_XUATBC],
        label: 'dft.bao-cao.lap-lich-bc',
        icon: 'CalendarBlankMenu'
      }
    },
    children: [
      {
        path: '',
        component: LapLichBaoCaoComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_DATLICH_XUATBC],
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
export class LapLichBaoCaoRoutingModule {
}
