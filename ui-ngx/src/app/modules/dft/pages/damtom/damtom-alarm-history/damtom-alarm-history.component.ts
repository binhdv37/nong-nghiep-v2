import {SelectionModel} from '@angular/cdk/collections';
import {AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ActivatedRoute} from '@angular/router';
import {AppState} from '@app/core/core.state';
import {DialogService} from '@app/core/public-api';
import {AlarmHistory} from '@app/modules/dft/models/alarm-history/alarm-history.model';
import {TelemetryKey} from '@app/modules/dft/models/alarm-history/telemetryKey.constant';
import {AlarmHistoryService} from '@app/modules/dft/service/alarm-history/alarm-history.service';
import {Direction, PageData, SortOrder, TimePageLink} from '@app/shared/public-api';
import {Store} from '@ngrx/store';
import {TranslateService} from '@ngx-translate/core';
import {ToastrService} from 'ngx-toastr';
import {merge, Observable, of, Subject, Subscription} from 'rxjs';
import {catchError, finalize, tap} from 'rxjs/operators';
import {FormBuilder, FormGroup} from '@angular/forms';


// export const MY_DATE_FORMATS = {
//   parse: {
//     dateInput: 'DD/MM/YYYY',
//   },
//   display: {
//     dateInput: 'DD/MM/YYYY',
//     monthYearLabel: 'MMMM YYYY',
//     dateA11yLabel: 'DD/MM/YYYY',
//     monthYearA11yLabel: 'MM/YYYY'
//   },
// };
@Component({
  selector: 'tb-damtom-alarm-history',
  templateUrl: './damtom-alarm-history.component.html',
  styleUrls: ['./damtom-alarm-history.component.scss'],
  providers: [
    // { provide: MAT_DATE_FORMATS, useValue: MY_DATE_FORMATS }
  ]
})
export class DamtomAlarmHistoryComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sort', {static: true}) sort: MatSort;

  // tslint:disable-next-line: no-input-rename
  @Input('damTomId') damTomId: string;

  @ViewChild('searchInput') searchInput: ElementRef;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;
  textSearchMode = false;

  dataSource = new MatTableDataSource<AlarmHistory>();
  selection = new SelectionModel<AlarmHistory>(true, []);
  // displayedColumns: string[] = ['select', 'actions', 'timeSnapshot', 'tenCanhBao', 'tenGateway', 'Temperature', 'pH', 'Salinity', 'DO', 'clear'];
  displayedColumns: string[] = ['select', 'actions', 'timeSnapshot', 'tenCanhBao', 'tenGateway', 'Temperature', 'Humidity', 'Luminosity', 'clear'];
  defaultPageSize = 20;
  displayPagination = true;
  pageSizeOptions = [5, 10, 20, 30, 40, 50];
  pageLink: TimePageLink;
  sortOrder: SortOrder;

  public get telemetryKey(): typeof TelemetryKey {
    return TelemetryKey;
  }

  totalPages: number;
  tolalElements: number;

  minDate = new Date(1900, 0, 1);
  maxDate = new Date();

  formTimeQuery: FormGroup;
  startTimeQuery: number;
  endTimeQuery: number;
  private searchEmitter$ = new Subject();
  private subscriptions$: Subscription[] = [];

  constructor(protected store: Store<AppState>,
              public route: ActivatedRoute,
              public translate: TranslateService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              private toastrSerive: ToastrService,
              protected fb: FormBuilder,
              private alarmHistoryService: AlarmHistoryService) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.sortOrder = {
      property: 'timeSnapshot',
      direction: Direction.DESC
    },
      this.startTimeQuery = null;
    this.endTimeQuery = null;
    this.pageLink = new TimePageLink(this.defaultPageSize, 0, '', this.sortOrder, this.startTimeQuery, this.endTimeQuery);
    this.initForm();
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

  initData() {
    this.fetchData();
    this.searchEmitter$.next();
  }

  getData() {
    this.initQueryFind();
    this.fetchData();
    this.searchEmitter$.next();
  }

  onSearch() {
    this.sortOrder = {
      property: this.sort.active,
      direction: Direction[this.sort.direction.toUpperCase()]
    };
    this.paginator.pageIndex = 0;
    this.initQueryFind();
    this.fetchData();
    this.searchEmitter$.next();
  }

  initQueryFind() {
    this.sortOrder = {
      property: this.sort.active,
      direction: Direction[this.sort.direction.toUpperCase()]
    },
      this.pageLink = new TimePageLink(
        this.paginator.pageSize,
        this.paginator.pageIndex,
        this.searchInput.nativeElement.value,
        this.sortOrder,
        this.startTimeQuery,
        this.endTimeQuery);
  }

  initForm() {
    this.formTimeQuery = this.fb.group(
      {
        startRangeTime: [],
        endRangeTime: [],
      },
      {validator: this.checkTimeRange});
  }


  checkTimeRange(group: FormGroup) {
    const startRangeTime = Date.parse(group.controls.startRangeTime.value);
    const endRangeTime = Date.parse(group.controls.endRangeTime.value);
    if ((Number.isNaN(endRangeTime) && Number.isNaN(startRangeTime))) {
      return group.controls.endRangeTime.setErrors(null);
    }
    return startRangeTime < endRangeTime ?
      group.controls.endRangeTime.setErrors(null)
      : group.controls.endRangeTime.setErrors({timeRangeInvalid: true});
  }

  onSubmitForm() {
    this.startTimeQuery = Date.parse(this.formTimeQuery.get('startRangeTime').value);
    this.endTimeQuery = Date.parse(this.formTimeQuery.get('endRangeTime').value);
    if (this.startTimeQuery > this.endTimeQuery) {
      this.dialogService.alert('Khoảng thời gian sai',
        'Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!',
        this.translate.instant('dft.admin.khachhang.yes'));
    } else {
      this.paginator.pageIndex = 0;
      this.initQueryFind();
      this.fetchData();
      this.searchEmitter$.next();
    }
  }

  refresh() {
    this.sort.active = 'timeSnapshot';
    this.sort.direction = 'desc';
    this.sortOrder = {
      property: this.sort.active,
      direction: Direction[this.sort.direction.toUpperCase()]
    },
      this.searchInput.nativeElement.value = '';
    this.pageLink = new TimePageLink(this.paginator.pageSize, 0, '', this.sortOrder, this.startTimeQuery, this.endTimeQuery);
    this.fetchData();
    this.searchEmitter$.next();
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    let numRows = 0;
    this.dataSource.data.forEach(row => {
      if (!row.clear && row.display) {
        numRows += 1;
      }
    });
    return numSelected >= numRows;
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => {
          if (!row.clear && row.display) {
            this.selection.select(row);
          }
        }
      );
  }

  openSnackBarSuccess(message: string) {
    this.toastrSerive.success(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: false,
      positionClass: 'toast-bottom-right',
    });
  }

  openSnackBarError(message: string) {
    this.toastrSerive.error(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: false,
      positionClass: 'toast-bottom-right',
    });
  }

  fetchData() {
    this.mainSource$.next(true);
    this.selection.clear();
    this.dataSource.data = [];
    this.alarmHistoryService
      .getListAlarmHistory(this.pageLink, this.damTomId).pipe(
      tap((pageAlarmHistory: PageData<AlarmHistory>) => {
        if (pageAlarmHistory.data.length > 0) {
          this.dataSource.data = pageAlarmHistory.data;
          this.totalPages = pageAlarmHistory.totalPages;
          this.tolalElements = pageAlarmHistory.totalElements;
        } else {
          this.dataSource.data = [];
        }
      }),
      finalize(() => {
        this.mainSource$.next(false);
      }),
      catchError((error) => {
        console.log(error);
        return of({});
      })
    ).subscribe();
  }

  onClearAlarm(snapshotId: string) {
    this.dialogService.confirmSave(
      this.translate.instant('dft.alarm-history.clear-title'),
      this.translate.instant('dft.alarm-history.clear-question'),
      this.translate.instant('dft.alarm-history.no'),
      this.translate.instant('dft.alarm-history.yes'),
      true
    ).subscribe((result) => {
      if (result) {
        this.clearAlarm(snapshotId);
      }
    });
  }

  clearAlarm(snapshotId: string) {
    this.mainSource$.next(true);
    this.alarmHistoryService
      .clearAlarm(snapshotId).pipe(
      tap((data) => {
        this.openSnackBarSuccess('Xác nhận đã xử lý cảnh báo thành công!');
        this.refresh();
      }),
      finalize(() => {
        this.mainSource$.next(false);
      }),
      catchError((error) => {
        this.openSnackBarError('Xác nhận đã xử lý cảnh báo thất bại!');
        console.log(error);
        return of({});
      })
    ).subscribe();
  }

  onClearAllAlarm() {
    this.dialogService.confirmSave(
      this.translate.instant('dft.alarm-history.clear-title'),
      this.translate.instant('dft.alarm-history.clear-list-question'),
      this.translate.instant('dft.alarm-history.no'),
      this.translate.instant('dft.alarm-history.yes'),
      true
    ).subscribe((result) => {
      if (result) {
        this.clearListAlarm();
      }
    });
  }

  clearListAlarm() {
    this.mainSource$.next(true);
    this.selection.selected.forEach(alarm => {
      if (!alarm.clear) {
        this.alarmHistoryService
          .clearAlarm(alarm.id).pipe(
          tap((data) => {
            this.selection.deselect(alarm);
            if (this.selection.isEmpty()) {
              this.mainSource$.next(false);
              this.openSnackBarSuccess('Xác nhận đã xử lý cảnh báo thành công!');
              this.refresh();
            }
          }),
          finalize(() => {
          }),
          catchError((error) => {
            console.log(error);
            return of({});
          })
        ).subscribe();
      }
    });
  }

  checkKeyAlarm(alarmKeys: string[], telemetryField: string) {
    if (alarmKeys === null || alarmKeys === undefined) {
      return false;
    }
    return alarmKeys.includes(telemetryField);
  }

}
