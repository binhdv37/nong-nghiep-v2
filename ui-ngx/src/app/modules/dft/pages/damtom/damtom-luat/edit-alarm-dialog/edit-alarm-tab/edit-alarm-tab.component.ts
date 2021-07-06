import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {LuatService} from '@modules/dft/service/luat.service';
import {DialogService} from '@core/services/dialog.service';
import {catchError, finalize, map} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {CreateAlarmFormData, DeviceProfileAlarm} from '@modules/dft/models/luat-canhbao/luatcb.model';
import {getMinMaxNguong} from "@modules/dft/pages/damtom/damtom-luat/damtom-luat.component";

@Component({
  selector: 'tb-edit-alarm-tab',
  templateUrl: './edit-alarm-tab.component.html',
  styleUrls: ['./edit-alarm-tab.component.scss']
})
export class EditAlarmTabComponent implements OnInit {
  isLoading$: Subject<boolean>;

  @Input()
  damtomId: string;

  @Input()
  alarmType: string;

  @Input()
  key: string;

  @Output()
  formDataChange = new EventEmitter<any>();

  myForm: FormGroup;

  min;
  max;

  // form data output:
  formData: CreateAlarmFormData;

  constructor(
    private fb: FormBuilder,
    private luatService: LuatService,
    private dialogService: DialogService,
  ) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
    this.getMinMaxValue();
    this.initForm();
    this.getAlarm();
    this.myForm.valueChanges.subscribe(() => {
      this.formData = this.myForm.value;
      this.formData.valid = this.isFormValid();
      this.emitFormData();
    });
  }

  getMinMaxValue(){
    const value = getMinMaxNguong(this.key);
    this.min = value.min;
    this.max = value.max;
  }

  initForm() {
    this.myForm = this.fb.group({
      alarmType: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      nguongTren: ['', [Validators.pattern('^(-)?\\d+(\\.\\d{1})?$'), this.validateRange(this.min, this.max)]],
      nguongDuoi: ['', [Validators.pattern('^(-)?\\d+(\\.\\d{1})?$'), this.validateRange(this.min, this.max)]],
      hour: [0],
      minute: [0],
      second: [0],
      viaNotification: [true],
      viaSms: [false],
      viaEmail: [false],
      active: [true]
    });
  }

  validateRange(min: number, max: number){
    return (control: AbstractControl) => {
      const value = control.value;
      const valueToNumber = Number(value);
      if (value === '' || isNaN(valueToNumber)) {
        return null;
      }
      else if (valueToNumber < min || valueToNumber > max) {
        return {invalidRange: true};
      }
      return null;
    };
  }

  emitFormData() {
    this.formDataChange.emit(this.formData);
  }

  isFormValid() {
    if (this.myForm.pristine) {
      return false;
    }
    if (this.myForm.get('alarmType').value === '') {
      return false;
    }
    if (this.myForm.get('nguongTren').value === '' && this.myForm.get('nguongDuoi').value === '') {
      return false;
    }
    return true;
  }

  getAlarm() {
    this.isLoading$.next(true);
    this.luatService.getAlarmV2(this.damtomId, this.alarmType).pipe(
      map(resp => {
        console.log('getAlarm : ', resp);
        this.updateFormData(resp);
      }),
      catchError((err) => {
        console.log(err);
        if (err.status === 400) {
          this.dialogService.alert('', err.err.message, 'ok', false);
          return null;
        } else {
          this.dialogService.alert('', 'Lỗi không xác định', 'ok', false);
        }
        return null;
      }),
      finalize(() => {
        this.isLoading$.next(false);
      })
    ).subscribe();
  }

  getNguongTrenLabel() {
    switch (this.key) {
      case 'Humidity':
        return 'Độ ẩm (%) >= ';
      case 'Luminosity':
        return 'Ánh sáng (Lux) >= ';
      default :
        return 'Nhiệt độ (°C) >= ';
    }
  }

  getNguongDuoiLabel() {
    switch (this.key) {
      case 'Humidity':
        return 'Độ ẩm (%) <= ';
      case 'Luminosity':
        return 'Ánh sáng (Lux) <= ';
      default :
        return 'Nhiệt độ (°C) <= ';
    }
  }

  updateFormData(alarm: DeviceProfileAlarm) {
    this.myForm.patchValue({
      alarmType: alarm.alarmType,
      nguongTren: this.getNguongTren(alarm),
      nguongDuoi: this.getNguongDuoi(alarm),
      hour: this.getDurationData(alarm).hour,
      minute: this.getDurationData(alarm).minute,
      second: this.getDurationData(alarm).second,
      viaNotification: alarm.dftAlarmRule.viaNotification,
      viaSms: alarm.dftAlarmRule.viaSms,
      viaEmail: alarm.dftAlarmRule.viaEmail,
      active: alarm.dftAlarmRule.active
    });
  }

  getNguongTren(alarm: DeviceProfileAlarm): string {
    let nguongTren = '';
    const predicates: any[] = alarm.createRules.CRITICAL.condition.condition[0].predicate.predicates;
    for (const x of predicates) {
      if (x.operation === 'GREATER_OR_EQUAL') {
        nguongTren = x.value.defaultValue;
      }
    }
    return nguongTren;
  }

  getNguongDuoi(alarm: DeviceProfileAlarm): string {
    let nguongDuoi = '';
    const predicates: any[] = alarm.createRules.CRITICAL.condition.condition[0].predicate.predicates;
    for (const x of predicates) {
      if (x.operation === 'LESS_OR_EQUAL') {
        nguongDuoi = x.value.defaultValue;
      }
    }
    return nguongDuoi;
  }

  getDurationData(alarm: DeviceProfileAlarm) {
    const spec = alarm.createRules.CRITICAL.condition.spec;
    if (spec.type === 'SIMPLE') {
      return {
        hour: 0,
        minute: 0,
        second: 0
      };
    } else {
      return this.getDuration(spec.unit, spec.value);
    }
  }

  getDuration(unit: string, value: number){
    switch (unit) {
      case 'DAYS':
        return this.secondsToHms(value * 24 * 60 * 60);
      case 'HOURS':
        return this.secondsToHms(value * 60 * 60);
      case 'MINUTES':
        return  this.secondsToHms(value * 60);
      default:
        return this.secondsToHms(value);
    }
  }

  secondsToHms(d: number) { // d: seconds
    d = Number(d);
    const h = Math.floor(d / 3600);
    const m = Math.floor(d % 3600 / 60);
    const s = Math.floor(d % 3600 % 60);
    return {
      hour: h,
      minute: m,
      second: s
    };
  }

}
