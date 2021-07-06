import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {BcMultiData, BcSingleData} from '@modules/dft/models/bao-cao/bao-cao.model';
import {ColorHelper} from '@swimlane/ngx-charts';

@Component({
  selector: 'tb-legend-bar-chart',
  templateUrl: './legend-bar-chart.component.html',
  styleUrls: ['./legend-bar-chart.component.scss']
})
export class LegendBarChartComponent implements OnInit, OnChanges {

  // input data :
  @Input() data: BcSingleData[];
  @Input() chartTitle: string;

  public activeEntries: any[] = [];
  public chartNames: string[];
  public colors: ColorHelper;

  // options
  gradient = true;
  showLegend = true;
  legendPosition = 'below';
  legendTitle = '';

  showLabels = true;
  animations = true;

  xAxis = true;
  yAxis = false;

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

  public legendLabelActivate(item: any): void {
    this.activeEntries = [item];
  }

  public legendLabelDeactivate(item: any): void {
    this.activeEntries = [];
  }

  ngOnInit() {
    this.chartNames = this.data.map((d: any) => d.name);
    this.colors = new ColorHelper(this.colorScheme, 'ordinal', this.chartNames, this.colorScheme);
  }

  ngOnChanges(): void {
    this.ngOnInit();
  }

}
