import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {merge, Observable, of, Subject, Subscription} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {DialogService} from '@core/services/dialog.service';
import {RpcHistoryService} from '@modules/dft/service/rpc/rpc-history.service';
import {RpcCommand} from '@modules/dft/models/rpc/rpc-command.model';
import {TimePageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {catchError, finalize, tap} from 'rxjs/operators';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {PageData} from '@shared/models/page/page-data';
import {DeviceRpc} from '@modules/dft/models/rpc/device-rpc.model';
import moment from 'moment';

@Component({
  selector: 'tb-damtom-rpc-history',
  templateUrl: './damtom-rpc-history.component.html',
  styleUrls: ['./damtom-rpc-history.component.scss']
})
export class DamtomRpcHistoryComponent implements OnInit, AfterViewInit {

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sort', {static: true}) sort: MatSort;

  // tslint:disable-next-line: no-input-rename
  @Input('damTomId') damTomId: string;

  deviceId: any = 0;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  dataSource = new MatTableDataSource<RpcCommand>();
  displayedColumns: string[] = ['commandTime', 'label', 'valueControl'];
  defaultPageSize = 20;
  displayPagination = true;
  pageSizeOptions = [5, 10, 20, 30, 40, 50];
  pageLink: TimePageLink;
  sortOrder: SortOrder;

  totalPages: number;
  tolalElements: number;

  minDate = new Date(1900, 0, 1);
  maxDate = new Date();

  formTimeQuery: FormGroup;
  startTimeQuery: number;
  endTimeQuery: number;

  listDeviceRpc: DeviceRpc[];

  private subscriptions$: Subscription[] = [];

  constructor(private deviceRpcService: DeviceRpcService,
              private rpcHistoryService: RpcHistoryService,
              protected store: Store<AppState>,
              public route: ActivatedRoute,
              public translate: TranslateService,
              private dialogService: DialogService,
              protected fb: FormBuilder) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.startTimeQuery = moment(Date.now()).startOf('day').valueOf();
    this.endTimeQuery = moment(Date.now()).endOf('day').valueOf();
    this.sortOrder = {
      property: 'commandTime',
      direction: Direction.DESC
    },
    this.pageLink = new TimePageLink(this.defaultPageSize, 0, '',
      this.sortOrder, this.startTimeQuery, this.endTimeQuery);
    this.getAllThietBiDieuKhien();
    this.initForm();
    this.initData();
  }

  ngAfterViewInit() {
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
    this.fetchData(this.deviceId);
  }

  getData() {
    this.initQueryFind();
    this.fetchData(this.deviceId);
  }

  getAllThietBiDieuKhien() {
    this.mainSource$.next(true);
    this.deviceRpcService.getAllRpcDevice(this.damTomId).pipe(
      tap((data: DeviceRpc[]) => {
        this.listDeviceRpc = data;
      }),
      finalize(() => {
        this.mainSource$.next(false);
      }),
      catchError(err => {
        console.log(err);
        return of({});
      })
    ).subscribe();
  }

  initQueryFind() {
    this.sortOrder = {
      property: this.sort.active,
      direction: Direction[this.sort.direction.toUpperCase()]
    },
      this.pageLink = new TimePageLink(
        this.paginator.pageSize,
        this.paginator.pageIndex,
        '',
        this.sortOrder,
        this.startTimeQuery,
        this.endTimeQuery);
  }

  initForm() {
    this.formTimeQuery = this.fb.group(
      {
        startRangeTime: new FormControl(moment(this.startTimeQuery)),
        endRangeTime: new FormControl(moment(this.endTimeQuery), Validators.required),
      },
      { validator: this.checkTimeRange });
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
      this.fetchData(this.deviceId);
    }
  }

  refresh() {
    this.sort.active = 'commandTime';
    this.sort.direction = 'desc';
    this.sortOrder = {
      property: this.sort.active,
      direction: Direction[this.sort.direction.toUpperCase()]
    },
    this.pageLink = new TimePageLink(this.paginator.pageSize, 0, '', this.sortOrder, this.startTimeQuery, this.endTimeQuery);
    this.fetchData(this.deviceId);
  }

  fetchData(deviceId?: string) {
    this.mainSource$.next(true);
    this.dataSource.data = [];
    this.rpcHistoryService
      .getListRpcHistory(this.pageLink, this.damTomId, deviceId).pipe(
      tap((pageRpcCommand: PageData<RpcCommand>) => {
        if (pageRpcCommand.data.length > 0) {
          this.dataSource.data = pageRpcCommand.data;
          this.totalPages = pageRpcCommand.totalPages;
          this.tolalElements = pageRpcCommand.totalElements;
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

  changeSelectedDevice(){
    this.refresh();
  }


}
