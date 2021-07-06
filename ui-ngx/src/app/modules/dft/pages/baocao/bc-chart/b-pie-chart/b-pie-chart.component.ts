import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ColorHelper} from '@swimlane/ngx-charts';
import {BcSingleData} from '@modules/dft/models/bao-cao/bao-cao.model';

@Component({
  selector: 'tb-b-pie-chart',
  templateUrl: './b-pie-chart.component.html',
  styleUrls: ['./b-pie-chart.component.scss']
})
export class BPieChartComponent implements OnInit, OnChanges {

  constructor() {
    // Object.assign(this, { single });
  }

  // single: any[];
  // view: any[] = [400, 400];

  @Input() chartTitle: string;
  @Input() data: BcSingleData[];

  // sum of value
  sum = 0;

  // binh dv
  public activeEntries: any[] = [];
  public chartNames: string[];
  public colors: ColorHelper;

  // options
  gradient = true;
  showLegend = true;
  legendPosition = 'below';
  legendTitle = '';


  colorScheme = {
    domain: ['#4270C1', '#E97C30', '#A3A3A3', '#FBBD00', '#5A99D3', '#6EAB46']
  };

  getTooltip(value: number){
    return `${value} ( ${this.parseFloat(value * 100 / this.sum, 2)}% )`; // gia tri - gia tri %
  }

  parseFloat(str: number, val: number){
    let result = str.toString();
    if (!result.includes('.')) {
      return result;
    }
    result = result.slice(0, (result.indexOf('.')) + val + 1);
    return result;
  }

  public legendLabelActivate(item: any): void {
    this.activeEntries = [item];
  }

  public legendLabelDeactivate(item: any): void {
    this.activeEntries = [];
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.calculateSum();
  }

  ngOnInit(): void {
    // Get chartNames
    this.chartNames = this.data.map((d: any) => d.name);
    // Convert hex colors to ColorHelper for consumption by legend
    this.colors = new ColorHelper(this.colorScheme, 'ordinal', this.chartNames, this.colorScheme);
    // calculate sum
    this.calculateSum();
  }

  calculateSum(){
    this.sum = 0;
    this.data.forEach(x => {
      this.sum += x.value;
    });
  }

  onSelect(event) {}

  onActivate(event) {}

  onDeactivate(event) {}

}
