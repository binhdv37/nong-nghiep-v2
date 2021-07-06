import {Component, Input} from '@angular/core';
import {BcSingleData} from '@modules/dft/models/bao-cao/bao-cao.model';

@Component({
  selector: 'tb-no-legend-bar-chart',
  templateUrl: './no-legend-bar-chart.component.html',
  styleUrls: ['./no-legend-bar-chart.component.scss']
})
export class NoLegendBarChartComponent {

  @Input() data: BcSingleData[];
  @Input() chartTitle: string;

  // view: any[] = [700, 400];
  // single: any[];

  // options
  showXAxis = true;
  showYAxis = true;
  gradient = false;
  showLegend = false;
  showXAxisLabel = false;
  xAxisLabel = 'Ngày';
  showYAxisLabel = false;
  yAxisLabel = 'Cảnh báo';

  colorScheme = {
    // domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA']
    domain: ['#4270C1']
  };

  constructor() {
    // Object.assign(this, { single });
  }

  onSelect(event) {
    // console.log(event);
  }

}
