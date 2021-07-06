import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {DialogData} from '@modules/dft/pages/users-dft/edit-users-dft.component';
import {LuatService} from '@modules/dft/service/luat.service';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {
  AlarmDto,
  AppKeyFilterPredicateOperation,
  AppOperatorAndOr,
  DataAlarm,
  IAlarm, ICondition, IEntityKey,
  IKeyFilter, IKeyFilterPredicate, Ipredicates,
  IServerity, ISpec, IValue,
  Telemetry
} from '@shared/models/alarmDto';
import {ToastrService} from 'ngx-toastr';
import {GroupRpc} from '@modules/dft/models/rpc/group-rpc.model';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';

@Component({
  selector: 'tb-luat-create',
  templateUrl: './luat-create.component.html',
  styleUrls: ['./luat-create.component.scss']
})
export class LuatCreateComponent implements OnInit {

  createForm: FormGroup;
  submitted = false;

  arrOperatorAndOr: any = [
    {id: AppOperatorAndOr.AND, displayName: 'Và'},
    {id: AppOperatorAndOr.OR, displayName: 'Hoặc'}
  ];
  numberArr: any[] = [
    {id: AppKeyFilterPredicateOperation.EQUAL, displayName: '='},
    {id: AppKeyFilterPredicateOperation.NOT_EQUAL, displayName: '!='},
    {id: AppKeyFilterPredicateOperation.GREATER, displayName: '>'},
    {id: AppKeyFilterPredicateOperation.LESS, displayName: '<'},
    {id: AppKeyFilterPredicateOperation.GREATER_OR_EQUAL, displayName: '>='},
    {id: AppKeyFilterPredicateOperation.LESS_OR_EQUAL, displayName: '<='}
  ];
  telemitryArr: any[] = [
    {id: Telemetry.TEMPERATURE, displayName: 'Nhiệt độ'},
    {id: Telemetry.DO, displayName: 'Độ Oxy hòa tan'},
    {id: Telemetry.ORP, displayName: 'Độ Oxy hóa khử'},
    {id: Telemetry.PH, displayName: 'Độ pH'},
    {id: Telemetry.SALINITY, displayName: 'Độ mặn'},
  ];
  deviceProfile;
  listGroupRPC: GroupRpc[] = [];

  // @ts-ignore
  Alarm = {
    alarmType: '',
    clearRule: null,
    createRules: {
      CRITICAL: new class implements IServerity {
        alarmDetails: null;
        condition: ICondition = new class implements ICondition {
          condition: [] = [];
          // tslint:disable-next-line:new-parens
          spec: ISpec = new class implements ISpec {
            type: 'SIMPLE';
          };
        }();
        schedule: null;
      }()
    },
    id: '',
    propagate: false,
    propagateRelationTypes: null,
  };
  alarmId;


  constructor(
    private luatService: LuatService,
    private groupRpcService: DeviceRpcService,
    private formBuilder: FormBuilder,
    private toast: ToastrService,
    private dialogRef: MatDialogRef<LuatCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {
  }

  ngOnInit(): void {
    this.luatService.getAlarm(this.data.id).subscribe(rs => {
      this.deviceProfile = rs.profileData;
      if (!!rs.profileData.alarms) {
      } else {
        this.deviceProfile.alarms = [];
      }
    });
    this.getAllBoDieuKhien();
    this.createForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      operatorAndOr: [AppOperatorAndOr.AND],
      notification: [true],
      sms: [false],
      email: [false],
      thucHienDieuKhien: [''],
      profile_Data: this.formBuilder.array([], Validators.required)
    });
  }

  createUUID() {
    // http://www.ietf.org/rfc/rfc4122.txt
    const s = [];
    const hexDigits = '0123456789abcdef';
    for (let i = 0; i < 36; i++) {
      s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = '4';  // bits 12-15 of the time_hi_and_version field to 0010
    // tslint:disable-next-line:no-bitwise
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = '-';

    const uuid = s.join('');
    return uuid;
  }

  getAllBoDieuKhien() {

    this.groupRpcService.getAllGroupRpc(this.data.id).subscribe(data => {
      if (data !== null && data !== undefined) {
        this.listGroupRPC = data;
      }
    });
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.createForm.valid) {
      for (const alarm of this.deviceProfile.alarms) {
        if (alarm.alarmType === this.createForm.value.name.trim()) {
          // console.log('chay vao dung');
          this.toast.error('Tên đã tồn tại!', '', {
            positionClass: 'toast-bottom-right',
            timeOut: 3000,
          });
          this.submitted = false;
          return;
        }
      }
      this.getValueForSave();
      const alarmDto: AlarmDto = {
        alarmId: this.alarmId,
        profile_Data: JSON.stringify(this.deviceProfile),
        damtomId: this.data.id,
        name: this.createForm.value.name.trim(),
        note: this.createForm.value.note,
        viaSms: this.createForm.value.sms,
        viaEmail: this.createForm.value.email,
        viaNotification: this.createForm.value.notification,
        thucHienDieuKhien: this.createForm.value.thucHienDieuKhien
      };
      this.luatService.saveAlarm(alarmDto).subscribe(rs => {
        this.toast.success('Thêm mới thành công!', '', {
          positionClass: 'toast-bottom-right',
          timeOut: 3000,
        });
        this.dialogRef.close();
      });
    } else {
      this.toast.error('Không được để trống!', '', {
        positionClass: 'toast-bottom-right',
        timeOut: 3000,
      });
    }
  }

  getValueForSave() {
    if (this.createForm.value.operatorAndOr === 1) {
      // and
      for (const value of this.getDieuKienArray.controls) {
        const keyFilter: IKeyFilter = new class implements IKeyFilter {
          key: IEntityKey = new class implements IEntityKey {
            key: string;
            type: 'TIME_SERIES';
          }();
          predicate: IKeyFilterPredicate = new class implements IKeyFilterPredicate {
            operation: string;
            type: 'NUMERIC';
            value: IValue = new class implements IValue {
              defaultValue: number;
              dynamicValue: null;
              userValue: null;
            }();
          }();
          valueType: 'NUMERIC';
        }();
        keyFilter.key.key = value.value.key;
        keyFilter.key.type = 'TIME_SERIES';
        keyFilter.predicate.type = 'NUMERIC';
        keyFilter.predicate.value.userValue = null;
        keyFilter.predicate.value.defaultValue = value.value.defaultValue.toFixed(2);
        keyFilter.predicate.value.dynamicValue = null;
        keyFilter.predicate.operation = value.value.operation;
        keyFilter.valueType = 'NUMERIC';
        // @ts-ignore
        this.Alarm.createRules.CRITICAL.condition.condition.push(keyFilter);
      }
      this.Alarm.alarmType = this.createForm.value.name;
      this.Alarm.id = this.createUUID();
      this.alarmId = this.Alarm.id;
      this.Alarm.propagateRelationTypes = null;
      this.Alarm.createRules.CRITICAL.schedule = null;
      this.Alarm.createRules.CRITICAL.alarmDetails = null;
      this.Alarm.createRules.CRITICAL.condition.spec.type = 'SIMPLE';
      // @ts-ignore
      this.deviceProfile.alarms.push(this.Alarm);
    } else {
      // or
      const predicate = {
        type: 'COMPLEX',
        operation: 'OR',
        predicates: [],
      };
      for (const value of this.getDieuKienArray.controls) {
        const keyFilterPredicate: Ipredicates = new (class
          implements Ipredicates {
          type: 'NUMERIC';
          operation: string;
          value: IValue = new (class implements IValue {
            defaultValue: any;
            dynamicValue: null;
            userValue: null;
          })();
        })();
        keyFilterPredicate.type = 'NUMERIC';
        keyFilterPredicate.value.userValue = null;
        keyFilterPredicate.value.defaultValue = value.value.defaultValue.toFixed(2);
        keyFilterPredicate.value.dynamicValue = null;
        keyFilterPredicate.operation = value.value.operation;
        predicate.predicates.push(keyFilterPredicate);
      }
      const condition = {
        key: {
          key: '',
          type: 'TIME_SERIES',
        },
        predicate: {},
        valueType: ''
      };
      condition.key.key = this.getDieuKienArray.controls[0].value.key;
      condition.predicate = predicate;
      condition.valueType = 'NUMERIC';
      // @ts-ignore
      this.Alarm.createRules.CRITICAL.condition.condition.push(condition);
      this.Alarm.alarmType = this.createForm.value.name;
      this.Alarm.id = this.createUUID();
      this.alarmId = this.Alarm.id;
      this.Alarm.propagateRelationTypes = null;
      this.Alarm.createRules.CRITICAL.schedule = null;
      this.Alarm.createRules.CRITICAL.alarmDetails = null;
      this.Alarm.createRules.CRITICAL.condition.spec.type = 'SIMPLE';
      // @ts-ignore
      this.deviceProfile.alarms.push(this.Alarm);
    }
  }

  backToIndex(): void {
    this.submitted = false;
    this.createForm.reset();
    this.dialogRef.close();
  }


  changeOperatorAndOr() {
    this.getDieuKienArray.controls = [];
  }

  get getDieuKienArray() {
    return this.createForm.get('profile_Data') as FormArray;
  }

  deleteDieuKien(i) {
    this.getDieuKienArray.removeAt(i);
  }

  addNewCreateRule() {
    const dieukien = this.formBuilder.group({
      key: new FormControl('', Validators.required),
      keyType: new FormControl('TIME_SERIES'),
      predicateType: new FormControl('NUMERIC'),
      userValue: new FormControl(''),
      defaultValue: new FormControl(0),
      dynamicValue: new FormControl(''),
      operation: new FormControl('', Validators.required),
      valueType: new FormControl('NUMERIC'),
    });
    this.getDieuKienArray.push(dieukien);
  }

  setOperation(i: number) {
    if (this.createForm.value.operatorAndOr === 2) {
      for (const data of this.getDieuKienArray.controls) {
        // @ts-ignore
        data.controls.key.setValue(this.createForm.value.profile_Data[i].key);
      }
    }
  }
}
