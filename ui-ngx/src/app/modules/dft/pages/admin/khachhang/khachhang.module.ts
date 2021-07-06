import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '@shared/shared.module';
import { KhachHangRoutingModule } from './khachhang-routing.module';
import { NgxDropzoneModule } from 'ngx-dropzone';
import { KhachHangComponent } from './khachhang.component';
import { EditKhachHangComponent } from './edit-khachhang/edit-khachhang.component';
import {MatListModule} from '@angular/material/list';
import {CameraSettingComponent} from '@modules/dft/pages/admin/camera-setting/camera-setting.component';
import { NotificationSettingComponent } from '../notification-setting/notification-setting.component';


@NgModule({
  declarations: [
    KhachHangComponent,
    EditKhachHangComponent,
    CameraSettingComponent,
    NotificationSettingComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    KhachHangRoutingModule,
    NgxDropzoneModule,
    MatListModule,
  ]
})
export class KhachHangModule { }
