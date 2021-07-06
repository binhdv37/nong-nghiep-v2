import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ColorHelper} from '@swimlane/ngx-charts';
import {BcMultiData} from '@modules/dft/models/bao-cao/bao-cao.model';

@Component({
  selector: 'tb-b-line-chart',
  templateUrl: './b-line-chart.component.html',
  styleUrls: ['./b-line-chart.component.scss']
})
export class BLineChartComponent implements OnInit, OnChanges {
  // multi: any[];
  // view: any[] = [700, 300];

  @Input() chartTitle: string;

  @Input() data: BcMultiData[];

  // options
  legend = false;
  // showLabels: boolean = true;
  // animations: boolean = true;

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

  public activeEntries: any[] = [];
  public chartNames: string[];
  public colors: ColorHelper;

  public legendLabelActivate(item: any): void {
    this.activeEntries = [item];
  }

  public legendLabelDeactivate(item: any): void {
    this.activeEntries = [];
  }

  public ngOnInit(): void {
    // Get chartNames
    this.chartNames = this.data.map((d: any) => d.name);
    // Convert hex colors to ColorHelper for consumption by legend
    this.colors = new ColorHelper(this.colorScheme, 'ordinal', this.chartNames, this.colorScheme);
  }

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

  ngOnChanges(changes: SimpleChanges): void {
    // tinh lai chart name vs colors
    this.ngOnInit();
  }

}
