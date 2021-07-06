import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {of, Subject} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {PageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {Authority} from '@shared/models/authority.enum';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ActivatedRoute} from '@angular/router';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {MatDialog} from '@angular/material/dialog';
import {getCurrentAuthUser} from '@core/auth/auth.selectors';
import {catchError, finalize, tap} from 'rxjs/operators';

@Component({
  selector: 'tb-damtom-table',
  templateUrl: './damtom-table.component.html',
  styleUrls: ['./damtom-table.component.scss']
})
export class DamtomTableComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild(MatPaginator) paginator: MatPaginator;

  mainSource$: Subject<boolean>;
  isLoading$: Subject<boolean>;
  textSearchMode = false;

  dataSource = new MatTableDataSource<any>();
  displayedColumnsdt: string[] = ['firstName', 'lastName', 'email'];
  displayPagination = true;
  pageLink: PageLink;
  sortOrder: SortOrder;

  totalPages: number;
  tolalElements: number;

  id: string;
  data;
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
  }

  getData() {
    this.dataSource.data = [];
    this.damTomService.getDamTom(this.id)
      .pipe(
        tap((data: any) => {
          this.dataSource.data = data.staffs;
        }),
        finalize(() => {
        }),
        catchError(err => {
          return of({});
        })
      )
      .subscribe();
  }

  checkAuth(auth: any): boolean {
    return this.authUser.scopes.includes(auth);
  }

  ngAfterViewInit() {
    // $('.mat-paginator-range-actions').append('<div class="new-div"></div>');
    // $('.mat-paginator-range-actions >button').appendTo($('.new-div'));
    // this.dataSource.paginator = this.paginator;
  }

  ngOnDestroy() {
    // $('.mat-paginator-range-actions').remove('<div class="new-div2"></div>');
    // $('.mat-paginator-range-actions >button').removeClass('new-div2');
    // this.dataSource.paginator = null;
  }

}
