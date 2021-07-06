import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {Observable, of, Subject} from 'rxjs';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';
import {TranslateService} from '@ngx-translate/core';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ToastrService} from 'ngx-toastr';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {DeviceRpc} from '@modules/dft/models/rpc/device-rpc.model';
import {RpcRequest} from '@modules/dft/pages/damtom/damtom-rpc/damtom-rpc.component';
import {escapedHTML} from '@modules/dft/service/utils.service';
import {catchError, delay, finalize, tap} from 'rxjs/operators';
import {MatCheckboxChange} from '@angular/material/checkbox';

export interface DialogData {
  deviceRpc: DeviceRpc;
  valueControl: number;
}

@Component({
  selector: 'tb-rpc-setting-dialog',
  templateUrl: './rpc-setting-dialog.component.html',
  styleUrls: ['./rpc-setting-dialog.component.scss']
})
export class RpcSettingDialogComponent implements OnInit, OnDestroy {

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  entityForm: FormGroup;
  rpcRequest: RpcRequest;

  constructor(private deviceRpcService: DeviceRpcService,
              protected translate: TranslateService,
              protected store: Store<AppState>,
              protected fb: FormBuilder,
              private toastService: ToastrService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              public dialogRef: MatDialogRef<RpcSettingDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public dataRpc: DialogData) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.buildFormControlRpc();
  }

  buildFormControlRpc() {
    this.entityForm = this.fb.group({
      hourCallback: [0, [Validators.min(0), Validators.max(24), Validators.required]],
      minuteCallback: [0, [Validators.min(0), Validators.max(60), Validators.required]],
      secondsCallback: [0, [Validators.min(0), Validators.max(60), Validators.required]],
      loopOption: [false, []],
      hourLoop: [{ value: 0, disabled: true},
        [Validators.min(0), Validators.max(24), Validators.required]],
      minuteLoop: [{ value: 0, disabled: true},
        [Validators.min(0), Validators.max(60), Validators.required]],
      secondsLoop: [{ value: 0, disabled: true},
        [Validators.min(0), Validators.max(60), Validators.required]],
      loopCount: [{ value: 1, disabled: true}, [Validators.min(1), Validators.max(20), Validators.required]]
    });
  }

  onChangeLoopOption(ob: MatCheckboxChange) {
    if (ob.checked) {
      this.entityForm.get('hourLoop').enable();
      this.entityForm.get('minuteLoop').enable();
      this.entityForm.get('secondsLoop').enable();
      this.entityForm.get('loopCount').enable();
    } else {
      this.entityForm.get('hourLoop').disable();
      this.entityForm.get('minuteLoop').disable();
      this.entityForm.get('secondsLoop').disable();
      this.entityForm.get('loopCount').disable();
    }
  }

  onSubmitForm() {
    const timeCallbackValue = this.entityForm.get('hourCallback').value * 60 * 60 * 1000
      + this.entityForm.get('minuteCallback').value * 60 * 1000
      + this.entityForm.get('secondsCallback').value * 1000;
    let timeLoop = 0;
    let loopCount = 0;
    const loopOption = this.entityForm.get('minuteCallback').value;
    if (loopOption) {
      timeLoop = this.entityForm.get('hourLoop').value * 60 * 60 * 1000
        + this.entityForm.get('minuteLoop').value * 60 * 1000
        + this.entityForm.get('secondsLoop').value * 1000;
      loopCount = this.entityForm.get('loopCount').value;
    }

    const confirmMsg = this.dataRpc.valueControl ?
      this.translate.instant('dft.device-rpc.on-rpc-warning', {0: escapedHTML(this.dataRpc.deviceRpc.tenThietBi)}) :
      this.translate.instant('dft.device-rpc.off-rpc-warning', {0: escapedHTML(this.dataRpc.deviceRpc.tenThietBi)});
    this.dialogService.confirmSave(
      this.translate.instant('dft.group-rpc.delete-question'),
      confirmMsg,
      this.translate.instant('dft.group-rpc.cancel-button'),
      this.translate.instant('dft.group-rpc.confirm-button'),
      true
    ).subscribe((result) => {
      if (result) {
        const rpcRequestBody: RpcRequest = {
          damTomId: this.dataRpc.deviceRpc.damTomId,
          deviceId: this.dataRpc.deviceRpc.deviceId,
          deviceName: this.dataRpc.deviceRpc.tenThietBi,
          setValueMethod: this.dataRpc.deviceRpc.setValueMethod,
          valueControl: this.dataRpc.valueControl,
          callbackOption: true,
          timeCallback: timeCallbackValue,
          loopOption,
          loopCount,
          loopTimeStep: timeLoop,
        };
        this.mainSource$.next(true);
        this.deviceRpcService
          .setManualCommandRpc(rpcRequestBody).pipe(
          delay(500),
          tap((data) => {
            const msg = this.translate.instant('dft.device-rpc.send-success');
            this.openSnackBarSuccess(msg);
          }),
          finalize(() => {
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
    });
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

  ngOnDestroy() {
    this.entityForm = null;
    this.isLoading$ = null;
  }


}
