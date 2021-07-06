import {Component, Inject, OnInit} from '@angular/core';
import {TelemetryKey} from '@modules/dft/models/alarm-history/telemetryKey.constant';
import {
  AlarmDtoV2,
  CheckAlarmNameExistDto,
  CreateAlarmFormData,
  DeviceProfileAlarm
} from '@modules/dft/models/luat-canhbao/luatcb.model';
import {LuatService} from '@modules/dft/service/luat.service';
import {ToastrService} from 'ngx-toastr';
import {DialogService} from '@core/services/dialog.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {forkJoin, Observable, Subject} from 'rxjs';

export class CreateAlarmDialogData {
  damtomId: string;
}

const DAMTOM_DOES_NOT_EXIST = 'Damtom does not exist';
const ALARM_RULE_NAME_ALREADY_EXIST = 'Alarm rule name already exist';
const ALARM_RULE_NAME_CANNOT_BE_NULL = 'Alarm rule name can not be null';


@Component({
  selector: 'tb-create-alarm-dialog',
  templateUrl: './create-alarm-dialog.component.html',
  styleUrls: ['./create-alarm-dialog.component.scss']
})
export class CreateAlarmDialogComponent implements OnInit {
  isLoading$: Subject<boolean>;

  allKey: string[] = [TelemetryKey.TEMPERATURE, TelemetryKey.HUMIDITY, TelemetryKey.LUMINOSITY];

  outputFormData: CreateAlarmFormData[] = [];

  constructor(
    private luatService: LuatService,
    private toastrService: ToastrService,
    private dialogService: DialogService,
    public dialogRef: MatDialogRef<CreateAlarmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public dialogData: CreateAlarmDialogData
  ) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
  }

  outputFormDataValid(): boolean {
    return this.outputFormData.some((element) => {
      return element.valid;
    });
  }

  updateOutputFormData(formData: CreateAlarmFormData) {
    this.outputFormData = this.outputFormData.filter(x => {
      return x.key !== formData.key;
    });
    this.outputFormData.push(formData);
  }

  keyToLabel(key: string) {
    switch (key) {
      case 'Humidity':
        return 'Độ ẩm';
      case 'Luminosity':
        return 'Ánh sáng';
      default:
        return 'Nhiệt độ';
    }
  }

  onSubmit() {
    if (!this.outputFormDataValid()) {
      this.dialogService.alert('', 'Thông tin nhập liệu không hợp lệ', 'ok', false);
      return;
    }
    const validFormDatas: CreateAlarmFormData[] = this.outputFormData.filter(x => {
      return x.valid === true;
    });
    this.saveAlarm(validFormDatas);
  }

  saveAlarm(validFormDatas: CreateAlarmFormData[]) {
    const alarmNameList: string[] = [];
    validFormDatas.forEach(x => {
      alarmNameList.push(x.alarmType);
    });

    // check input name duplicate
    if (this.checkIfDuplicateExist(alarmNameList)) {
      this.dialogService.alert('', 'Tên luật cảnh báo không được giống nhau!', 'ok', false);
      return;
    }
    // check name already exist:
    const checkNameExistDto: CheckAlarmNameExistDto = {
      damtomId: this.dialogData.damtomId,
      alarmTypes: alarmNameList
    };

    this.isLoading$.next(true);
    this.luatService.checkNameExistV2(checkNameExistDto).subscribe(resp => {
          if (resp.exist) {
            this.dialogService.alert('', `Tên luật cảnh báo "${resp.alarmType}" đã tồn tại`, 'ok', false);
          } else {
            this.saveData(validFormDatas, this.dialogData.damtomId);
          }
        },
        error => {
          console.log(error);
          if (error.status === 400 && error.error.message === DAMTOM_DOES_NOT_EXIST) {
            this.dialogService.alert('', 'Nhà vườn không tồn tại', 'ok', false);
          } else {
            this.dialogService.alert('', 'Lỗi không xác định', 'ok', false);
          }
          this.isLoading$.next(false);
        },
        () => {
          this.isLoading$.next(false);
        });
  }

  saveData(formDatas: CreateAlarmFormData[], damtomId: string) {
    const requestList: Observable<any>[] = [];
    for (const x of formDatas) {
      const alarmDtoV2: AlarmDtoV2 = this.formDataToAlarmDtoV2(damtomId, x);
      requestList.push(this.luatService.saveAlarmV2(alarmDtoV2));
    }
    this.isLoading$.next(true);
    forkJoin(requestList).subscribe(() => {
        this.toastrService.success('Thêm mới thành công', '', {
          positionClass: 'toast-bottom-right',
          timeOut: 3000,
        });
        this.dialogRef.close();
      },
      error => {
        console.log(error);
        if (error.status === 400 && error.error.message === DAMTOM_DOES_NOT_EXIST) {
          this.dialogService.alert('', 'Nhà vườn không tồn tại', 'ok', false);
        } else if (error.status === 400 && error.error.message === ALARM_RULE_NAME_CANNOT_BE_NULL) {
          this.dialogService.alert('', 'Tên luật cảnh báo không được bỏ trống', 'ok', false);
        } else if (error.status === 400 && error.error.message === ALARM_RULE_NAME_ALREADY_EXIST) {
          this.dialogService.alert('', 'Tên luật cảnh báo đã tồn tại', 'ok', false);
        } else {
          this.dialogService.alert('', 'Lỗi không xác định', 'ok', false);
        }
        this.isLoading$.next(false);
      },
      () => {
        this.isLoading$.next(false);
      }
    );
  }

  checkIfDuplicateExist(alarmNameList: string[]) {
    return new Set(alarmNameList).size !== alarmNameList.length;
  }

  formDataToAlarmDtoV2(damtomId: string, formData: CreateAlarmFormData): AlarmDtoV2 {
    const alarm: DeviceProfileAlarm = {
      alarmType: formData.alarmType,
      createRules: {
        CRITICAL: {
          condition: {
            condition: [
              {
                key: {
                  type: 'TIME_SERIES',
                  key: formData.key
                },
                valueType: 'NUMERIC',
                predicate: {
                  type: 'COMPLEX',
                  operation: 'OR',
                  predicates: this.getPredicate(formData)
                }
              }
            ],
            spec: this.getSpec(formData)
          }
        }
      },
      propagate: false,
      dftAlarmRule: {
        viaSms: formData.viaSms,
        viaEmail: formData.viaEmail,
        viaNotification: formData.viaNotification,
        rpcAlarm: false,
        active: formData.active
      }
    };
    return {
      damtomId: this.dialogData.damtomId,
      deviceProfileAlarm: alarm
    };
  }

  getPredicate(formData: CreateAlarmFormData) {
    const predicates = [];
    if (formData.nguongTren !== '') {
      predicates.push({
        type: 'NUMERIC',
        operation: 'GREATER_OR_EQUAL',
        value: {
          defaultValue: Number(formData.nguongTren)
        }
      });
    }
    if (formData.nguongDuoi !== '') {
      predicates.push({
        type: 'NUMERIC',
        operation: 'LESS_OR_EQUAL',
        value: {
          defaultValue: Number(formData.nguongDuoi)
        }
      });
    }
    return predicates;
  }

  getSpec(formData: CreateAlarmFormData) {
    if (formData.hour === 0 && formData.minute === 0 && formData.second === 0) {
      return {
        type: 'SIMPLE'
      };
    }
    return {
      type: 'DURATION',
      unit: 'SECONDS',
      value: formData.hour * 3600 + formData.minute * 60 + formData.second
    };
  }


}
