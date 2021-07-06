import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {PageLink} from '@shared/models/page/page-link';
import {merge, Subject, Subscription} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {ActivatedRoute} from '@angular/router';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {MatDialog} from '@angular/material/dialog';
import {ThietbicreateComponent} from '@modules/dft/pages/damtom/thietbicreate/thietbicreate.component';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {catchError, finalize, tap} from 'rxjs/operators';
import {ToastrService} from 'ngx-toastr';
import {DialogService} from '@core/services/dialog.service';
import {DamtomeditComponent} from '@modules/dft/pages/damtom/damtomedit/damtomedit.component';
import {ThietbieditComponent} from '@modules/dft/pages/damtom/thietbiedit/thietbiedit.component';
import {ThietbiwiewComponent} from '@modules/dft/pages/damtom/thietbiwiew/thietbiwiew.component';
import {escapedHTML} from '@modules/dft/service/utils.service';

@Component({
  selector: 'tb-damtom-thietbi',
  templateUrl: './damtom-thietbi.component.html',
  styleUrls: ['./damtom-thietbi.component.scss']
})
export class DamtomThietbiComponent implements OnInit, AfterViewInit {

  // tslint:disable-next-line:no-input-rename
  @Input('damtomId') damtomId: string;

  @ViewChild('searchInput') searchInput: ElementRef;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sort', {static: true}) sort: MatSort;

  defaultPageSize = 10;
  displayPagination = true;
  pageSizeOptions = [10, 20, 30, 40, 50, 100];
  displayedColumns: string[] = ['actions', 'device.name', 'quantity', 'active', 'note'];
  sortOrder: SortOrder;
  pageLink: PageLink;
  dataSource = new MatTableDataSource<any>();
  dataSource2 = new MatTableDataSource<any>();

  totalPages: number;
  tolalElements: number;
  private searchEmitter$ = new Subject();
  private subscriptions$: Subscription[] = [];

  constructor(private activatedRoute: ActivatedRoute,
              private damTomService: DamTomService,
              private toast: ToastrService,
              private dialogService: DialogService,
              public dialog: MatDialog) {
  }

  ngOnInit(): void {
    console.log('thiet bi', this.damtomId);
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    };
    this.pageLink = new PageLink(this.defaultPageSize, 0, '', this.sortOrder);
    this.getDevice();
  }

  ngAfterViewInit(): void {
    $( '.mat-paginator-range-actions' ).append( '<div class="new-div"></div>' );
    $('.mat-paginator-range-actions >button').appendTo( $('.new-div') );
    const sortSubscription = this.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));
    this.subscriptions$.push(sortSubscription);

    const paginatorSubscriptions = merge(this.sort.sortChange, this.paginator.page).pipe(
      tap(() => this.getDataThietBi()),
    ).subscribe();
    this.subscriptions$.push(paginatorSubscriptions);
  }

  getDevice() {
    this.dataSource.data = [];
    this.damTomService.getListDevice(this.damtomId, this.pageLink).subscribe(result => {
      if (result.data.length > 0) {
        console.log(123321, result);
        this.dataSource.data = result.data;
        this.totalPages = result.totalPages;
        this.tolalElements = result.totalElements;
        this.getLength();
      } else {
        this.dataSource.data = [];
      }
    });
  }

  getLength() {
    for (const element of this.dataSource.data) {
      this.damTomService.getGateWay(element.id).subscribe(rs => {
        const device = {
          value: element,
          sl: rs.listDevices.length
        };
        this.dataSource2.data.push(device);
      });
    }
  }


  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ThietbicreateComponent, {
      data: {id: this.damtomId}
    });
    dialogRef.afterClosed().subscribe(result => {
      this.ngOnInit();
    });
  }

  getDataThietBi() {
    this.initQueryFind();
    this.getDevice();
    this.searchEmitter$.next();
  }

  initQueryFind() {
    this.sortOrder = {property: this.sort.active, direction: Direction[this.sort.direction.toUpperCase()]};
    this.pageLink = new PageLink(this.paginator.pageSize,
      this.paginator.pageIndex, this.searchInput.nativeElement.value.trim(), this.sortOrder);
  }

  refresh() {
    this.ngOnInit();
  }

  delete(element) {
    this.dialogService.confirm(
      'Bạn có chắc chắn không?',
      'Bộ thiết bị ' + escapedHTML(element.device.name) + ' sẽ bị xóa',
      'Hủy', 'Xóa',
      true
    ).subscribe((result) => {
      if (result) {
        this.damTomService.deleteGateWay(element.id)
          .subscribe(rs => {
            if (rs === 1) {
              this.toast.error('Xóa không thành công!', 'Bộ thiết bị đang kích hoạt', {
                positionClass: 'toast-bottom-right',
                timeOut: 3000,
              });
            } else {
              this.toast.success('Xóa thành công!', '', {
                positionClass: 'toast-bottom-right',
                timeOut: 3000,
              });
              this.refresh();
            }
          });
      }
    });
  }

  edit(element) {
    const dialogRef = this.dialog.open(ThietbieditComponent, {
      data: {id: element}
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  view(element) {
    const dialogRef = this.dialog.open(ThietbiwiewComponent, {
      data: {id: element}
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }
}
