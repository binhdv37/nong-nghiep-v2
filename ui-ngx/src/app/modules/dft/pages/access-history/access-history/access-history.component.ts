import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {merge, Subject, Subscription} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {PageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {TableAction} from '@modules/dft/models/action.model';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {MatDialog} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {DomSanitizer} from '@angular/platform-browser';
import {AccessHistoryService} from '@modules/dft/service/accessHistory/access-history.service';
import {catchError, finalize, tap} from 'rxjs/operators';
import {PageData} from '@shared/models/page/page-data';
import {AuditLog} from '@shared/models/audit-log.models';
import {DetailsAccessHistoryComponent} from '@modules/dft/pages/access-history/details-access-history/details-access-history.component';
import {AccessHistoryPage} from '@modules/dft/pages/access-history/access-history-page';
import { saveAs } from 'file-saver';
import {FormBuilder, FormGroup} from '@angular/forms';

@Component({
  selector: 'tb-access-history',
  templateUrl: './access-history.component.html',
  styleUrls: ['./access-history.component.scss']
})
export class AccessHistoryComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('searchInput') searchInput: ElementRef;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sort', {static: true}) sort: MatSort;

  isLoading$: Subject<boolean>;
  textSearchMode = false;
  // listEntityType: string[] = ['TENANT', 'CUSTOMER', 'USER', 'ROLE', 'DAM_TOM', 'DASHBOARD', 'ASSET',
  //   'DEVICE', 'ALARM', 'RULE_CHAIN', 'RULE_NODE', 'ENTITY_VIEW', 'WIDGETS_BUNDLE',
  //   'WIDGET_TYPE', 'TENANT_PROFILE', 'DEVICE_PROFILE', 'API_USAGE_STATE'];
  listEntityType: string[] = ['USER', 'ROLE', 'DAM_TOM', 'DEVICE', 'GATEWAY', 'ALARM', 'CAMERA', 'ALARM_RULE', 'GROUP_RPC',  'REPORT_SCHEDULE'];

  dataSource = new MatTableDataSource<AuditLog>();
  displayedColumns: string[] = ['details', 'createdTime', 'entityType', 'entityName', 'userName', 'actionType', 'actionStatus'];
  defaultPageSize = 10;
  displayPagination = true;
  pageSizeOptions = [10, 20, 30, 40, 50, 100];
  pageLink: PageLink;
  accessHistoryPage: AccessHistoryPage;
  sortOrder: SortOrder;
  startTime: number;
  endTime: number;
  type = '';
  defaultSelectedValue = 'ALL';
  formTimeQuery: FormGroup;
  minDate = new Date(1900, 0, 1);
  maxDate = new Date();

  totalPages: number;
  totalElements: number;

  public get tableAction(): typeof TableAction {
    return TableAction;
  }

  private searchEmitter$ = new Subject();
  private subscriptions$: Subscription[] = [];

  constructor(protected store: Store<AppState>,
              public route: ActivatedRoute,
              public translate: TranslateService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              private domSanitizer: DomSanitizer,
              private diaglogService: DialogService,
              private fb: FormBuilder,
              private cd: ChangeDetectorRef,
              private sanitizer: DomSanitizer,
              private accessHistoryService: AccessHistoryService) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
    this.endTime = Date.now();
    // const lastDay = now - 86400000;
    const x = new Date();
    x.setHours(0, 0, 0, 0);
    this.startTime = x.getTime(); // 0h ngay hom nay

    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    };
    this.accessHistoryPage = new AccessHistoryPage(this.defaultPageSize, 0, '', this.sortOrder, this.startTime, this.endTime);
    this.pageLink = new PageLink(this.defaultPageSize, 0, '', this.sortOrder);
    this.initData();
    this.initForm();
  }

  ngAfterViewInit(): void {
    $( '.mat-paginator-range-actions' ).append( '<div class="new-div"></div>' );
    $('.mat-paginator-range-actions >button').appendTo( $('.new-div') );
    // const sortSubscription = this.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));
    // this.subscriptions$.push(sortSubscription);

    const paginatorSubscriptions = merge(this.sort.sortChange, this.paginator.page).pipe(
      tap(() => this.getData()),
    ).subscribe();
    // this.subscriptions$.push(paginatorSubscriptions);
  }

  initForm() {
    this.formTimeQuery = this.fb.group(
      {
        startRangeTime: [new Date(this.startTime)],
        endRangeTime: [new Date(this.endTime)]
      },
      {validator: this.checkTimeRange});
  }

  checkTimeRange(group: FormGroup) {
    const startRangeTime = Date.parse(group.controls.startRangeTime.value);
    const endRangeTime = Date.parse(group.controls.endRangeTime.value);
    return startRangeTime < endRangeTime ?
      group.controls.endRangeTime.setErrors(null)
      : group.controls.endRangeTime.setErrors({timeRangeInvalid: true});
  }

  initData() {
    this.fetchData();
    this.searchEmitter$.next();
  }

  fetchData() {
    this.isLoading$.next(true);
    this.dataSource.data = [];
    this.accessHistoryService.getAllAccessHistory(this.accessHistoryPage).pipe(tap((pageLog: PageData<AuditLog>) => {
        if (pageLog.data.length > 0) {
          this.dataSource.data = pageLog.data;
          this.totalPages = pageLog.totalPages;
          this.totalElements = pageLog.totalElements;
        } else {
          this.dataSource.data = [];
          // this.totalPages = pageLog.totalPages;
          this.totalElements = pageLog.totalElements;
        }
      }),
      finalize(() => {
        this.isLoading$.next(false);
      }),
      catchError((error) => {
        console.log(error);
        return null;
      })
    ).subscribe();
  }

  onSearch(){
    this.paginator.pageIndex = 0;
    this.getData();
  }

  getData() {
    this.initQueryFindAll();
    this.fetchData();
    this.searchEmitter$.next();
  }

  onChangeEntityType(value: any): void {
    const finalValue = value.value + '';
    this.type = finalValue;
    this.initQueryFind(finalValue);
    this.fetchData();
    this.searchEmitter$.next();
  }

  initQueryFindAll(): void {
    if (this.type !== '') {
      this.initQueryFind(this.type);
    } else if (this.startTime > 0 && this.type !== '' && this.endTime > 0) {
      this.initQueryFind(this.type, this.startTime, this.endTime);
    } else if (this.type !== '' && this.startTime > 0) {
      this.initQueryFind(this.type, this.startTime);
    } else {
      this.initQueryFind();
    }
  }

  initQueryFind(entityType?: string, startTime?: number, endTime?: number) {
    this.sortOrder = {property: this.sort.active, direction: Direction[this.sort.direction.toUpperCase()]};
    this.accessHistoryPage = new AccessHistoryPage(
      this.paginator.pageSize,
      this.paginator.pageIndex,
      this.searchInput.nativeElement.value,
      this.sortOrder,
      this.startTime,
      this.endTime,
      entityType);
  }

  refresh() {
    this.searchInput.nativeElement.value = '';
    this.ngOnInit();
  }



  ngOnDestroy() {
    this.isLoading$.next(false);
  }

  exportExcel() {
    this.initQueryFindAll();
    const data = this.accessHistoryService.exportExcel(this.accessHistoryPage).subscribe(
      next => {
        if (this.type === ''){ saveAs(next, 'Lịch sử truy cập' + this.type + '.xlsx'); }
        else { saveAs(next, 'Lịch sử truy cập' + ' - ' +  this.type + '.xlsx'); }
      }
    );
  }

  openDialogDetails(id: any) {
    const dialogRef = this.dialog.open(DetailsAccessHistoryComponent, {
      data: {id}
    });
    // dialogRef.afterClosed().subscribe(result => {
    //   this.refresh();
    // });
  }

  initQueryFindByDateRange(startTime?: number, endTime?: number) {
    this.sortOrder = {property: this.sort.active, direction: Direction[this.sort.direction.toUpperCase()]};
    this.accessHistoryPage = new AccessHistoryPage(
      this.paginator.pageSize,
      this.paginator.pageIndex,
      this.searchInput.nativeElement.value,
      this.sortOrder,
      startTime,
      endTime);
  }

  // onStartTime(value: any): void {
  //   this.startTime = Date.parse(value);
  // }
  //
  // onEndTime(value: any): void {
  //   this.endTime = Date.parse(value);
  //   if  (this.type !== '') {
  //     this.initQueryFind(this.type, this.startTime, this.endTime);
  //   } else if (this.type === '') {
  //     this.initQueryFindByDateRange(this.startTime, this.endTime);
  //   }
  //   this.fetchData();
  //   this.searchEmitter$.next();
  //
  // }

  //
  onSubmitForm() {
    const startTs = Date.parse(this.formTimeQuery.get('startRangeTime').value);
    const endTs = Date.parse(this.formTimeQuery.get('endRangeTime').value);
    if (startTs > endTs) {
      this.diaglogService.alert('Khoảng thời gian sai',
        'Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!',
        this.translate.instant('dft.admin.khachhang.yes'));
    } else {
      this.startTime = startTs;
      this.endTime = endTs;
      this.getData();
    }
  }

  // binhdv
  convertEntityType(entityType: string){
      let result: string;
      switch (entityType) {
        case 'USER' :
          result = 'Tài khoản';
          break;
        case 'ROLE' :
          result = 'Vai trò';
          break;
        case 'DAM_TOM' :
          result = 'Đầm tôm';
          break;
        case 'DEVICE' :
          result = 'Thiết bị';
          break;
        case 'GATEWAY' :
          result = 'Bộ thiết bị';
          break;
        case 'ALARM' :
          result = 'Cảnh báo';
          break;
        case 'CAMERA' :
          result = 'Camera';
          break;
        case 'ALARM_RULE' :
          result = 'Luật cảnh báo';
          break;
        case 'GROUP_RPC' :
          result = 'Bộ điều khiển';
          break;
        case 'REPORT_SCHEDULE' :
          result = 'Lịch xuất báo cáo';
          break;
        default :
          result = entityType;
      }
      return result;
  }

  convertActionType(actionType: string){
    let result: string;
    switch (actionType) {
      case 'ADDED' :
        result = 'Thêm mới';
        break;
      case 'DELETED' :
        result = 'Xóa';
        break;
      case 'UPDATED' :
        result = 'Cập nhật';
        break;
      case 'LOGIN' :
        result = 'Đăng nhập';
        break;
      case 'LOGOUT' :
        result = 'Đăng xuất';
        break;
      case 'RPC_CALL' :
        result = 'Điều khiển thiết bị';
        break;
      case 'ALARM_CLEAR' :
        result = 'Xử lí cảnh báo';
        break;
      default:
        result = actionType;
    }
    return result;
  }

  convertActionStatus(actionStatus: string){
    let result: string;
    switch (actionStatus) {
      case 'SUCCESS' :
        result = 'Thành công';
        break;
      case 'FAILURE' :
        result = 'Thất bại';
        break;
      default :
        result = actionStatus;
    }
    return result;
  }

}
