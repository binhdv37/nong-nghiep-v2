import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {UsersDftService} from '@modules/dft/service/usersDft/users-dft.service';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {PageLink} from '@shared/models/page/page-link';
import {catchError, finalize, tap} from 'rxjs/operators';
import {PageData} from '@shared/models/page/page-data';
import {UsersDft} from '@modules/dft/models/usersDft/users-dft.model';
import {MatTableDataSource} from '@angular/material/table';
import {merge, of, Subject, Subscription} from 'rxjs';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {TableAction} from '@modules/dft/models/action.model';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {DomSanitizer} from '@angular/platform-browser';
import {SelectionModel} from '@angular/cdk/collections';

export interface DialogData {
  staffsArr: any;
}

@Component({
  selector: 'tb-damtomuser',
  templateUrl: './damtomuser.component.html',
  styleUrls: ['./damtomuser.component.scss']
})

export class DamtomuserComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('searchInput') searchInput: ElementRef;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sort', {static: true}) sort: MatSort;


  isLoading$: Subject<boolean>;
  textSearchMode = false;
  staffsArr: any;

  dataSource = new MatTableDataSource<UsersDft>();
  selection = new SelectionModel<UsersDft>(true, []);
  displayedColumns: string[] = ['actions', 'firstName', 'lastName', 'email'];
  defaultPageSize = 10000;
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
              private dialogRef: MatDialogRef<DamtomuserComponent>,
              public translate: TranslateService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              private domSanitizer: DomSanitizer,
              private cd: ChangeDetectorRef,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private usersDftService: UsersDftService) {
    this.isLoading$ = new Subject<boolean>();
  }


  ngOnInit(): void {
    if (!!this.data.staffsArr.data) {
      this.staffsArr = this.data.staffsArr.data;
    } else {
      this.staffsArr = this.data.staffsArr;
    }
    this.selection = new SelectionModel<UsersDft>(true, this.selection.selected);
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
    const sortSubscription = this.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));
    this.subscriptions$.push(sortSubscription);

    const paginatorSubscriptions = merge(this.sort.sortChange, 10000).pipe(
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
          for (const value of this.dataSource.data) {
            for (const staff of this.staffsArr) {
              if (!!staff.staff) {
                if (staff.staff.id === value.id.id) {
                  const checkExisted = this.selection.selected.filter(data => {
                    return data.id.id === value.id.id;
                  });
                  if (checkExisted.length > 0) {
                    // do nothing;
                  } else {
                    this.selection.select(value);
                  }
                }
              } else {
                if (staff.id.id === value.id.id) {
                  const checkExisted = this.selection.selected.filter(data => {
                    return data.id.id === value.id.id;
                  });
                  if (checkExisted.length > 0) {
                    // do nothing;
                  } else {
                    this.selection.select(value);
                  }
                }
              }
            }
          }
        } else {
          this.dataSource.data = [];
        }
      }),
      finalize(() => {
        this.isLoading$.next(false);
      }),
      catchError((error) => {
        return of({});
      })
    ).subscribe();
  }

  getData() {
    this.initQueryFind();
    this.fetchData();
    this.searchEmitter$.next();
  }

  initQueryFind() {
    this.sortOrder = {property: this.sort.active, direction: Direction[this.sort.direction.toUpperCase()]};
    this.pageLink = new PageLink(10000, 0, this.searchInput.nativeElement.value, this.sortOrder);
  }

  refresh() {
    this.searchInput.nativeElement.value = '';
    this.ngOnInit();
  }

  backToIndex(): void {
    this.dialogRef.close();
  }

  onSubmit() {
    console.log(this.selection.selected);
    this.dialogRef.close(this.selection.selected);
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.selection.select(row));
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }
}
