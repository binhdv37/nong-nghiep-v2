import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import {UsersDft} from '@modules/dft/models/usersDft/users-dft.model';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {TranslateService} from '@ngx-translate/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {merge, Subject, Subscription} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {PageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {ActivatedRoute} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {DomSanitizer} from '@angular/platform-browser';
import {UsersDftService} from '@modules/dft/service/usersDft/users-dft.service';
import {catchError, finalize, tap} from 'rxjs/operators';
import {PageData} from '@shared/models/page/page-data';
import {TableAction} from '@modules/dft/models/action.model';
import {UserId} from '@shared/models/id/user-id';
import {CreateUsersDftComponent} from '@modules/dft/pages/users-dft/create-users-dft.component';
import {EditUsersDftComponent} from '@modules/dft/pages/users-dft/edit-users-dft.component';
import {UsersDftDetailsComponent} from '@modules/dft/pages/users-dft/users-dft-details.component';
import {NotificationUsersComponent} from '@modules/dft/pages/users-dft/notification-users/notification-users.component';
import {ToastrService} from 'ngx-toastr';
import {escapedHTML} from '@modules/dft/service/utils.service';

const INTERNAL_001 = 'Fail while processing in thingsboard.';
const OK_003 = 'Delete user successful in thingsboard.';
const BAD_005 = 'Fail while delete user from thingsboard.';
const BAD_009 = 'Cannot delete user has logged in before.';

@Component({
  selector: 'tb-users-dft',
  templateUrl: './users-dft.component.html',
  styleUrls: ['./users-dft.component.scss']
})
export class UsersDftComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('searchInput') searchInput: ElementRef;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sort', {static: true}) sort: MatSort;

  isLoading$: Subject<boolean>;
  textSearchMode = false;

  dataSource = new MatTableDataSource<UsersDft>();
  displayedColumns: string[] = ['actions', 'firstName', 'email', 'lastName', 'uc.enabled'];
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

  private searchEmitter$ = new Subject();
  private subscriptions$: Subscription[] = [];

  constructor(protected store: Store<AppState>,
              public route: ActivatedRoute,
              public translate: TranslateService,
              public dialog: MatDialog,
              private toastrService: ToastrService,
              private dialogService: DialogService,
              private domSanitizer: DomSanitizer,
              private cd: ChangeDetectorRef,
              private usersDftService: UsersDftService) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    };
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

  initData() {
    this.fetchData();
    this.searchEmitter$.next();
  }

  fetchData() {
    this.isLoading$.next(true);
    this.dataSource.data = [];
    this.usersDftService.getAllUsersDft(this.pageLink).pipe(tap((pageUsersDft: PageData<UsersDft>) => {
        if (pageUsersDft.data.length > 0) {
          this.dataSource.data = pageUsersDft.data;
          this.totalPages = pageUsersDft.totalPages;
          this.tolalElements = pageUsersDft.totalElements;
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

  onSearch(){
    this.paginator.pageIndex = 0;
    this.getData();
  }

  getData() {
    this.initQueryFind();
    this.fetchData();
    this.searchEmitter$.next();
  }

  initQueryFind() {
    this.sortOrder = {property: this.sort.active, direction: Direction[this.sort.direction.toUpperCase()]};
    this.pageLink = new PageLink(this.paginator.pageSize, this.paginator.pageIndex, this.searchInput.nativeElement.value, this.sortOrder);
  }

  deleteEntity(usersDft: UsersDft) {
    this.dialogService.confirm(
      this.translate.instant('dft.users.delete-usersDft-title'),
      this.translate.instant('dft.users.delete-usersDft-text', {userName: escapedHTML(usersDft.firstName)}),
      this.translate.instant('dft.users.dialog.cancel-button'),
      this.translate.instant('dft.users.dialog.delete-button'),
      true
    ).subscribe((result) => {
      if (result) {
        this.isLoading$.next(true);
        this.usersDftService.deleteUsersDft(usersDft.id.id).pipe(
          tap((data) => {
            this.checkResponse(data);
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

  checkResponse(deletedUser: UsersDft): void {
    if (deletedUser.responseCode === 500 && deletedUser.responseMessage === INTERNAL_001) {
      this.dialog.open(NotificationUsersComponent, {
        data: {action: 'delete', message: 'Hệ thống hiện tại đang bị lỗi, thử lại sau ít phút'}
      });
    }
    else if (deletedUser.responseCode === 400 && deletedUser.responseMessage === BAD_005) {
      this.dialog.open(NotificationUsersComponent, {
        data: {action: 'delete', message: 'Hệ thống hiện tại đang bị lỗi, thử lại sau ít phút'}
      });
    }
    // binhdv - k cho xóa tk đã từng đăng nhập
    else if (deletedUser.responseCode === 400 && deletedUser.responseMessage === BAD_009){
      this.dialogService.alert('', 'Tài khoản đã từng đăng nhập, không thể xóa', 'Đóng', false);
    }
    else if (deletedUser.responseCode === 200 && deletedUser.responseMessage === OK_003) {
      this.openSnackBar('Xóa tài khoản thành công!');
    }
    else {
      console.log(deletedUser.responseMessage);
    }
  }

  refresh() {
    this.searchInput.nativeElement.value = '';
    this.ngOnInit();
  }

  openEditDialog(usersDftId: UserId, index: number): void {
    const dialogRef = this.dialog.open(EditUsersDftComponent, {
      data: { id: usersDftId.id, index}
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  openCreateDialog(): void{
    const dialogRef = this.dialog.open(CreateUsersDftComponent);
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  openDetailsDialog(usersDftId: UserId): void {
    const dialogRef = this.dialog.open(UsersDftDetailsComponent, {
      data: { id: usersDftId.id}
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  openSnackBar(message: string): void {
    this.toastrService.success(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }
}
