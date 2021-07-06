import {ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
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
import {catchError, finalize, tap} from 'rxjs/operators';
import {PageData} from '@shared/models/page/page-data';
import {Role} from '@modules/dft/models/role.model';
import {RoleService} from '@modules/dft/service/role.service';
import {EditRoleComponent} from '@modules/dft/pages/role/edit-role/edit-role.component';
import {ToastrService} from 'ngx-toastr';
import {escapedHTML} from '@modules/dft/service/utils.service';

@Component({
  selector: 'tb-role',
  templateUrl: './role.component.html',
  styleUrls: ['./role.component.scss']
})
export class RoleComponent implements OnInit {

  @ViewChild('searchInput') searchInput: ElementRef;

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild('sort', { static: true }) sort: MatSort;

  isLoading$: Subject<boolean>;

  dataSource = new MatTableDataSource<Role>();
  displayedColumns: string[] = ['actions', 'name', 'note', 'createdTime'];
  defaultPageSize = 10;
  displayPagination = true;
  pageSizeOptions = [10, 20, 30, 40, 50, 100];
  pageLink: PageLink;
  sortOrder: SortOrder;

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
              private toastrService: ToastrService,
              private domSanitizer: DomSanitizer,
              private cd: ChangeDetectorRef,
              private roleService: RoleService) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit() {
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    },
      this.pageLink = new PageLink(this.defaultPageSize, 0, '', this.sortOrder);
    this.initData();
  }

  ngOnDestroy() {
    this.isLoading$.next(false);
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
    this.isLoading$.next(true);
    this.dataSource.data = [];
    this.roleService
      .getTenantRoles(this.pageLink).pipe(
      tap((pageData: PageData<Role>) => {
        if (pageData.data.length > 0) {
          this.dataSource.data = pageData.data;
          this.totalPages = pageData.totalPages;
          this.totalElements = pageData.totalElements;
        } else {
          this.dataSource.data = [];
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

  initData() {
    this.fetchData();
    this.searchEmitter$.next();
  }

  getData() {
    this.initQueryFind();
    this.fetchData();
    this.searchEmitter$.next();
  }

  onSearch(){
    this.paginator.pageIndex = 0; // set pageIndex back to 0
    this.initQueryFind();
    this.fetchData();
  }

  deleteEntity(id: string, name: string) {
    this.dialogService.confirm(
      this.translate.instant('dft.role.delete-role-title'),
      this.translate.instant('Vai trò ' + escapedHTML(name) + ' sẽ bị xóa!'),
      this.translate.instant('dft.role.cancel-button'),
      this.translate.instant('dft.role.delete-button'),
      true
    ).subscribe((result) => {
      if (result) {
        this.isLoading$.next(true);
        this.roleService
          .deleteRoleById(id).pipe(
          tap((data) => {
            if (data === 0){
              this.dialogService.alert('', this.translate.instant('dft.role.notify.cant-delete'));
            } else if (data === 1){
              this.toastrService.success(this.translate.instant('dft.role.notify.delete-success'), '', {
                positionClass: 'toast-bottom-right',
                timeOut: 3000,
              });
            }
            this.refresh();
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
    });
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
    this.searchInput.nativeElement.value = '';
    this.ngOnInit();
  }

  public handlePage(e: any) {
    this.defaultPageSize = e.pageSize;
  }

  openEditDialog(tabIndex: number, roleId: string, name: string, actionOpen: string): void {
    const dialogRef = this.dialog.open(EditRoleComponent, {
      data: { tabIndex, id: roleId, name, action: actionOpen }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === TableAction.ADD_ENTITY) { // Thêm mới thành công
        this.toastrService.success(this.translate.instant('dft.role.notify.save-success'), '', {
          positionClass: 'toast-bottom-right',
          timeOut: 3000,
        });
        this.refresh();
      } else if (result === TableAction.EDIT_ENTITY){
        this.toastrService.success(this.translate.instant('dft.role.notify.edit-success'), '', {
          positionClass: 'toast-bottom-right',
          timeOut: 3000,
        });
        this.refresh();
      }
    });
  }

}
