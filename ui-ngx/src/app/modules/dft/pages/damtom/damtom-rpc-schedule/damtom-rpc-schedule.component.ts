import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {DeviceRpc} from '@modules/dft/models/rpc/device-rpc.model';
import {TableAction} from '@modules/dft/models/action.model';
import {Observable, of, Subject} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {GroupRpc} from '@modules/dft/models/rpc/group-rpc.model';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {MatDialog} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {ToastrService} from 'ngx-toastr';
import {catchError, finalize, tap} from 'rxjs/operators';
import {escapedHTML} from '@modules/dft/service/utils.service';
// tslint:disable-next-line:max-line-length
import {RpcScheduleService} from '@modules/dft/service/rpc/rpc-schedule.service';
import {RpcSchedule} from '@modules/dft/models/rpc/schedule-rpc.model';
import {RpcScheduleEditComponent} from '@modules/dft/pages/damtom/damtom-rpc-schedule/rpc-schedule-edit/rpc-schedule-edit.component';

@Component({
  selector: 'tb-damtom-rpc-schedule',
  templateUrl: './damtom-rpc-schedule.component.html',
  styleUrls: ['./damtom-rpc-schedule.component.scss']
})
export class DamtomRpcScheduleComponent implements OnInit, OnDestroy {


  @Input() damTomId: string;

  @Input() rpcDeviceList: DeviceRpc[];

  public get tableAction(): typeof TableAction {
    return TableAction;
  }

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  groupRpcIsLoading: string[] = [];

  dataSource = new MatTableDataSource<RpcSchedule>();
  displayedColumns: string[] = ['actions', 'name', 'active', 'createdTime'];

  intervalSetting;

  constructor(private deviceRpcService: DeviceRpcService,
              private rpcScheduleService: RpcScheduleService,
              protected store: Store<AppState>,
              public route: ActivatedRoute,
              public translate: TranslateService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              private toastrSerive: ToastrService) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.fetchData();
  }

  fetchData() {
    this.mainSource$.next(true);
    this.dataSource.data = [];
    this.rpcScheduleService
      .getAllRpcSchedule(this.damTomId).pipe(
      tap((data: RpcSchedule[]) => {
        if (data.length > 0) {
          this.dataSource.data = data;
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

  deleteEntity(rpcSchedule: RpcSchedule) {
    this.dialogService.confirm(
      this.translate.instant('dft.group-rpc.delete-question'),
      this.translate.instant('dft.group-rpc.delete-warning',
        {0: escapedHTML(rpcSchedule.name)}),
      this.translate.instant('dft.group-rpc.cancel-button'),
      this.translate.instant('dft.group-rpc.delete-button'),
      true
    ).subscribe((result) => {
      if (result) {
        this.mainSource$.next(true);
        this.dataSource.data = [];
        this.rpcScheduleService
          .deleteRpcSchedule(rpcSchedule.id).pipe(
          tap((data) => {
            const message = 'Xóa hẹn giờ điều khiển thành công';
            this.openSnackBarSuccess(message);
          }),
          finalize(() => {
            this.mainSource$.next(false);
            this.fetchData();
          }),
          catchError((error) => {
            console.log(error);
            const message = 'Xóa hẹn giờ điều khiển thất bại';
            this.openSnackBarSuccess(message);
            return of({});
          })
        ).subscribe();
      }
    });
  }

  openEditDialog(id: string, damTomId: string, scheduleName: string,
                 actionOpen: string): void {
    const dialogRef = this.dialog.open(RpcScheduleEditComponent, {
      data: {id, damTomId, scheduleName, action: actionOpen}
    });

    dialogRef.afterClosed().subscribe(result => {
        this.fetchData();
      });
  }

  openSnackBarSuccess(message: string) {
    this.toastrSerive.success(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }

  openSnackBarWarning(message: string) {
    this.toastrSerive.warning(message, null, {
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

  isLoadingGroupRpc(groupRpc: GroupRpc) {
    return this.groupRpcIsLoading.includes(groupRpc.groupRpcId);
  }

  ngOnDestroy() {
    this.groupRpcIsLoading = [];
  }

}
