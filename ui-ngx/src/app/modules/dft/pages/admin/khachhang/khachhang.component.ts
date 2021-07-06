import {TableAction} from './../../../models/action.model';
import {EditKhachHangComponent} from './edit-khachhang/edit-khachhang.component';
import {KhachHangService} from '../../../service/khachhang/khachhang.service';
import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute} from '@angular/router';
import {AppState} from '@app/core/core.state';
import {DialogService} from '@app/core/public-api';
import {Store} from '@ngrx/store';
import {TranslateService} from '@ngx-translate/core';
import {merge, Observable, of, Subject, Subscription} from 'rxjs';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {KhachHang} from '@app/modules/dft/models/khachhang.model';
import {catchError, finalize, tap} from 'rxjs/operators';
import {Direction, PageData, PageLink, SortOrder} from '@app/shared/public-api';
import {ToastrService} from 'ngx-toastr';
import {escapedHTML} from '@modules/dft/service/utils.service';
import {DftAdminSettingsService} from '@modules/dft/service/khachhang/camera.service';

@Component({
  selector: 'tb-khachhang',
  templateUrl: './khachhang.component.html',
  styleUrls: ['./khachhang.component.scss']
})
export class KhachHangComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('searchInput') searchInput: ElementRef;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sort', {static: true}) sort: MatSort;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;
  textSearchMode = false;

  dataSource = new MatTableDataSource<KhachHang>();
  displayedColumns: string[] = ['actions', 'ma_khach_hang', 'ten_khach_hang', 't.phone', 't.email', 'ngay_bat_dau', 'active'];
  defaultPageSize = 10;
  displayPagination = true;
  pageSizeOptions = [5, 10, 20, 30, 40, 50, 100];
  pageLink: PageLink;
  sortOrder: SortOrder;

  totalPages: number;
  tolalElements: number;

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
              private toastrSerive: ToastrService,
              private khachHangService: KhachHangService,
              private cameraService: DftAdminSettingsService) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.sort.active = 'created_time';
    this.sort.direction = 'desc';
    this.sortOrder = {
      property: this.sort.active,
      direction: Direction[this.sort.direction.toUpperCase()]
    },
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
    this.khachHangService
      .getListKhachHang(this.pageLink).pipe(
      tap((pageKhachHang: PageData<KhachHang>) => {
        if (pageKhachHang.data.length > 0) {
          this.dataSource.data = pageKhachHang.data;
          this.totalPages = pageKhachHang.totalPages;
          this.tolalElements = pageKhachHang.totalElements;
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

  deleteEntity(khachHang: KhachHang) {
    this.dialogService.confirm(
      this.translate.instant('dft.admin.khachhang.delete-khachhang-title'),
      this.translate.instant('dft.admin.khachhang.delete-khachhang-text', {0: escapedHTML(khachHang.tenKhachHang)}),
      this.translate.instant('dft.admin.khachhang.no'),
      this.translate.instant('dft.admin.khachhang.yes'),
      true
    ).subscribe((result) => {
      if (result) {
        this.mainSource$.next(true);
        this.dataSource.data = [];
        this.khachHangService
          .deleteKhachHang(khachHang.id).pipe(
          tap((data) => {
            const message = this.translate.instant('dft.admin.khachhang.delete-success');
            this.openSnackBar(message);
            this.deleteShinobiAccount(khachHang.email);
            this.refresh();
          }),
          finalize(() => {
            this.mainSource$.next(false);
          }),
          catchError((error) => {
            console.log(error);
            this.dialogService.alert(
              this.translate.instant('dft.admin.khachhang.delete-failed'),
              this.translate.instant('dft.admin.khachhang.existCustomer'),
              this.translate.instant('dft.admin.khachhang.yes'));
            this.refresh();
            return of({});
          })
        ).subscribe();
      }
    });
  }

  deleteShinobiAccount(email: string) {
    this.mainSource$.next(true);
    this.cameraService
      .deleteShinobiAccount(email).pipe(
      tap((data) => {
      }),
      finalize(() => {
        this.mainSource$.next(false);
      }),
      catchError((error) => {
        const message = 'Xóa tài khoản camera thất bại';
        this.openSnackBarError(message);
        return of({});
      })
    ).subscribe();
  }

  initQueryFind() {
    this.sortOrder = {
      property: this.sort.active,
      direction: Direction[this.sort.direction.toUpperCase()]
    },
      this.pageLink = new PageLink(
        this.paginator.pageSize,
        this.paginator.pageIndex,
        this.searchInput.nativeElement.value,
        this.sortOrder);
  }

  refresh() {
    this.pageLink = new PageLink(
      this.paginator.pageSize,
      0,
      this.searchInput.nativeElement.value,
      this.sortOrder);
    this.fetchData();
    this.searchEmitter$.next();
  }

  openEditDialog(khachHangId: string, tenKhachHang: string, actionOpen: string): void {
    const dialogRef = this.dialog.open(EditKhachHangComponent, {
      data: {id: khachHangId, name: tenKhachHang, action: actionOpen}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === TableAction.ADD_ENTITY) {
        const message = this.translate.instant('dft.admin.khachhang.create-success');
        this.openSnackBar(message);
        this.refresh();
      }
      if (result === TableAction.EDIT_ENTITY) {
        const message = this.translate.instant('dft.admin.khachhang.update-success');
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

  openSnackBarError(message: string) {
    this.toastrSerive.error(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }

}
