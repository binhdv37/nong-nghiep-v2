import { KhachHangComponent } from './khachhang.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Authority } from '@app/shared/public-api';
import {CameraSettingComponent} from '@modules/dft/pages/admin/camera-setting/camera-setting.component';
import { NotificationSettingComponent } from '../notification-setting/notification-setting.component';

const routes: Routes = [
  {
    path: 'sys-admin/khach-hang',
    data: {
      breadcrumb: {
        auth: [Authority.SYS_ADMIN],
        label: 'dft.admin.khachhang.manage',
        icon: 'UsersMenu'
      }
    },
    children: [
      {
        path: '',
        component: KhachHangComponent,
        data: {
          auth: [Authority.SYS_ADMIN],
        }
      }
    ]
  },
  {
    path: 'settings/camera',
    data: {
      breadcrumb: {
        auth: [Authority.SYS_ADMIN],
        label: 'dft.admin.camera.setting',
        icon: 'videocam'
      }
    },
    children: [
      {
        path: '',
        component: CameraSettingComponent,
        data: {
          auth: [Authority.SYS_ADMIN],
        }
      }
    ]
  },
  {
    path: 'settings/notification',
    data: {
      breadcrumb: {
        auth: [Authority.SYS_ADMIN],
        label: 'dft.admin.notification.setting',
        icon: 'notification'
      }
    },
    children: [
      {
        path: '',
        component: NotificationSettingComponent,
        data: {
          auth: [Authority.SYS_ADMIN],
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [
  ]
})
export class KhachHangRoutingModule { }
