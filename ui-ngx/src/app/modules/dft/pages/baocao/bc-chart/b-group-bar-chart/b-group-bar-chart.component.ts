import {Component, Input} from '@angular/core';
import {BcMultiData} from '@modules/dft/models/bao-cao/bao-cao.model';
import {ColorHelper} from '@swimlane/ngx-charts';

@Component({
  selector: 'tb-b-group-bar-chart',
  templateUrl: './b-group-bar-chart.component.html',
  styleUrls: ['./b-group-bar-chart.component.scss']
})
export class BGroupBarChartComponent {

  @Input() data: BcMultiData[];

  activeEntries: any[] = [];
  chartNames: string[] = ['Số lần gửi thông tin cảnh báo', 'Số lần thành công', 'Số lần thất bại'];
  colors: ColorHelper;

  // options
  showXAxis = true;
  showYAxis = true;
  showLegend = false;
  legendPosition = 'below';
  legendTitle = '';

  colorScheme = {
    domain: ['#4472C4', '#ED7D31', '#A5A5A5']
  };

  constructor(

  ) {
    this.colors = new ColorHelper(this.colorScheme, 'ordinal', this.chartNames, this.colorScheme);
  }

  legendLabelActivate(item: any): void {
    this.activeEntries = [item];
  }

  legendLabelDeactivate(item: any): void {
    this.activeEntries = [];
  }

  onSelect(data): void {
  }

  onActivate(data): void {
  }

  onDeactivate(data): void {
  }

}
