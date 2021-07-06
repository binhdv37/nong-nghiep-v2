import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {Authority} from '@shared/models/authority.enum';
import {BcDlgiamsatComponent} from '@modules/dft/pages/baocao/bc-dlgiamsat/bc-dlgiamsat.component';
import {BcCanhbaoComponent} from '@modules/dft/pages/baocao/bc-canhbao/bc-canhbao.component';
import {BcDashboardComponent} from '@modules/dft/pages/baocao/bc-dashboard/bc-dashboard.component';
import {BcTonghopComponent} from '@modules/dft/pages/baocao/bc-tonghop/bc-tonghop.component';
import {BcThongbaoComponent} from '@modules/dft/pages/baocao/bc-thongbao/bc-thongbao.component';
import {HomeLinksComponent} from '@home/pages/home-links/home-links.component';
import {BcKetnoiComponent} from '@modules/dft/pages/baocao/bc-ketnoi/bc-ketnoi.component';

const routes: Routes = [
  {
    path: 'bao-cao',
    data: {
      auth: [Authority.TENANT_ADMIN, Authority.PAGES_BAOCAO],
      breadcrumb: {
        label: 'Báo cáo',
        icon: 'ClipboardTextMenu'
      }
    },
    children: [
      {
        path: '',
        component: HomeLinksComponent,
        pathMatch: 'full'
      },
      {
        path: 'bc-tonghop',
        component: BcTonghopComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_BC_TONGHOP],
          title: 'Báo cáo tổng hợp',
          breadcrumb: {
            label: 'Báo cáo tổng hợp',
            icon: 'bcTongHopMenu'
          }
        }
      },
      {
        path: 'bc-dlgiamsat',
        component: BcDlgiamsatComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_BC_DLGIAMSAT],
          title: 'Báo cáo dữ liệu giám sát',
          breadcrumb: {
            label: 'Báo cáo dữ liệu giám sát',
            icon: 'bcDuLieuGiamSatMenu'
          }
        }
      },
      {
        path: 'bc-canhbao',
        component: BcCanhbaoComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_BC_CANHBAO],
          title: 'Báo cáo cảnh báo',
          breadcrumb: {
            label: 'Báo cáo cảnh báo',
            icon: 'bcCanhBaoMenu'
          }
        }
      },
      {
        path: 'bc-ketnoi-cambien',
        component: BcKetnoiComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_BC_KETNOI_CAMBIEN],
          title: 'dft.bao-cao.bc-ketnoi-cb',
          breadcrumb: {
            label: 'dft.bao-cao.bc-ketnoi-cb',
            icon: 'bcCamBienMenu'
          }
        }
      },
      {
        path: 'bc-gui-ttcb',
        component: BcThongbaoComponent,
        data: {
          auth: [Authority.TENANT_ADMIN, Authority.PAGES_BC_GUI_TTCB],
          title: 'Báo cáo thông báo',
          breadcrumb: {
            label: 'Báo cáo thông báo',
            icon: 'bcThongBaoMenu'
          }
        }
      },
    ]
  },
  {
    path: 'damtom-dashboard',
    component: BcDashboardComponent,
    data: {
      auth: [Authority.TENANT_ADMIN, Authority.PAGES_DAMTOM],
      title: 'dft.bao-cao.bc-dashboard',
      breadcrumb: {
        label: 'dft.bao-cao.bc-dashboard',
        icon: 'PresentationChart'
      }
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BaocaoRoutingModule {
}
