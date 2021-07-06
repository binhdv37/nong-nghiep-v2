import {AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Observable, of, Subject, Subscription} from 'rxjs';
import {MatPaginator} from '@angular/material/paginator';
import {TranslateService} from '@ngx-translate/core';
import {DuLieuCamBienService} from '@app/modules/dft/service/dlcb/dulieucambien.service';
import {catchError, finalize, tap} from 'rxjs/operators';
import {FormBuilder, FormGroup} from '@angular/forms';
import moment from 'moment';
import {BoDuLieuCamBien} from '@app/modules/dft/models/dlcb/bodulieucambien.model';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {DialogService} from '@app/core/public-api';


@Component({
  selector: 'tb-damtom-dlcb',
  templateUrl: './damtom-dlcb.component.html',
  styleUrls: ['./damtom-dlcb.component.scss'],
})
export class DamTomDLCBComponent implements OnInit, AfterViewInit, OnDestroy {

  constructor(
    public translate: TranslateService,
    private duLieuCamBienService: DuLieuCamBienService,
    protected fb: FormBuilder,
    private diaglogService: DialogService) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  // tslint:disable-next-line: no-input-rename
  @Input('damTomId') damTomId: string;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sort', {static: true}) sort: MatSort;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  minDate = new Date(1900, 0, 1);
  maxDate = new Date();

  formTimeQuery: FormGroup;
  startTimeQuery: number;
  endTimeQuery: number;

  totalPages: number;
  tolalElements: number;

  // dataSource = ELEMENT_DATA;
  dataSource = new MatTableDataSource<BoDuLieuCamBien>();

  // displayedColumns: string[] = ['khoangThoiGian', 'tenGateway', 'Temperature', 'pH', 'Salinity', 'DO'];
  displayedColumns: string[] = ['khoangThoiGian', 'tenGateway', 'Temperature', 'Humidity', 'Luminosity'];
  defaultPageSize = 10;
  displayPagination = true;
  pageSizeOptions = [5, 10, 20, 30, 40, 50, 100];
  private subscriptions$: Subscription[] = [];
  spans = {};

  tempRowId = null;
  tempRowCount = null;

  ngOnInit() {
    this.initForm();
    this.onSubmitForm();
    this.getDataByRangeTime(this.damTomId, this.startTimeQuery, this.endTimeQuery, this.defaultPageSize, 0);
  }

  ngOnDestroy() {
    this.startTimeQuery = null;
    this.endTimeQuery = null;
    this.dataSource.data = [];
  }

  ngAfterViewInit(): void {
    $( '.mat-paginator-range-actions' ).append( '<div class="new-div"></div>' );
    $('.mat-paginator-range-actions >button').appendTo( $('.new-div') );
    const paginatorSubscriptions = this.paginator.page.pipe(
      tap(() => this.getDataByRangeTime(this.damTomId, this.startTimeQuery,
        this.endTimeQuery, this.paginator.pageSize, this.paginator.pageIndex)),
    ).subscribe();
    this.subscriptions$.push(paginatorSubscriptions);
  }


  initForm() {
    this.formTimeQuery = this.fb.group(
      {
        startRangeTime: [moment().startOf('day').toDate()],
        endRangeTime: [moment().seconds(0).milliseconds(0).toDate()],
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

  onSubmitForm() {
    this.startTimeQuery = Date.parse(this.formTimeQuery.get('startRangeTime').value);
    this.endTimeQuery = Date.parse(this.formTimeQuery.get('endRangeTime').value);
    if (this.startTimeQuery > this.endTimeQuery) {
      this.diaglogService.alert('Khoảng thời gian sai',
        'Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!',
        this.translate.instant('dft.admin.khachhang.yes'));
    } else {
      this.paginator.pageIndex = 0;
      this.getDataByRangeTime(this.damTomId, this.startTimeQuery,
        this.endTimeQuery, this.paginator.pageSize, 0);
    }
  }

  getDataByRangeTime(damTomId: string, startTime: number, endTime: number, pageSize: number, page: number) {
    this.mainSource$.next(true);
    this.dataSource.data = [];
    const query = `damTomId=${damTomId}&startTime=${startTime}&endTime=${endTime}&pageSize=${pageSize}&page=${page}`;
    this.duLieuCamBienService.getListDuLieuCamBien(query)
      .pipe(
        tap(dataDlcb => {
          this.dataSource.data = [];
          if (dataDlcb.data.length > 0) {
            this.dataSource.data = dataDlcb.data;
            this.totalPages = dataDlcb.totalPages;
            this.tolalElements = dataDlcb.totalElements;
          }
        }),
        finalize(() => {
          this.mainSource$.next(false);
        }),
        catchError(err => {
          console.log(err);
          return of({});
        })
      )
      // tslint:disable-next-line: deprecation
      .subscribe();
  }

}

