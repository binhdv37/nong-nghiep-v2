import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {DeviceRpc} from '@app/modules/dft/models/rpc/device-rpc.model';
import {DeviceRpcService} from '@app/modules/dft/service/rpc/device-rpc.service';
import {Observable, of, Subject} from 'rxjs';
import {catchError, delay, finalize, takeUntil, tap} from 'rxjs/operators';
import {ToastrService} from 'ngx-toastr';
import {TranslateService} from '@ngx-translate/core';
import {DialogService} from '@core/services/dialog.service';
import {escapedHTML} from '@modules/dft/service/utils.service';
import moment from 'moment';
import {DftDeviceService} from '@modules/dft/service/device.service';
import {MatDialog} from '@angular/material/dialog';
import {RpcSettingDialogComponent} from '@modules/dft/pages/damtom/damtom-rpc/rpc-setting-dialog/rpc-setting-dialog.component';

export interface RpcRequest {
  damTomId: string;
  deviceId: string;
  deviceName: string;
  setValueMethod: string;
  valueControl: number;
  callbackOption: boolean;
  timeCallback: number;
  loopOption: boolean;
  loopCount: number;
  loopTimeStep: number;
}

@Component({
  selector: 'tb-damtom-rpc',
  templateUrl: './damtom-rpc.component.html',
  styleUrls: ['./damtom-rpc.component.scss']
})
export class DamtomRpcComponent implements OnInit, OnDestroy {

  // tslint:disable-next-line: no-input-rename
  @Input('damTomId') damTomId: string;

  protected ngUnsubscribe: Subject<void> = new Subject<void>();

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;
  listDeviceRpc: DeviceRpc[];
  deviceRequestList: string[] = [];

  intervalSetting;

  constructor(private deviceRpcService: DeviceRpcService,
              private dftDeviceService: DftDeviceService,
              protected translate: TranslateService,
              private dialogService: DialogService,
              public dialog: MatDialog,
              private toastService: ToastrService) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.fetchData();
  }

  fetchData() {
    this.mainSource$.next(true);
    this.listDeviceRpc = [];
    this.deviceRpcService
      .getAllRpcDevice(this.damTomId).pipe(
      tap((data: DeviceRpc[]) => {
        this.listDeviceRpc = data;
        this.listDeviceRpc.forEach(deviceRpc => {
          this.getStatusDevice(deviceRpc.deviceId, deviceRpc.tenThietBi);
        });
        this.intervalSetting = setInterval(() => {
          this.listDeviceRpc.forEach(deviceRpc => {
            this.getStatusDevice(deviceRpc.deviceId, deviceRpc.tenThietBi);
          });
        }, 10000);
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

  getStatusDevice(deviceId: string, deviceName: string) {
    this.deviceRpcService
      .getLastestStatusDevice(deviceId, deviceName).pipe(
      takeUntil(this.ngUnsubscribe),
      tap((data) => {
        this.listDeviceRpc.forEach(deviceRpc => {
          if (deviceRpc.deviceId === deviceId) {
            if (data[deviceName][0].ts > (moment().valueOf() - 300000)) {
              deviceRpc.statusDevice = parseInt(data[deviceName][0].value, 10);
            } else {
              deviceRpc.statusDevice = -1;
            }
          }
        });
      }),
      finalize(() => {
      }),
      catchError((error) => {
        console.log(error);
        return of({});
      })
    ).subscribe();
  }

  onControlMode(valueControl: number, deviceRpc: DeviceRpc) {
    const rpcRequestBody: RpcRequest = {
      damTomId: deviceRpc.damTomId,
      deviceId: deviceRpc.deviceId,
      deviceName: deviceRpc.tenThietBi,
      setValueMethod: deviceRpc.setValueMethod,
      valueControl,
      callbackOption: false,
      timeCallback: 0,
      loopOption: false,
      loopCount: 0,
      loopTimeStep: 0,
    };
    this.mainSource$.next(true);
    this.deviceRequestList.push(deviceRpc.deviceId);
    this.deviceRpcService
      .setManualCommandRpc(rpcRequestBody).pipe(
      delay(500),
      tap((data) => {
        const msg = this.translate.instant('dft.device-rpc.send-success');
        this.openSnackBarSuccess(msg);
        this.listDeviceRpc.forEach(elem => {
          if (elem.deviceId === deviceRpc.deviceId) {
            elem.statusDevice = valueControl;
          }
        });
      }),
      finalize(() => {
        const numberIndex = this.deviceRequestList.indexOf(deviceRpc.deviceId);
        this.deviceRequestList.splice(numberIndex, 1);
        this.mainSource$.next(false);
      }),
      catchError((error) => {
        console.log(error);
        const msg = this.translate.instant('dft.device-rpc.send-failed');
        this.openSnackBarError(msg);
        return of({});
      })
    ).subscribe();
  }

  openSnackBarError(message: string) {
    this.toastService.error(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }

  openSnackBarSuccess(message: string) {
    this.toastService.success(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }

  inControlLoading(deviceRpc: DeviceRpc) {
    return this.deviceRequestList.includes(deviceRpc.deviceId);
  }

  openDeviceSettingDialog(deviceRpc: DeviceRpc, controlValue: number) {
    const dialogRef = this.dialog.open(RpcSettingDialogComponent, {
      data: {deviceRpc, controlValue}
    });

    dialogRef.afterClosed().subscribe(result => {
      clearInterval(this.intervalSetting);
      this.fetchData();
    });
  }

  onToggle(event: any, deviceRpc: DeviceRpc) {
    const confirmMsg = event.checked ?
      this.translate.instant('dft.device-rpc.on-rpc-warning', {0: escapedHTML(deviceRpc.tenThietBi)}) :
      this.translate.instant('dft.device-rpc.off-rpc-warning', {0: escapedHTML(deviceRpc.tenThietBi)});
    this.dialogService.confirm(
      this.translate.instant('dft.group-rpc.delete-question'),
      confirmMsg,
      this.translate.instant('dft.group-rpc.cancel-button'),
      this.translate.instant('dft.group-rpc.confirm-button'),
      true
    ).subscribe((result) => {
      if (result) {
        if (event.checked) {
          this.onControlMode(1, deviceRpc);
        } else {
          this.onControlMode(0, deviceRpc);
        }
      } else {
        event.source.checked = !event.checked;
      }
    });
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalSetting);
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  getTimeDurationStatus(timestamp: number) : string {

    if(timestamp === 0) {
      return 'Không xác định';
    }

    const currentTime = moment().valueOf();
    const input = currentTime - timestamp;

    let totalHours, totalMinutes, totalSeconds, hours, minutes;
    totalSeconds = input / 1000;
    totalMinutes = totalSeconds / 60;
    totalHours = totalMinutes / 60;

    minutes = Math.floor(totalMinutes) % 60;
    hours = Math.floor(totalHours) % 60;

    if (hours !== 0) {
      return hours + ' giờ trước';
    }
    if (minutes !== 0) {
      return minutes + ' phút trước';
    }
  }

}
