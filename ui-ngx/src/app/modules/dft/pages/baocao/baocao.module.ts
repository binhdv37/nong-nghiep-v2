import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '@shared/shared.module';

import {BaocaoRoutingModule} from './baocao-routing.module';
import {BcDlgiamsatComponent} from './bc-dlgiamsat/bc-dlgiamsat.component';
import {BcCanhbaoComponent} from './bc-canhbao/bc-canhbao.component';
import {BcDashboardComponent} from './bc-dashboard/bc-dashboard.component';
import {BcTonghopComponent} from './bc-tonghop/bc-tonghop.component';
import {HomeComponentsModule} from '@home/components/home-components.module';
import {BPieChartComponent} from '@modules/dft/pages/baocao/bc-chart/b-pie-chart/b-pie-chart.component';
import {BLineChartComponent} from '@modules/dft/pages/baocao/bc-chart/b-line-chart/b-line-chart.component';
import { NoLegendBarChartComponent } from './bc-chart/no-legend-bar-chart/no-legend-bar-chart.component';
import { NoLegendLineChartComponent } from './bc-chart/no-legend-line-chart/no-legend-line-chart.component';
import { BGroupBarChartComponent } from './bc-chart/b-group-bar-chart/b-group-bar-chart.component';
import { BcKetnoiComponent } from './bc-ketnoi/bc-ketnoi.component';
import { LegendBarChartComponent } from './bc-chart/legend-bar-chart/legend-bar-chart.component';
import { BcKetnoiTableComponent } from './bc-ketnoi/bc-ketnoi-table/bc-ketnoi-table.component';
import { BcThongbaoComponent } from './bc-thongbao/bc-thongbao.component';


@NgModule({
  declarations: [
    BcDlgiamsatComponent,
    BLineChartComponent,
    BcCanhbaoComponent,
    BcDashboardComponent,
    BPieChartComponent,
    BcTonghopComponent,
    NoLegendBarChartComponent,
    NoLegendLineChartComponent,
    BGroupBarChartComponent,
    BcThongbaoComponent,
    BGroupBarChartComponent,
    BcKetnoiComponent,
    LegendBarChartComponent,
    BcKetnoiTableComponent],
  imports: [
    CommonModule,
    BaocaoRoutingModule,
    SharedModule,
    HomeComponentsModule
  ]
})
export class BaocaoModule {
}
