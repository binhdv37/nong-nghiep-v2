import {Component, Input} from '@angular/core';
import {BcMultiData} from '@modules/dft/models/bao-cao/bao-cao.model';

@Component({
  selector: 'tb-no-legend-line-chart',
  templateUrl: './no-legend-line-chart.component.html',
  styleUrls: ['./no-legend-line-chart.component.scss']
})
export class NoLegendLineChartComponent {

  // input data :
  @Input() data: BcMultiData[];
  @Input() chartTitle: string;

  // multi: any[];
  // view: any[] = [700, 300];

  // options
  legend = false;

  showLabels = true;
  animations = true;

  xAxis = true;
  yAxis = true;

  showYAxisLabel = false;
  showXAxisLabel = false;
  xAxisLabel = 'Year';
  yAxisLabel = 'Population';
  timeline = false;

  colorScheme = {
    domain: ['#5AA454', '#E44D25', '#CFC0BB', '#7aa3e5', '#a8385d', '#aae3f5']
  };

  constructor() {
    // Object.assign(this, { multi });
  }

  onSelect(data): void {
    // console.log('Item clicked', JSON.parse(JSON.stringify(data)));
  }

  onActivate(data): void {
    // console.log('Activate', JSON.parse(JSON.stringify(data)));
  }

  onDeactivate(data): void {
    // console.log('Deactivate', JSON.parse(JSON.stringify(data)));
  }

}
