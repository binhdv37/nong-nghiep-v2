import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Subject} from 'rxjs';
import {AlarmDtoV2, CreateAlarmFormData, DeviceProfileAlarm} from '@modules/dft/models/luat-canhbao/luatcb.model';
import {DialogService} from '@core/services/dialog.service';
import {LuatService} from '@modules/dft/service/luat.service';
import {ToastrService} from 'ngx-toastr';

export class EditAlarmDialogData {
  damtomId: string;
  alarmType: string;
  key: string;
  alarmId: string;
}

const DAMTOM_DOES_NOT_EXIST = 'Damtom does not exist';
const ALARM_RULE_NAME_ALREADY_EXIST = 'Alarm rule name already exist';
const ALARM_RULE_DOES_NOT_EXIST = 'Alarm rule does not exist';
const ALARM_RULE_NAME_CANNOT_BE_NULL = 'Alarm rule name can not be null';


@Component({
  selector: 'tb-edit-alarm-dialog',
  templateUrl: './edit-alarm-dialog.component.html',
  styleUrls: ['./edit-alarm-dialog.component.scss']
})
export class EditAlarmDialogComponent implements OnInit {
  isLoading$: Subject<boolean>;

  //  output form data
  outputFormData: CreateAlarmFormData = {
    alarmType: '',
    nguongTren: '',
    nguongDuoi: '',
    hour: 0,
    minute: 0,
    second: 0,
    viaNotification: true,
    viaSms: false,
    viaEmail: false,
    active: true,
    valid: false
  };

  constructor(
    private luatService: LuatService,
    private toastrService: ToastrService,
    private dialogService: DialogService,
    public dialogRef: MatDialogRef<EditAlarmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public dialogData: EditAlarmDialogData
  ) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
  }

  onSubmit(){
    if (this.outputFormData.valid === false){
      this.dialogService.alert('', 'Thông tin nhập liêu không hợp lệ', 'ok', false);
      return;
    }
    this.saveAlarm(this.outputFormData);
  }

  saveAlarm(formData: CreateAlarmFormData){
    this.isLoading$.next(true);
    const alarmDtoV2: AlarmDtoV2 = this.formDataToAlarmDtoV2(this.dialogData.damtomId, this.outputFormData);
    this.luatService.saveAlarmV2(alarmDtoV2).subscribe(
      resp => {
        this.toastrService.success('Cập nhật thành công', '', {
          positionClass: 'toast-bottom-right',
          timeOut: 3000,
        });
        this.dialogRef.close();
      },
      error => {
        console.log(error);
        if (error.status === 400 && error.error.message === DAMTOM_DOES_NOT_EXIST){
          this.dialogService.alert('', 'Nhà vườn không tồn tại', 'ok', false);
        } else if (error.status === 400 && error.error.message === ALARM_RULE_NAME_CANNOT_BE_NULL){
          this.dialogService.alert('', 'Tên luật cảnh báo không được bỏ trống', 'ok', false);
        } else if (error.status === 400 && error.error.message === ALARM_RULE_DOES_NOT_EXIST){
          this.dialogService.alert('', 'Luật cảnh báo không tồn tại', 'ok', false);
        } else if (error.status === 400 && error.error.message === ALARM_RULE_NAME_ALREADY_EXIST){
          this.dialogService.alert('', 'Tên luật cảnh báo đã tồn tại', 'ok', false);
        }
        else{
          this.dialogService.alert('', 'Lỗi không xác định', 'ok', false);
        }
      },
      () => {
        this.isLoading$.next(false);
      }
    );
  }

  formDataToAlarmDtoV2(damtomId: string, formData: CreateAlarmFormData): AlarmDtoV2 {
    const alarm: DeviceProfileAlarm = {
      id: this.dialogData.alarmId,
      alarmType: formData.alarmType,
      createRules: {
        CRITICAL: {
          condition: {
            condition: [
              {
                key: {
                  type: 'TIME_SERIES',
                  key: this.dialogData.key
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
    return  {
      damtomId,
      deviceProfileAlarm: alarm
    };
  }

  getPredicate(formData: CreateAlarmFormData){
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

  getSpec(formData: CreateAlarmFormData){
    if (formData.hour === 0 && formData.minute === 0 && formData.second === 0){
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

  keyToLabel(key: string){
    switch (key) {
      case 'DO':
        return 'DO';
      case 'Salinity':
        return 'Độ mặn';
      case 'pH':
        return 'pH';
      default:
        return 'Nhiệt độ';
    }
  }

  updateOutputFormData(formData: CreateAlarmFormData){
    this.outputFormData = formData;
  }

}
