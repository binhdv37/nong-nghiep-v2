import {Component, Input, OnInit} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';

@Component({
  selector: 'tb-bc-ketnoi-table',
  templateUrl: './bc-ketnoi-table.component.html',
  styleUrls: ['./bc-ketnoi-table.component.scss']
})
export class BcKetnoiTableComponent implements OnInit {
  @Input() dataSource = new MatTableDataSource<any>();

  @Input() isLoading$;

  // displayedColumns: string[] = ['thoiGian', 'Temperature', 'pH', 'Salinity', 'DO'];
  displayedColumns: string[] = ['thoiGian', 'Temperature', 'Humidity', 'Luminosity'];

  constructor() {
  }

  ngOnInit(): void {
  }

}
