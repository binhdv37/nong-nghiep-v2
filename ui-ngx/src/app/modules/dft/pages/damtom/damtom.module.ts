import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '@shared/shared.module';
import {DamTomComponent} from './damtom.component';
import {DamTomRoutingModule} from './damtom-routing.module';
import {DamtomviewComponent} from './damtomview/damtomview.component';
import {DamtomcreateComponent} from './damtomcreate/damtomcreate.component';
import {MatTabsModule} from '@angular/material/tabs';
import {DamtomuserComponent} from './damtomuser/damtomuser.component';
import {ThietbicreateComponent} from './thietbicreate/thietbicreate.component';
import {ThietbieditComponent} from './thietbiedit/thietbiedit.component';
import {DamtomeditComponent} from './damtomedit/damtomedit.component';
import {DamTomGiamsatComponent} from './damtom-giamsat/damtom-giamsat.component';
import {HomeComponentsModule} from '@app/modules/home/components/public-api';
import {DamtomThietbiComponent} from './damtom-thietbi/damtom-thietbi.component';
import {DamTomDLCBComponent} from './damtom-dlcb/damtom-dlcb.component';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatMomentDatetimeModule} from '@mat-datetimepicker/moment';
import {MAT_DATETIME_FORMATS, MatDatetimepickerModule} from '@mat-datetimepicker/core';
import {ThietbiwiewComponent} from './thietbiwiew/thietbiwiew.component';
import {QlcameraComponent} from './qlcamera/qlcamera.component';
import {EditCameraComponent} from './qlcamera/edit-camera/edit-camera.component';
import {DamtomLuatComponent} from './damtom-luat/damtom-luat.component';
import {LuatCreateComponent} from './damtom-luat/luat-create/luat-create.component';
import {DamtomAlarmHistoryComponent} from './damtom-alarm-history/damtom-alarm-history.component';
import { LuatEditComponent } from './damtom-luat/luat-edit/luat-edit.component';
import { LuatViewComponent } from './damtom-luat/luat-view/luat-view.component';
import { DamtomRpcComponent } from './damtom-rpc/damtom-rpc.component';
import { DamtomGroupRpcComponent } from './damtom-group-rpc/damtom-group-rpc.component';
import { DamtomGroupRpcEditComponent } from './damtom-group-rpc/damtom-group-rpc-edit/damtom-group-rpc-edit.component';
import { SafePipe } from './qlcamera/pipe/safe.pipe';
import { RpcSettingDialogComponent } from './damtom-rpc/rpc-setting-dialog/rpc-setting-dialog.component';
import { ChangeLabelDialogComponent } from './damtom-thietbi/change-label-dialog/change-label-dialog.component';
import {RpcSettingComponent} from '@modules/dft/pages/damtom/damtom-group-rpc/damtom-group-rpc-edit/rpc-setting/rpc-setting.component';
import { DamtomRpcScheduleComponent } from './damtom-rpc-schedule/damtom-rpc-schedule.component';
import { RpcScheduleEditComponent } from './damtom-rpc-schedule/rpc-schedule-edit/rpc-schedule-edit.component';
import { CreateAlarmDialogComponent } from './damtom-luat/create-alarm-dialog/create-alarm-dialog.component';
import { EditAlarmDialogComponent } from './damtom-luat/edit-alarm-dialog/edit-alarm-dialog.component';
import { DetailAlarmDialogComponent } from './damtom-luat/detail-alarm-dialog/detail-alarm-dialog.component';
import { EditAlarmTabComponent } from './damtom-luat/edit-alarm-dialog/edit-alarm-tab/edit-alarm-tab.component';
import { DetailAlarmTabComponent } from './damtom-luat/detail-alarm-dialog/detail-alarm-tab/detail-alarm-tab.component';
import { CreateAlarmTabComponent } from './damtom-luat/create-alarm-dialog/create-alarm-tab/create-alarm-tab.component';
import { DamtomRpcHistoryComponent } from './damtom-rpc-history/damtom-rpc-history.component';
import { DamtomRpcAutoComponent } from './damtom-rpc-auto/damtom-rpc-auto.component';
import { CreateRpcAutoDialogComponent } from './damtom-rpc-auto/create-rpc-auto-dialog/create-rpc-auto-dialog.component';
import { CreateSukienDialogComponent } from './damtom-rpc-auto/create-rpc-auto-dialog/create-sukien-dialog/create-sukien-dialog.component';
import { CreateDieukhienDialogComponent } from './damtom-rpc-auto/create-rpc-auto-dialog/create-dieukhien-dialog/create-dieukhien-dialog.component';
import {EditRpcAutoDialogComponent} from '@modules/dft/pages/damtom/damtom-rpc-auto/edit-rpc-auto-dialog/edit-rpc-auto-dialog.component';
import { DamtomTableComponent } from './damtomview/damtom-table/damtom-table.component';

@NgModule({
  declarations: [
    DamTomComponent,
    DamtomviewComponent,
    DamtomcreateComponent,
    DamtomuserComponent,
    ThietbicreateComponent,
    ThietbieditComponent,
    DamtomeditComponent,
    DamTomGiamsatComponent,
    DamtomThietbiComponent,
    DamTomDLCBComponent,
    ThietbiwiewComponent,
    QlcameraComponent,
    EditCameraComponent,
    DamtomLuatComponent,
    LuatCreateComponent,
    DamtomAlarmHistoryComponent,
    LuatEditComponent,
    LuatViewComponent,
    DamtomRpcComponent,
    DamtomGroupRpcComponent,
    DamtomGroupRpcEditComponent,
    RpcSettingComponent,
    SafePipe,
    RpcSettingDialogComponent,
    ChangeLabelDialogComponent,
    DamtomRpcScheduleComponent,
    CreateAlarmDialogComponent,
    EditAlarmDialogComponent,
    DetailAlarmDialogComponent,
    EditAlarmTabComponent,
    DetailAlarmTabComponent,
    CreateAlarmTabComponent,
    RpcScheduleEditComponent,
    DamtomRpcHistoryComponent,
    DamtomRpcAutoComponent,
    CreateRpcAutoDialogComponent,
    CreateSukienDialogComponent,
    CreateDieukhienDialogComponent,
    EditRpcAutoDialogComponent,
    DamtomTableComponent,
  ],
  imports: [
    CommonModule,
    SharedModule,
    HomeComponentsModule,
    DamTomRoutingModule,
    MatTabsModule,
    MatDatepickerModule,
    MatMomentDatetimeModule,
    MatDatetimepickerModule
  ],
  providers: [
    {
      provide: MAT_DATETIME_FORMATS,
      useValue: {
        parse: {
          dateInput: 'YYYY-MM-DD',
          monthInput: 'MMMM',
          timeInput: 'HH:mm',
          datetimeInput: 'DD-MM-YYYY HH:mm'
        },
        display: {
          dateInput: 'DD-MM-YYYY',
          monthInput: 'MM',
          datetimeInput: 'DD-MM-YYYY HH:mm',
          timeInput: 'HH:mm',
          monthYearLabel: 'MM-YYYY',
          dateA11yLabel: 'L',
          monthYearA11yLabel: 'MM-YYYY',
          popupHeaderDateLabel: 'DD-MM'
        }
      }
    }
  ]
})
export class DamTomModule { }
