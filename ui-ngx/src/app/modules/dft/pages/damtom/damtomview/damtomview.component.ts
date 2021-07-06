import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {of, Subject, Subscription} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {PageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {MatDialog} from '@angular/material/dialog';
import {Authority} from '@shared/models/authority.enum';
import {getCurrentAuthUser} from '@core/auth/auth.selectors';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {catchError, finalize, tap} from 'rxjs/operators';

@Component({
  selector: 'tb-damtomview',
  templateUrl: './damtomview.component.html',
  styleUrls: ['./damtomview.component.scss']
})
export class DamtomviewComponent implements OnInit, AfterViewInit {

  @ViewChild('searchInput') searchInput: ElementRef;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  mainSource$: Subject<boolean>;
  isLoading$: Subject<boolean>;
  textSearchMode = false;

  dataSource = new MatTableDataSource<any>();
  displayedColumns: string[] = ['actions', 'name', 'quantity', 'active', 'note'];
  displayedColumnsdt: string[] = ['stt', 'firstName', 'lastName', 'email'];
  displayPagination = true;
  pageLink: PageLink;
  sortOrder: SortOrder;

  totalPages: number;
  tolalElements: number;

  id: string;
  data;
  private searchEmitter$ = new Subject();
  authUser;



  public get authority(): typeof Authority {
    return Authority;
  }

  constructor(
    protected store: Store<AppState>,
    private activatedRoute: ActivatedRoute,
    private damTomService: DamTomService,
    public dialog: MatDialog
  ) {
    this.authUser = getCurrentAuthUser(this.store);
  }

  ngOnInit(): void {
    this.id = this.activatedRoute.snapshot.paramMap.get('id');
    this.getData();
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    },
    this.initData();
  }

  getData() {
    this.damTomService.getDamTom(this.id)
      .pipe(
        tap(data => {
          this.data = data;
          this.dataSource.data = this.data.staffs;
        }),
        finalize(() => {}),
        catchError(err => {
          return of({});
        })
      )
      .subscribe();
  }

  initData() {
    this.fetchData();
    this.searchEmitter$.next();
  }

  fetchData() {
    this.dataSource.data = [];
  }

  getDataThietBi() {
    this.fetchData();
    this.searchEmitter$.next();
  }

  checkAuth(auth: any): boolean {
    return this.authUser.scopes.includes(auth);
  }

  ngAfterViewInit() {
    // $('.mat-paginator-range-actions' ).append( '<div class="new-div2"></div>');
    // $('.mat-paginator-range-actions > button').appendTo( $('.new-div2'));
    // this.dataSource.paginator = this.paginator;
  }

}
