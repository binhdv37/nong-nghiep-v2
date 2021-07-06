import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {GroupRpc} from '@app/modules/dft/models/rpc/group-rpc.model';
import {DeviceRpcService} from '@app/modules/dft/service/rpc/device-rpc.service';
import {Observable, of, Subject} from 'rxjs';
import {catchError, finalize, tap} from 'rxjs/operators';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {MatDialog} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {ToastrService} from 'ngx-toastr';
import {TableAction} from '@modules/dft/models/action.model';
// tslint:disable-next-line:max-line-length
import {DamtomGroupRpcEditComponent} from '@modules/dft/pages/damtom/damtom-group-rpc/damtom-group-rpc-edit/damtom-group-rpc-edit.component';
import {DeviceRpc} from '@modules/dft/models/rpc/device-rpc.model';
import {escapedHTML} from '@modules/dft/service/utils.service';

@Component({
  selector: 'tb-damtom-group-rpc',
  templateUrl: './damtom-group-rpc.component.html',
  styleUrls: ['./damtom-group-rpc.component.scss']
})
export class DamtomGroupRpcComponent implements OnInit, OnDestroy {

  @Input() damTomId: string;

  @Input() rpcDeviceList: DeviceRpc[];

  public get tableAction(): typeof TableAction {
    return TableAction;
  }

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  groupRpcIsLoading: string[] = [];

  dataSource = new MatTableDataSource<GroupRpc>();
  displayedColumns: string[] = ['actions', 'name', 'createdTime'];

  intervalSetting;

  constructor(private deviceRpcService: DeviceRpcService,
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
    this.deviceRpcService
      .getAllGroupRpc(this.damTomId).pipe(
      tap((data: GroupRpc[]) => {
        if (data.length > 0) {
          this.dataSource.data = data;
          this.intervalSetting = setInterval(() => {
            data.forEach(groupRpc => {
              this.getStatusBoDieuKhien(groupRpc);
            });
          }, 10000);
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
      // tslint:disable-next-line: deprecation
    ).subscribe();
  }

  deleteEntity(groupRpc: GroupRpc) {
    this.dialogService.confirm(
      this.translate.instant('dft.group-rpc.delete-question'),
      this.translate.instant('dft.group-rpc.delete-warning', {0: escapedHTML(groupRpc.name)}),
      this.translate.instant('dft.group-rpc.cancel-button'),
      this.translate.instant('dft.group-rpc.delete-button'),
      true
    ).subscribe((result) => {
      if (result) {
        this.mainSource$.next(true);
        this.dataSource.data = [];
        this.deviceRpcService
          .deleteGroupRpc(groupRpc.groupRpcId).pipe(
          tap((data) => {
            const message = this.translate.instant('dft.group-rpc.delete-success');
            this.openSnackBarSuccess(message);
          }),
          finalize(() => {
            this.mainSource$.next(false);
            this.fetchData();
          }),
          catchError((error) => {
            console.log(error);
            const message = this.translate.instant('dft.group-rpc.delete-failed');
            this.openSnackBarSuccess(message);
            return of({});
          })
        ).subscribe();
      }
    });
  }

  startGroupRpc(groupRpc: GroupRpc) {
    this.dialogService.confirmSave(
      this.translate.instant('dft.group-rpc.delete-question'),
      this.translate.instant('dft.group-rpc.start-rpc-warning', {0: groupRpc.name}),
      this.translate.instant('dft.group-rpc.cancel-button'),
      this.translate.instant('dft.group-rpc.confirm-button'),
      true
    ).subscribe((result) => {
      if (result) {
        this.deviceRpcService.startGroupRpcDevice(groupRpc.groupRpcId).pipe(
          tap((data) => {
            const message = this.translate.instant('dft.group-rpc.send-success');
            this.openSnackBarSuccess(message);
            this.groupRpcIsLoading.push(groupRpc.groupRpcId);
          }),
          finalize(() => {
          }),
          catchError((error) => {
            console.log(error);
            const message = this.translate.instant('dft.group-rpc.send-failed');
            this.openSnackBarError(message);
            return of({});
          })
        ).subscribe();
      }
    });
  }

  stopGroupRpc(groupRpc: GroupRpc) {
    this.dialogService.confirmSave(
      this.translate.instant('dft.group-rpc.delete-question'),
      this.translate.instant('dft.group-rpc.stop-rpc-warning', {0: groupRpc.name}),
      this.translate.instant('dft.group-rpc.cancel-button'),
      this.translate.instant('dft.group-rpc.confirm-button'),
      true
    ).subscribe((result) => {
      if (result) {
        this.deviceRpcService.stopGroupRpcDevice(groupRpc.groupRpcId).pipe(
          tap((data) => {
            const message = this.translate.instant('dft.group-rpc.cancel-rpc-success');
            this.openSnackBarSuccess(message);
            const index = this.groupRpcIsLoading.indexOf(groupRpc.groupRpcId);
            this.groupRpcIsLoading.splice(index, 1);
          }),
          finalize(() => {
          }),
          catchError((error) => {
            console.log(error);
            const message = this.translate.instant('dft.group-rpc.cancel-rpc-failed');
            this.openSnackBarError(message);
            return of({});
          })
        ).subscribe();
      }
    });
  }

  getStatusBoDieuKhien(groupRpc: GroupRpc) {
    this.deviceRpcService.statusGroupRpcDevice(groupRpc.groupRpcId)
      .pipe(
        tap((data: any) => {
          if (data.loading) {
            if (!this.groupRpcIsLoading.includes(data.groupRpcId)) {
              this.groupRpcIsLoading.push(data.groupRpcId);
            }
          } else {
            if (this.groupRpcIsLoading.includes(data.groupRpcId)) {
              const index = this.groupRpcIsLoading.indexOf(data.groupRpcId);
              this.groupRpcIsLoading.splice(index, 1);
            }
          }
        }),
        finalize(() => {
        }),
        catchError((error) => {
          console.log(error);
          return of({});
        })
      ).subscribe();
  }


  openEditDialog(id: string, damTomId: string, groupRpcName: string, actionOpen: string, deviceRpcList: DeviceRpc[]): void {
    const dialogRef = this.dialog.open(DamtomGroupRpcEditComponent, {
      data: {id, damTomId, name: groupRpcName, action: actionOpen, deviceRpcList}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === TableAction.ADD_ENTITY) {
        const message = this.translate.instant('dft.group-rpc.create-success');
        this.openSnackBarSuccess(message);
        this.fetchData();
      }
      if (result === TableAction.EDIT_ENTITY) {
        const message = this.translate.instant('dft.group-rpc.update-success');
        this.openSnackBarSuccess(message);
        this.fetchData();
      }
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

