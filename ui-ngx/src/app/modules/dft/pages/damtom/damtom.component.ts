import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {AppState} from '@app/core/core.state';
import {Store} from '@ngrx/store';
import {TranslateService} from '@ngx-translate/core';
import {DamTom} from '../../models/damtom.model';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {merge, Observable, Subject, Subscription} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {PageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {TableAction} from '@modules/dft/models/action.model';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {DomSanitizer} from '@angular/platform-browser';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {catchError, finalize, tap} from 'rxjs/operators';
import {PageData} from '@shared/models/page/page-data';
import {DamtomcreateComponent} from '@modules/dft/pages/damtom/damtomcreate/damtomcreate.component';
import {DamtomeditComponent} from '@modules/dft/pages/damtom/damtomedit/damtomedit.component';
import {ToastrService} from 'ngx-toastr';
import {getCurrentAuthUser} from '@core/auth/auth.selectors';
import {Authority} from '@shared/models/authority.enum';
import {AuthUser} from '@shared/models/user.model';

@Component({
  selector: 'tb-damtom',
  templateUrl: './damtom.component.html',
  styleUrls: ['./damtom.component.scss']
})
export class DamTomComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('searchInput') searchInput: ElementRef;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sort', {static: true}) sort: MatSort;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;
  textSearchMode = false;

  dataSource = new MatTableDataSource<DamTom>();
  displayedColumns: string[] = ['actions', 'name', 'address', 'note', 'active'];
  defaultPageSize = 10;
  displayPagination = true;
  pageSizeOptions = [10, 20, 30, 40, 50, 100];
  pageLink: PageLink;
  sortOrder: SortOrder;

  totalPages: number;
  tolalElements: number;

  authUser: AuthUser;

  public get authority(): typeof Authority {
    return Authority;
  }

  public get tableAction(): typeof TableAction {
    return TableAction;
  }

  private searchEmitter$ = new Subject();
  private subscriptions$: Subscription[] = [];

  constructor(protected store: Store<AppState>,
              public route: ActivatedRoute,
              public router: Router,
              public translate: TranslateService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              private domSanitizer: DomSanitizer,
              private cd: ChangeDetectorRef,
              private toast: ToastrService,
              private damTomService: DamTomService) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
    this.authUser = getCurrentAuthUser(this.store);
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

  ngOnDestroy(): void {
  }

  ngOnInit(): void {
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    },
      this.pageLink = new PageLink(this.defaultPageSize, 0, '', this.sortOrder);
    this.initData();
  }

  initData() {
    this.fetchData();
    this.searchEmitter$.next();
  }

  fetchData() {
    this.mainSource$.next(true);
    this.dataSource.data = [];
    this.damTomService
      .getListDamTom(this.pageLink).pipe(
      tap((pageDamTom: PageData<DamTom>) => {
        if (pageDamTom.data.length > 0) {
          this.dataSource.data = pageDamTom.data;
          this.totalPages = pageDamTom.totalPages;
          this.tolalElements = pageDamTom.totalElements;
        } else {
          this.dataSource.data = [];
        }
        console.log(111, this.dataSource.data);
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
    this.sortOrder = {property: this.sort.active, direction: Direction[this.sort.direction.toUpperCase()]};
    this.pageLink = new PageLink(this.paginator.pageSize, this.paginator.pageIndex, this.searchInput.nativeElement.value, this.sortOrder);
  }

  refresh() {
    // this.searchInput.nativeElement.value = '';
    this.ngOnInit();
  }

  delete(value) {
    this.dialogService.confirm(
      'Bạn có chắc chắn không?',
      'Đầm tôm ' + value.name + ' sẽ bị xóa',
      'Hủy', 'Xóa',
      true
    ).subscribe((result) => {
      if (result) {
        this.mainSource$.next(true);
        this.damTomService
          .deleteDamTom(value.id.id).pipe(
          tap((data) => {
            this.refresh();
          }),
          finalize(() => {
            this.mainSource$.next(false);
          }),
          catchError((error) => {
            console.log(error);
            return null;
          })
        ).subscribe(rs => {
          if (rs === 1) {
            this.toast.error('Xóa không thành công!', 'Đầm tôm đang chứa thiết bị', {
              positionClass: 'toast-bottom-right',
              timeOut: 3000,
            });
          } else {
            this.toast.success('Xóa thành công!', '', {
              positionClass: 'toast-bottom-right',
              timeOut: 3000,
            });
          }
        });
      }
    });
  }


  openCreateDialog(): void {
    const dialogRef = this.dialog.open(DamtomcreateComponent);
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  edit(value): void {
    console.log('id', value);
    const dialogRef = this.dialog.open(DamtomeditComponent, {
      data: {id: value.id}
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  checkAuth(auth: any): boolean {
    return this.authUser.scopes.includes(auth);
  }
}
