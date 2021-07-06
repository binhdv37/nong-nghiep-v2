import {Component, Inject, OnInit} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {DeviceRpc} from '@modules/dft/models/rpc/device-rpc.model';
import {TranslateService} from '@ngx-translate/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {DeviceSetting} from '@modules/dft/models/rpc/group-rpc.model';
import {MatCheckboxChange} from '@angular/material/checkbox';



export const timeCallbackValidator: ValidatorFn = (abstractControl: AbstractControl): ValidationErrors | null => {
  const fg = (abstractControl as FormGroup);
  let isValid = false;
  if (fg.controls.callbackOption.value) {
    if (fg.controls.hourCallback.value <= 0
      && fg.controls.minuteCallback.value <= 0
      && fg.controls.secondsCallback.value <= 0) {
      isValid = true;
    }
  }

  if (isValid) { return { valid: true }; }
  return null;
};

export interface DialogData {
  deviceRpcList: DeviceRpc[];
}

@Component({
  selector: 'tb-rpc-setting',
  templateUrl: './rpc-setting.component.html',
  styleUrls: ['./rpc-setting.component.scss']
})
export class RpcSettingComponent implements OnInit {

  entityForm: FormGroup;

  deviceSetting: DeviceSetting;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  constructor(protected translate: TranslateService,
              protected fb: FormBuilder,
              public dialogRef: MatDialogRef<RpcSettingComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.buildForm();
  }

  buildForm() {
    this.entityForm = this.fb.group(
      {
        deviceId: new FormControl('', [Validators.required]),
        valueControl: new FormControl(false, []),
        hoursDelay: [0, [Validators.min(0), Validators.max(24), Validators.required]],
        minutesDelay: [0, [Validators.min(0), Validators.max(60), Validators.required]],
        secondsDelay: [0, [Validators.min(0), Validators.max(60), Validators.required]],
        callbackOption: [false, []],
        hourCallback: [{value: 0, disabled: true}, [Validators.min(0), Validators.max(24), Validators.required]],
        minuteCallback: [{value: 0, disabled: true}, [Validators.min(0), Validators.max(60), Validators.required]],
        secondsCallback: [{value: 0, disabled: true}, [Validators.min(0), Validators.max(60), Validators.required]],
        loopOption: [{value: false, disabled: true}, []],
        hourLoop: [{value: 0, disabled: true},
          [Validators.min(0), Validators.max(24), Validators.required]],
        minuteLoop: [{value: 0, disabled: true},
          [Validators.min(0), Validators.max(60), Validators.required]],
        secondsLoop: [{value: 0, disabled: true},
          [Validators.min(0), Validators.max(60), Validators.required]],
        loopCount: [{value: 1, disabled: true}, [Validators.min(1), Validators.max(20), Validators.required]]
      }, {
        validators: [timeCallbackValidator]
      }
    );
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

  onSubmitForm() {
    this.deviceSetting = new DeviceSetting();
    this.deviceSetting.deviceId = this.entityForm.get('deviceId').value;
    this.data.deviceRpcList.forEach(deviceRpc => {
      if (deviceRpc.deviceId === this.deviceSetting.deviceId) {
        this.deviceSetting.deviceName = deviceRpc.tenThietBi;
        this.deviceSetting.label = deviceRpc.label;
      }
    });

    const delayTime = this.entityForm.get('hoursDelay').value * 60 * 60 * 1000
      + this.entityForm.get('minutesDelay').value * 60 * 1000
      + this.entityForm.get('secondsDelay').value * 1000;

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

    this.deviceSetting.delayTime = delayTime;
    this.deviceSetting.callbackOption = callbackOption;
    this.deviceSetting.timeCallback = timeCallbackValue;
    this.deviceSetting.loopOption = loopOption;
    this.deviceSetting.loopCount = loopCount;
    this.deviceSetting.loopTimeStep = timeLoop;
    this.deviceSetting.valueControl = this.entityForm.get('valueControl').value === true ? 1 : 0;

    this.dialogRef.close(this.deviceSetting);
  }

}
