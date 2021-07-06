import {Component, Input, OnInit} from '@angular/core';
import {Subject} from 'rxjs';
import {FormBuilder, FormGroup} from '@angular/forms';
import {LuatService} from '@modules/dft/service/luat.service';
import {DialogService} from '@core/services/dialog.service';
import {catchError, finalize, map} from 'rxjs/operators';
import {DeviceProfileAlarm} from '@modules/dft/models/luat-canhbao/luatcb.model';

@Component({
  selector: 'tb-detail-alarm-tab',
  templateUrl: './detail-alarm-tab.component.html',
  styleUrls: ['./detail-alarm-tab.component.scss']
})
export class DetailAlarmTabComponent implements OnInit {
  isLoading$: Subject<boolean>;

  @Input()
  damtomId: string;

  @Input()
  alarmType: string;

  @Input()
  key: string;

  myForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private luatService: LuatService,
    private dialogService: DialogService,
  ) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
    this.initForm();
    this.getAlarm();
    this.myForm.disable();
  }

  initForm() {
    this.myForm = this.fb.group({
      alarmType: [''],
      nguongTren: [''],
      nguongDuoi: [''],
      hour: [0],
      minute: [0],
      second: [0],
      viaNotification: [true],
      viaSms: [false],
      viaEmail: [false],
      active: [true]
    });
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
        if (err.status === 400){
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

  updateFormData(alarm: DeviceProfileAlarm){
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

  getNguongTrenLabel(){
    switch (this.key) {
      case 'DO':
        return 'DO (mg/l) >= ';
      case 'Salinity':
        return 'Độ mặn (‰) >= ';
      case 'Temperature':
        return 'Nhiệt độ (°C) >= ';
      default :
        return 'pH >= ';
    }
  }

  getNguongDuoiLabel(){
    switch (this.key) {
      case 'DO':
        return 'hoặc DO (mg/l) <= ';
      case 'Salinity':
        return 'hoặc Độ mặn (‰) <= ';
      case 'Temperature':
        return 'hoặc Nhiệt độ (°C) <= ';
      default :
        return 'hoặc pH <= ';
    }
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
