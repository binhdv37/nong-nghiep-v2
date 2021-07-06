import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '@shared/shared.module';
import {HomeComponentsModule} from '@home/components/home-components.module';
import {LapLichBaoCaoRoutingModule} from '@modules/dft/pages/lap-lich-bao-cao/lap-lich-bao-cao-routing.module';
import {LapLichBaoCaoComponent} from '@modules/dft/pages/lap-lich-bao-cao/lap-lich-bao-cao.component';
import { CreateReportSchedule } from './edit-lap-lich-bao-cao/create-report-schedule.component';
import { DetailReportSchedule } from './detail-lap-lich-bao-cao/detail-report-schedule.component';
import { UpdateReportSchedule } from './update-lap-lich-bao-cao/update-report-schedule.component';


@NgModule({
  declarations: [
    LapLichBaoCaoComponent,
    CreateReportSchedule,
    DetailReportSchedule,
    UpdateReportSchedule
    ],
  imports: [
    CommonModule,
    SharedModule,
    HomeComponentsModule,
    LapLichBaoCaoRoutingModule
  ]
})
export class LapLichBaoCaoModule {
}
