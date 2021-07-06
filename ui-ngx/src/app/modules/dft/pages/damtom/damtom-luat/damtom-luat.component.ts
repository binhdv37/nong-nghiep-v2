import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {LuatService} from '@modules/dft/service/luat.service';
import {MatDialog} from '@angular/material/dialog';
import {Subject} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {DialogService} from '@core/services/dialog.service';
import {ToastrService} from 'ngx-toastr';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort, Sort} from '@angular/material/sort';
import {escapedHTML} from '@modules/dft/service/utils.service';
import {EditAlarmDialogComponent} from '@modules/dft/pages/damtom/damtom-luat/edit-alarm-dialog/edit-alarm-dialog.component';
import {DetailAlarmDialogComponent} from '@modules/dft/pages/damtom/damtom-luat/detail-alarm-dialog/detail-alarm-dialog.component';
import {CreateAlarmDialogComponent} from '@modules/dft/pages/damtom/damtom-luat/create-alarm-dialog/create-alarm-dialog.component';

export function getMinMaxNguong(key: string){
  switch (key) {
    case 'DO':
      return {min: 0, max: 1000};
    case 'pH':
      return {min: 0, max: 14};
    case 'Temperature':
      return {min: 0, max: 40};
    default:
      return {min: 0, max: 35};
  }
}

@Component({
  selector: 'tb-damtom-luat',
  templateUrl: './damtom-luat.component.html',
  styleUrls: ['./damtom-luat.component.scss']
})
export class DamtomLuatComponent implements OnInit, AfterViewInit {
  @ViewChild('searchInput') searchInput: ElementRef;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  @Input() damtomId: string;
  private searchEmitter$ = new Subject();
  dataSource: MatTableDataSource<any>;
  isLoading$: Subject<boolean>;
  // displayedColumns: string[] = ['actions', 'name', 'ruler'];
  displayedColumns: string[] = ['actions', 'name', 'keyType', 'active', 'createdTime'];
  defaultPageSize = 10;
  displayPagination = true;
  pageSizeOptions = [10, 20, 30, 40, 50, 100];

  totalPages: number;
  tolalElements: number;
  view = '';
  profileData;

  toanBoLuat: any[] = [];

  constructor(
    private luatService: LuatService,
    public dialog: MatDialog,
    private toast: ToastrService,
    private dialogService: DialogService,
  ) {
  }

  ngOnInit(): void {
    this.getData();
  }

  ngAfterViewInit() {
    $( '.mat-paginator-range-actions' ).append( '<div class="new-div"></div>' );
    $('.mat-paginator-range-actions >button').appendTo( $('.new-div') );
  }

  sortData(sort: Sort) {
    if (this.dataSource === undefined) {
      return;
    }
    const data = this.dataSource.data.slice();
    if (!sort.active || sort.direction === '') {
      this.dataSource.data = this.toanBoLuat;
      return;
    }

    this.dataSource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case 'name': return this.compare(a.alarmType, b.alarmType, isAsc);
        case 'keyType': return this.compare(this.getAlarmKeyType(a), this.getAlarmKeyType(b), isAsc);
        case 'active': return this.compare(this.getAlarmActive(a), this.getAlarmActive(b), isAsc);
        case 'createdTime': return this.compare(this.getCreatedTime(a), this.getCreatedTime(b), isAsc);
        default: return 0;
      }
    });
  }


 compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}

  getData(){
      this.luatService.getAllAlarmsV2(this.damtomId).subscribe(rs => {
        if (!!rs) {
          this.searchInput.nativeElement.value = '';
          this.dataSource = new MatTableDataSource(rs);
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        }
        // this.toanBoLuat = this.dataSource.data;
        this.toanBoLuat = rs;
      });
      this.searchEmitter$.next();
  }

  refresh() {
    this.ngOnInit();
  }

  applyFilter() {
    this.sort.direction = '';
    let filterValue = this.searchInput.nativeElement.value;
    filterValue = filterValue.trim().toLowerCase();
    this.dataSource.data = this.toanBoLuat.filter(element => {
      return element.alarmType.toLowerCase().includes(filterValue);
    });
    this.searchEmitter$.next();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(CreateAlarmDialogComponent, {
      data: {
        damtomId: this.damtomId
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  detailsRuler(element) {
    const dialogRef = this.dialog.open(DetailAlarmDialogComponent, {
      data: {
        damtomId: this.damtomId,
        alarmType: element.alarmType,
        key: element.createRules.CRITICAL.condition.condition[0].key.key,
        alarmId: element.id
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  editRuler(element) {
    console.log(element);
    const dialogRef = this.dialog.open(EditAlarmDialogComponent, {
      data: {
        damtomId: this.damtomId,
        alarmType: element.alarmType,
        key: element.createRules.CRITICAL.condition.condition[0].key.key,
        alarmId: element.id
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  deleteAlarmRule(id: string, alarmType: string){
    this.dialogService.confirm(
      'Bạn có chắc chắn không?',
      'Luật cảnh báo ' + escapedHTML(alarmType) + ' sẽ bị xóa',
      'Hủy', 'Xóa',
      true
    ).subscribe((result) => {
      if (result) {
        this.luatService.deleteAlarmV2(this.damtomId, id).subscribe(rs => {
            this.toast.success('Xóa thành công!', '', {
              positionClass: 'toast-bottom-right',
              timeOut: 3000,
            });
            this.refresh();
          },
          error => {
            if (error.status === 400){
              this.dialogService.alert('', error.message, 'ok', false);
            } else {
              this.dialogService.alert('', 'Hệ thống gặp lỗi không xác định', 'ok', false);
            }
          }
        );
      }
    });
  }

  getAlarmKeyType(element: any){
    return this.convertAlarmKeyType(element.createRules.CRITICAL.condition.condition[0].key.key);
  }

  getAlarmActive(element: any){
    return element.dftAlarmRule.active;
  }

  getCreatedTime(element: any){
    return (element.dftAlarmRule.createdTime === null) ? 1624381200000 : element.dftAlarmRule.createdTime;
  }

  convertAlarmKeyType(alarmKeyType: string){
    switch (alarmKeyType) {
      case 'Temperature':
        return 'Nhiệt độ';
      case 'pH':
        return 'pH';
      case 'DO':
        return 'DO';
      case 'Salinity':
        return 'Độ mặn';
      default :
        return alarmKeyType;
    }
  }

}
