import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {Observable, of, Subject} from 'rxjs';
import {AbstractControl, FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {RpcRequest} from '@modules/dft/pages/damtom/damtom-rpc/damtom-rpc.component';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';
import {TranslateService} from '@ngx-translate/core';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ToastrService} from 'ngx-toastr';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {RpcScheduleService} from '@modules/dft/service/rpc/rpc-schedule.service';
import {catchError, finalize, tap} from 'rxjs/operators';
import {DeviceRpc} from '@modules/dft/models/rpc/device-rpc.model';
import {GroupRpc} from '@modules/dft/models/rpc/group-rpc.model';
import {RpcSchedule} from '@modules/dft/models/rpc/schedule-rpc.model';
import {TableAction} from '@modules/dft/models/action.model';
import {RpcScheduleValidator} from '@modules/dft/service/rpc/rpc-schedule.validator';

export const timeCallbackValidator: ValidatorFn = (abstractControl: AbstractControl): ValidationErrors | null => {
  const fg = (abstractControl as FormGroup);
  let isInValid = false;
  if (fg.controls.callbackOption.value) {
    if (fg.controls.hourCallback.value <= 0
      && fg.controls.minuteCallback.value <= 0
      && fg.controls.secondsCallback.value <= 0) {
      isInValid = true;
    }
  }

  if (isInValid) {
    return {callbackTimeInvalid: true};
  }
  return null;
};

export const cronDayOfWeekValidator: ValidatorFn = (abstractControl: AbstractControl): ValidationErrors | null => {
  const fg = (abstractControl as FormGroup);
  let isInValid = false;
  if (!fg.controls.cronT2.value
    && !fg.controls.cronT3.value
    && !fg.controls.cronT4.value
    && !fg.controls.cronT5.value
    && !fg.controls.cronT6.value
    && !fg.controls.cronT7.value
    && !fg.controls.cronCN.value) {
    isInValid = true;
  }

  if (isInValid) {
    return {cronDayOfWeekInvalid: true};
  }
  return null;
};

export const selectRpcValidator: ValidatorFn = (abstractControl: AbstractControl): ValidationErrors | null => {
  const fg = (abstractControl as FormGroup);
  let isInValid = false;
  if (fg.controls.isSelectRpc.value) {
    if (fg.controls.deviceId.value == null) {
      isInValid = true;
    }
  } else {
    if (fg.controls.groupRpcId.value == null) {
      isInValid = true;
    }
  }

  if (isInValid) {
    return {selectRpcInvalid: true};
  }
  return null;
};

export interface DialogData {
  id: string;
  scheduleName: string;
  damTomId: string;
  action: string;
}

@Component({
  selector: 'tb-rpc-schedule-edit',
  templateUrl: './rpc-schedule-edit.component.html',
  styleUrls: ['./rpc-schedule-edit.component.scss']
})
export class RpcScheduleEditComponent implements OnInit, OnDestroy {

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  entityForm: FormGroup;
  isEditEntity = false;
  isAddEntity = false;
  isDetailsEntity = false;
  rpcRequest: RpcRequest;

  listGroupRpc: GroupRpc[] = [];
  listDeviceRpc: DeviceRpc[] = [];

  rpcScheduleId: string = null;
  rpcSettingId: string = null;

  dayOfWeekSchdule = {
    T2: true,
    T3: true,
    T4: true,
    T5: true,
    T6: true,
    T7: true,
    CN: true,
  };

  constructor(private deviceRpcService: DeviceRpcService,
              private rpcScheduleService: RpcScheduleService,
              private rpcScheduleValidator: RpcScheduleValidator,
              protected translate: TranslateService,
              protected store: Store<AppState>,
              protected fb: FormBuilder,
              private toastService: ToastrService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              public dialogRef: MatDialogRef<RpcScheduleEditComponent>,
              @Inject(MAT_DIALOG_DATA) public dataRpc: DialogData) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.buildFormControlRpc();
    this.getAllThietBiDieuKhien();
    this.getAllNhomDieuKhien();
    if (!!this.dataRpc.id) {
      this.rpcScheduleId = this.dataRpc.id;
    }
    if (!!this.rpcScheduleId && this.rpcScheduleId.length > 0) {
      this.initFormData(this.rpcScheduleId);
    }
  }

  buildFormControlRpc() {
    this.entityForm = this.fb.group({
        name: new FormControl('', [Validators.maxLength(255), Validators.required],
          !!this.dataRpc.id ? this.rpcScheduleValidator.rpcSchuduleNameValidator(this.dataRpc.damTomId)
            : this.rpcScheduleValidator.editRpcScheduleNameValidator(this.dataRpc.damTomId, this.dataRpc.id)),
        active: [true],
        isSelectRpc: [true, [Validators.required]],
        groupRpcId: [null],
        deviceId: [null],
        cronHours: [0, [Validators.min(0), Validators.max(24), Validators.required]],
        cronMinutes: [0, [Validators.min(0), Validators.max(60), Validators.required]],
        cronSeconds: [0, [Validators.min(0), Validators.max(60), Validators.required]],
        cronT2: [true],
        cronT3: [true],
        cronT4: [true],
        cronT5: [true],
        cronT6: [true],
        cronT7: [true],
        cronCN: [true],
        valueControl: [false],
        callbackOption: [false, []],
        hourCallback: [{value: 0, disabled: true}, [Validators.min(0), Validators.max(24), Validators.required]],
        minuteCallback: [{value: 0, disabled: true}, [Validators.min(0), Validators.max(60), Validators.required]],
        secondsCallback: [{value: 0, disabled: true}, [Validators.min(0), Validators.max(60), Validators.required]],
        loopOption: [{value: 0, disabled: true}, []],
        hourLoop: [{value: 0, disabled: true},
          [Validators.min(0), Validators.max(24), Validators.required]],
        minuteLoop: [{value: 0, disabled: true},
          [Validators.min(0), Validators.max(60), Validators.required]],
        secondsLoop: [{value: 0, disabled: true},
          [Validators.min(0), Validators.max(60), Validators.required]],
        loopCount: [{value: 1, disabled: true}, [Validators.min(1), Validators.max(20), Validators.required]]
      },
      {
        validators: [
          timeCallbackValidator,
          cronDayOfWeekValidator,
          selectRpcValidator
        ]
      }
    );
  }

  initFormData(id: string) {
    this.mainSource$.next(true);
    this.rpcScheduleService.getRpcScheduleById(id)
      .pipe(
        tap((data: RpcSchedule) => {
          this.rpcSettingId = data.rpcSettingId;
          this.entityForm.patchValue({name: data.name});
          this.entityForm.patchValue({isSelectRpc: data.deviceId ? true : false});
          this.entityForm.patchValue({deviceId: data.deviceId});
          this.entityForm.patchValue({groupRpcId: data.groupRpcId});
          this.entityForm.patchValue({valueControl: data.valueControl});
          this.entityForm.patchValue({active: data.active});
          if (data.loopOption) {
            this.entityForm.patchValue({loopCount: data.loopCount});
          }

          const totalSecondsCallback = data.timeCallback / 1000;
          const totalMinutesCallback = totalSecondsCallback / 60;
          const totalHoursCallback = totalMinutesCallback / 60;
          const secondsCallback = Math.floor(totalSecondsCallback) % 60;
          const minuteCallback = Math.floor(totalMinutesCallback) % 60;
          const hourCallback = Math.floor(totalHoursCallback) % 60;
          this.entityForm.patchValue({hourCallback});
          this.entityForm.patchValue({minuteCallback});
          this.entityForm.patchValue({secondsCallback});

          const totalSecondsLoop = data.loopTimeStep / 1000;
          const totalMinuteLoop = totalSecondsLoop / 60;
          const totalHourLoop = totalMinuteLoop / 60;
          const hourLoop = Math.floor(totalHourLoop) % 60;
          const minuteLoop = Math.floor(totalMinuteLoop) % 60;
          const secondsLoop = Math.floor(totalSecondsLoop) % 60;
          this.entityForm.patchValue({hourLoop});
          this.entityForm.patchValue({minuteLoop});
          this.entityForm.patchValue({secondsLoop});

          this.entityForm.patchValue({callbackOption: data.callbackOption});
          if (data.callbackOption) {
            this.entityForm.get('hourCallback').enable();
            this.entityForm.get('minuteCallback').enable();
            this.entityForm.get('secondsCallback').enable();
            this.entityForm.get('loopOption').enable();
          }

          this.entityForm.patchValue({loopOption: data.loopOption});
          if (data.loopOption) {
            this.entityForm.get('hourLoop').enable();
            this.entityForm.get('minuteLoop').enable();
            this.entityForm.get('secondsLoop').enable();
            this.entityForm.get('loopCount').enable();
          }

          const [seconds, minutes, hours, days, month, week] = data.cron.split(' ');
          const weekLoop = week.split(',');
          if (!weekLoop.includes('1')) {
            this.entityForm.get('cronT2').setValue(false);
          }

          if (!weekLoop.includes('2')) {
            this.entityForm.get('cronT3').setValue(false);
          }

          if (!weekLoop.includes('3')) {
            this.entityForm.get('cronT4').setValue(false);
          }

          if (!weekLoop.includes('4')) {
            this.entityForm.get('cronT5').setValue(false);
          }

          if (!weekLoop.includes('5')) {
            this.entityForm.get('cronT6').setValue(false);
          }

          if (!weekLoop.includes('6')) {
            this.entityForm.get('cronT7').setValue(false);
          }

          if (!weekLoop.includes('7')) {
            this.entityForm.get('cronCN').setValue(false);
          }

          const cronHours = parseInt(hours, 10);
          const cronMinutes = parseInt(minutes, 10);
          const cronSeconds = parseInt(seconds, 10);
          this.entityForm.patchValue({cronHours});
          this.entityForm.patchValue({cronMinutes});
          this.entityForm.patchValue({cronSeconds});
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

  converToNumber(arr: any) {
    if (arr === '*') {
      return 0;
    } else {
      return Number(arr);
    }
  }

  onChangeCallbackOption(ob: MatCheckboxChange) {
    if (ob.checked) {
      this.entityForm.get('hourCallback').enable();
      this.entityForm.get('minuteCallback').enable();
      this.entityForm.get('secondsCallback').enable();
      this.entityForm.get('loopOption').enable();
    } else {
      this.entityForm.get('hourCallback').disable();
      this.entityForm.get('minuteCallback').disable();
      this.entityForm.get('secondsCallback').disable();
      this.entityForm.get('loopOption').disable();
      this.entityForm.get('hourLoop').disable();
      this.entityForm.get('minuteLoop').disable();
      this.entityForm.get('secondsLoop').disable();
      this.entityForm.get('loopCount').disable();

      this.entityForm.get('loopOption').setValue(false);
      this.entityForm.get('hourLoop').setValue(0);
      this.entityForm.get('minuteLoop').setValue(0);
      this.entityForm.get('secondsLoop').setValue(0);
      this.entityForm.get('loopCount').setValue(1);
    }
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

      this.entityForm.get('hourLoop').setValue(0);
      this.entityForm.get('minuteLoop').setValue(0);
      this.entityForm.get('secondsLoop').setValue(0);
      this.entityForm.get('loopCount').setValue(1);
    }
  }

  getAllThietBiDieuKhien() {
    this.mainSource$.next(true);
    this.deviceRpcService.getAllRpcDevice(this.dataRpc.damTomId).pipe(
      tap((data: DeviceRpc[]) => {
        this.listDeviceRpc = data;
      }),
      finalize(() => {
        this.mainSource$.next(false);
      }),
      catchError(err => {
        console.log(err);
        return of({});
      })
    ).subscribe();
  }

  getAllNhomDieuKhien() {
    this.mainSource$.next(true);
    this.deviceRpcService.getAllGroupRpc(this.dataRpc.damTomId).pipe(
      tap((data: GroupRpc[]) => {
        this.listGroupRpc = data;
      }),
      finalize(() => {
        this.mainSource$.next(false);
      }),
      catchError(err => {
        console.log(err);
        return of({});
      })
    ).subscribe();
  }

  onSubmitForm() {
    const callbackOption = this.entityForm.get('callbackOption').value;
    let timeCallbackValue = 0;
    if (callbackOption) {
      timeCallbackValue = this.entityForm.get('hourCallback').value * 60 * 60 * 1000
        + this.entityForm.get('minuteCallback').value * 60 * 1000
        + this.entityForm.get('secondsCallback').value * 1000;
    }
    let timeLoop = 0;
    let loopCount = 0;
    const loopOption = this.entityForm.get('loopOption').value;
    if (loopOption) {
      timeLoop = this.entityForm.get('hourLoop').value * 60 * 60 * 1000
        + this.entityForm.get('minuteLoop').value * 60 * 1000
        + this.entityForm.get('secondsLoop').value * 1000;
      loopCount = this.entityForm.get('loopCount').value;
    }

    const cronExpression = this.convertToCron(this.entityForm.get('cronHours').value,
      this.entityForm.get('cronMinutes').value,
      this.entityForm.get('cronSeconds').value);


    const rpcSchedule: RpcSchedule = {
      id: this.rpcScheduleId,
      damTomId: this.dataRpc.damTomId,
      name: this.entityForm.get('name').value,
      rpcSettingId: this.rpcSettingId,
      deviceId: this.entityForm.get('isSelectRpc').value ? this.entityForm.get('deviceId').value : null,
      groupRpcId: this.entityForm.get('isSelectRpc').value ? null : this.entityForm.get('groupRpcId').value,
      valueControl: (this.entityForm.get('valueControl').value ? 1 : 0),
      cron: cronExpression,
      active: this.entityForm.get('active').value,
      callbackOption,
      timeCallback: timeCallbackValue,
      loopOption,
      loopCount,
      loopTimeStep: timeLoop
    };
    console.log(rpcSchedule);
    if (!!this.rpcScheduleId) {
      this.updateRpcSchedule(this.rpcScheduleId, rpcSchedule);
    } else {
      this.saveRpcSchedule(rpcSchedule);
    }
  }

  saveRpcSchedule(rpcSchedule: RpcSchedule) {
    this.rpcScheduleService.saveRpcSchedule(rpcSchedule).pipe(
      tap(data => {
        const mess = 'Thêm mới hẹn giờ điều khiển thành công';
        this.openSnackBarSuccess(mess);
        this.dialogRef.close();
      }),
      finalize(() => {

      }),
      catchError(err => {
        const mess = 'Thêm mới hẹn giờ điều khiển thất bại';
        this.openSnackBarError(mess);
        console.log(err);
        return of({});
      })
    ).subscribe();
  }

  updateRpcSchedule(rpcScheduleId: string, rpcSchedule: RpcSchedule) {
    this.rpcScheduleService.updateRpcSchedule(rpcSchedule, rpcScheduleId).pipe(
      tap(data => {
        const mess = 'Cập nhật hẹn giờ điều khiển thành công';
        this.openSnackBarSuccess(mess);
        this.dialogRef.close();
      }),
      finalize(() => {

      }),
      catchError(err => {
        console.log(err);
        const mess = 'Cập nhật hẹn giờ điều khiển thất bại';
        this.openSnackBarError(mess);
        return of({});
      })
    ).subscribe();
  }

  convertToCron(hoursCron: number, minutesCron: number, secondsCron: number): string {
    const day = [];
    if (this.entityForm.get('cronT2').value) {
      day.push(1);
    }
    if (this.entityForm.get('cronT3').value) {
      day.push(2);
    }
    if (this.entityForm.get('cronT4').value) {
      day.push(3);
    }
    if (this.entityForm.get('cronT5').value) {
      day.push(4);
    }
    if (this.entityForm.get('cronT6').value) {
      day.push(5);
    }
    if (this.entityForm.get('cronT7').value) {
      day.push(6);
    }
    if (this.entityForm.get('cronCN').value) {
      day.push(7);
    }
    return `${secondsCron} ${minutesCron} ${hoursCron} * * ${day}`;
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

  onClickDayOfWeekSchedule(dayOfWeek: any) {
    this.dayOfWeekSchdule[dayOfWeek] = !this.dayOfWeekSchdule[dayOfWeek];
  }

  getTitleAndView() {
    let result = 'Thêm mới hẹn giờ điều khiển';
    if (!this.dataRpc.id && this.dataRpc.action === TableAction.ADD_ENTITY) {
      this.isAddEntity = true;
      return result;
    }

    if (this.dataRpc && this.dataRpc.action === TableAction.EDIT_ENTITY) {
      result = 'Cập nhật hẹn giờ điều khiển ' + this.dataRpc.scheduleName;
      this.isEditEntity = true;
      return result;
    }

    if (this.dataRpc && this.dataRpc.action === TableAction.DETAIL_ENTITY) {
      result = result = 'Chi tiết hẹn giờ điều khiển ' + this.dataRpc.scheduleName;
      this.isDetailsEntity = true;
      const controls = this.entityForm.controls;
      controls.name.disable();
      controls.active.disable();
      controls.isSelectRpc.disable();
      controls.groupRpcId.disable();
      controls.deviceId.disable();
      controls.cronHours.disable();
      controls.cronMinutes.disable();
      controls.cronSeconds.disable();
      controls.valueControl.disable();
      controls.callbackOption.disable();
      controls.hourCallback.disable();
      controls.minuteCallback.disable();
      controls.secondsCallback.disable();
      controls.loopOption.disable();
      controls.hourLoop.disable();
      controls.minuteLoop.disable();
      controls.secondsLoop.disable();
      controls.loopCount.disable();
      return result;
    }
  }

  ngOnDestroy() {
    this.entityForm = null;
    this.isLoading$ = null;
  }

}
