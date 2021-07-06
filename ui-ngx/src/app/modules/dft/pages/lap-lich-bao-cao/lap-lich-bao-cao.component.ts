import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {merge, Observable, of, Subject, Subscription} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {KhachHang} from '@modules/dft/models/khachhang.model';
import {PageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {TableAction} from '@modules/dft/models/action.model';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {MatDialog} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {ToastrService} from 'ngx-toastr';
import {catchError, finalize, tap} from 'rxjs/operators';
import {PageData} from '@shared/models/page/page-data';
import {LapLichBaoCaoService} from '@modules/dft/service/lap-lich-bao-cao/lap-lich-bao-cao.service';
import {ReportSchedule} from '@modules/dft/models/lap-lich-bao-cao/ReportSchedule.model';
import { escapedHTML } from '../../service/utils.service';
import { CreateReportSchedule } from './edit-lap-lich-bao-cao/create-report-schedule.component';
import { DetailReportSchedule } from './detail-lap-lich-bao-cao/detail-report-schedule.component';
import { UpdateReportSchedule } from './update-lap-lich-bao-cao/update-report-schedule.component';



 const ReportNameMap =[
  {key :'WARNING_REPORT' , value :'Báo cáo cảnh báo'},
  {key :'MONITORING_DATA_REPORT' , value :'Báo cáo dữ liệu giám sát'},
  {key :'SENSOR_CONNECTION_REPORT' , value :'Báo cáo kết nối cảm biến'},
  {key :'NOTIFICATION_DATA_REPORT' , value :'Báo cáo dữ liệu thông báo'},
  {key :'SYNTHESIS_REPORT' , value :'Báo cáo tổng hợp'}
]
const DayInWeek = [
  { 'key': 1, 'value': 'Thứ 2' },
  { 'key': 2, 'value': 'Thứ 3' },
  { 'key': 3, 'value': 'Thứ 4' },
  { 'key': 4, 'value': 'Thứ 5' },
  { 'key': 5, 'value': 'Thứ 6' },
  { 'key': 6, 'value': 'Thứ 7' },
  { 'key': 0, 'value': 'Chủ nhật' }
]

@Component({
  selector: 'tb-lap-lich-bao-cao',
  templateUrl: './lap-lich-bao-cao.component.html',
  styleUrls: ['./lap-lich-bao-cao.component.scss']
})
export class LapLichBaoCaoComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild('sort', { static: true }) sort: MatSort;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  dataSource = new MatTableDataSource<ReportSchedule>();
  displayedColumns: string[] = ['actions', 'schedule_name', 'report_name', 'lap_lai', 'ngay_gio', 'active'];
  defaultPageSize = 10;
  displayPagination = true;
  pageSizeOptions = [10, 20, 30, 40, 50, 100];
  pageLink: PageLink;
  sortOrder: SortOrder;

  totalPages: number;
  tolalElements: number;

  public get tableAction(): typeof TableAction {
    return TableAction;
  }

  private subscriptions$: Subscription[] = [];

  constructor(protected store: Store<AppState>,
              public route: ActivatedRoute,
              private toastrService: ToastrService,
              public translate: TranslateService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              private toastrSerive: ToastrService,
              private lapLichBaoCaoService: LapLichBaoCaoService) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    };
    this.pageLink = new PageLink(this.defaultPageSize, 0, '', this.sortOrder);
    this.initData();
  }

  ngOnDestroy() {
    this.mainSource$.next(false);
  }

  ngAfterViewInit(): void {
    $( '.mat-paginator-range-actions' ).append( '<div class="new-div"></div>' );
    $('.mat-paginator-range-actions >button').appendTo( $('.new-div') );
    const sortSubscription = this.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));
    this.subscriptions$.push(sortSubscription);

    const paginatorSubscriptions = merge(this.sort.sortChange, this.paginator.page).pipe(
      tap(() => this.getData()),
    ).subscribe();
    this.subscriptions$.push(paginatorSubscriptions);
  }

  fetchData() {
    this.mainSource$.next(true);
    this.dataSource.data = [];
    this.lapLichBaoCaoService
      .getListReportSchedule(this.pageLink).pipe(
      tap((pageReportSchedule: PageData<ReportSchedule>) => {
        console.log(pageReportSchedule);

        if (pageReportSchedule.data.length > 0) {
          this.dataSource.data = pageReportSchedule.data;
          this.totalPages = pageReportSchedule.totalPages;
          this.tolalElements = pageReportSchedule.totalElements;
        } else {
          this.dataSource.data = [];
        }
      }),
      finalize(() => {
        this.mainSource$.next(false);
        // this.isLoading$.next(false);
      }),
      catchError((error) => {
        console.log(error);
        return of({});
      })
    ).subscribe();
  }

  initData() {
    this.fetchData();
  }

  getData() {
    this.initQueryFind();
    this.fetchData();
  }

  onSearch() {
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    };
      this.paginator.pageIndex = 0;
    this.pageLink = new PageLink(this.paginator.pageSize, this.paginator.pageIndex, '', this.sortOrder);
    this.fetchData();
  }

  deleteEntity(id: string, name: string) {
    this.dialogService.confirm(
      this.translate.instant('dft.report-schedule.delete-schedule-title'),
      this.translate.instant('Lịch xuất báo cáo ' + escapedHTML(name) + ' sẽ bị xóa!'),
      this.translate.instant('dft.report-schedule.cancel-button'),
      this.translate.instant('dft.report-schedule.delete-button'),
      true
    ).subscribe((result) => {
      if (result) {
        this.mainSource$.next(true);
        this.lapLichBaoCaoService
          .deleteReportSchedule(id).pipe(
          tap((data) => {
            console.log(data);
              this.toastrService.success(this.translate.instant('dft.report-schedule.notify.delete-success'), '', {
                positionClass: 'toast-bottom-right',
                timeOut: 3000,
              });
            this.refresh();
          },
          err=>{
            this.dialogService.alert('', this.translate.instant('dft.report-schedule.notify.cant-delete'));
          }),
          finalize(() => {
            this.mainSource$.next(false);
          }),
          catchError((error) => {
            console.log(error);
            return null;
          })
        ).subscribe();
      }
    });
  }

  initQueryFind() {
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    };
      this.pageLink = new PageLink(
        this.paginator.pageSize,
        this.paginator.pageIndex,
        '',
        this.sortOrder);
  }

  refresh() {
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    };
      this.pageLink = new PageLink(this.paginator.pageSize, 0, '', this.sortOrder);
    this.fetchData();
  }

  openEditDialog(): void {
    const dialogRef = this.dialog.open(CreateReportSchedule, {
      data: {}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result === TableAction.ADD_ENTITY) {
        const message = this.translate.instant('dft.report-schedule.notify.save-success');
        this.openSnackBar(message);
        this.refresh();
      }
      if (result === TableAction.EDIT_ENTITY) {
        const message = this.translate.instant('dft.report-schedule.notify.edit-success');
        this.openSnackBar(message);
        this.getData();
      }
    });
  }
  openDetailsDialog(reportScheduleId:string): void {
    const dialogRef = this.dialog.open(DetailReportSchedule, {
      data: { id: reportScheduleId}
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }
  openUpdateDialog(reportScheduleId:string): void {
    const dialogRef = this.dialog.open(UpdateReportSchedule, {
      data: {id: reportScheduleId}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result === TableAction.ADD_ENTITY) {
        const message = this.translate.instant('dft.report-schedule.notify.save-success');
        this.openSnackBar(message);
        this.refresh();
      }
      if (result === TableAction.EDIT_ENTITY) {
        const message = this.translate.instant('dft.report-schedule.notify.edit-success');
        this.openSnackBar(message);
        this.getData();
      }
    });
  }


  openSnackBar(message: string) {
    this.toastrSerive.success(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }

  convertCronToRepeatName(cron: string) {
    var [second, minutes, hour, day, month, week] = cron.split(' ');
    let repeat;
    if (day === '*' && month === '*' && week === '*') {
      repeat = 'Ngày';
    }
    else if (month === '*' && week === '*' && day !== '*') {
      repeat = 'Tháng';
    }
    else if (month === '*' && day === '*' && week !== '*') {
      repeat = 'Tuần';
    }
    return repeat;
  }
  getTenBaoCao(key: string) {
    return ReportNameMap.find(entries =>
      entries.key === key
    )
  }
  convertCronToDate(cron: string){
    let [second, minutes, hour, day, month, week] = cron.split(' ');
    let repeat;
    if (day === '*' && month === '*' && week === '*') {
      repeat = 'Ngày';
    }
    else if (month === '*' && week === '*' && day !== '*') {
      repeat = 'Tháng';
    }
    else if (month === '*' && day === '*' && week !== '*') {
      repeat = 'Tuần';
    }

    if(repeat==='Tuần'){
      repeat = `${hour}h${minutes} `+ DayInWeek.find(entries=>entries.key===parseInt(week)).value;
    }
    else if(repeat==='Tháng'){
      repeat = `${hour}h${minutes} Ngày ${day} Hàng  ${repeat}`;
    }
    else if(repeat==='Ngày'){
      repeat = `${hour}h${minutes} Hàng ${repeat}`;
    }

    return repeat;
  }

}
