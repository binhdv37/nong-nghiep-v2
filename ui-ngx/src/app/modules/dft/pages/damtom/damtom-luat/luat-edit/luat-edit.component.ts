import {Component, Inject, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {
  AlarmDto,
  AppKeyFilterPredicateOperation,
  AppOperatorAndOr,
  ICondition, IEntityKey, IKeyFilter, IKeyFilterPredicate, Ipredicates,
  IServerity,
  ISpec, IValue,
  Telemetry
} from '@shared/models/alarmDto';
import {LuatService} from '@modules/dft/service/luat.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {DialogData} from '@modules/dft/pages/users-dft/edit-users-dft.component';
import {ToastrService} from 'ngx-toastr';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';
import {GroupRpc} from '@modules/dft/models/rpc/group-rpc.model';

@Component({
  selector: 'tb-luat-edit',
  templateUrl: './luat-edit.component.html',
  styleUrls: ['./luat-edit.component.scss']
})
export class LuatEditComponent implements OnInit {
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
  damtomId;
  alarm;
  tenCanhBao = '';
  index;
  listGroupRPC: GroupRpc[] = [];

  constructor(
    private luatService: LuatService,
    private formBuilder: FormBuilder,
    private groupRpcService: DeviceRpcService,
    private toast: ToastrService,
    private dialogRef: MatDialogRef<LuatEditComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {
  }

  ngOnInit(): void {
    this.alarmId = this.data.id;
    this.damtomId = this.data.index;
    this.createForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      operatorAndOr: [AppOperatorAndOr.AND],
      notification: [''],
      sms: [''],
      email: [''],
      thucHienDieuKhien: [''],
      profile_Data: this.formBuilder.array([], Validators.required)
    });
    this.getAllBoDieuKhien();
    this.getdata();

  }

  getdata() {
    this.luatService.getAlarmEdit(this.data.id).subscribe(rs => {
      this.deviceProfile = rs.deviceProfile.profileData;
      this.tenCanhBao = rs.name;
      this.setValue(rs);
    });
  }

  getAllBoDieuKhien() {
    this.groupRpcService.getAllGroupRpc(this.damtomId).subscribe(data => {
      if (data !== null && data !== undefined) {
        this.listGroupRPC = data;
      }
    });
  }

  setValue(rs) {
    this.createForm.patchValue({name: rs.name});
    // this.createForm.patchValue({operatorAndOr: this.value.address});
    this.createForm.patchValue({notification: rs.viaNotification});
    this.createForm.patchValue({sms: rs.viaSms});
    this.createForm.patchValue({email: rs.viaEmail});
    if (!!rs.groupRpcId) {
      this.createForm.patchValue({thucHienDieuKhien: rs.groupRpcId});
    }
    for (let i = 0; i < rs.deviceProfile.profileData.alarms.length; i++) {
      if (rs.deviceProfile.profileData.alarms[i].id === this.alarmId) {
        this.alarm = rs.deviceProfile.profileData.alarms[i];
        this.index = i;
      }
    }

    if (this.alarm.createRules.CRITICAL.condition.condition[0].predicate.operation === 'OR') {
      this.createForm.patchValue({operatorAndOr: AppOperatorAndOr.OR});
    } else {
      this.createForm.patchValue({operatorAndOr: AppOperatorAndOr.AND});
    }
    this.setArrValue();
  }

  setArrValue() {
    if (this.alarm.createRules.CRITICAL.condition.condition[0].predicate.operation === 'OR') {
      const tendlcb = this.alarm.createRules.CRITICAL.condition.condition[0].key.key;
      for (const value of this.alarm.createRules.CRITICAL.condition.condition[0].predicate.predicates) {
        const dieukien = this.formBuilder.group({
          key: new FormControl(tendlcb, Validators.required),
          defaultValue: new FormControl(value.value.defaultValue, Validators.required),
          operation: new FormControl(value.operation),
        });
        this.getDieuKienArray.push(dieukien);
      }
    } else {
      for (const value of this.alarm.createRules.CRITICAL.condition.condition) {
        const dieukien = this.formBuilder.group({
          key: new FormControl(value.key.key, Validators.required),
          defaultValue: new FormControl(value.predicate.value.defaultValue),
          operation: new FormControl(value.predicate.operation),
        });
        this.getDieuKienArray.push(dieukien);
      }
    }

  }

  get getDieuKienArray() {
    return this.createForm.get('profile_Data') as FormArray;
  }

  backToIndex(): void {
    this.submitted = false;
    this.createForm.reset();
    this.dialogRef.close();
  }

  deleteDieuKien(i) {
    this.getDieuKienArray.removeAt(i);
  }

  setOperation(i: number) {
    if (this.createForm.value.operatorAndOr === 2) {
      for (const data of this.getDieuKienArray.controls) {
        // @ts-ignore
        data.controls.key.setValue(this.createForm.value.profile_Data[i].key);
      }
    }
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

  changeOperatorAndOr() {
    this.getDieuKienArray.controls = [];
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.createForm.valid) {
      for (let i = 0; i < this.deviceProfile.alarms.length; i++) {
        if (this.deviceProfile.alarms[i].alarmType === this.createForm.value.name.trim() && i !== this.index) {
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
        damtomId: this.damtomId,
        name: this.createForm.value.name.trim(),
        note: this.createForm.value.note,
        viaSms: this.createForm.value.sms,
        viaEmail: this.createForm.value.email,
        viaNotification: this.createForm.value.notification,
        thucHienDieuKhien: this.createForm.value.thucHienDieuKhien
      };
      this.luatService.saveAlarm(alarmDto).subscribe(rs => {
        this.toast.success('Cập nhật thành công!', '', {
          positionClass: 'toast-bottom-right',
          timeOut: 3000,
        });
        this.dialogRef.close();
      });
    } else {
      this.toast.error('Không được để trống', '', {
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
      this.Alarm.id = this.alarmId;
      this.alarmId = this.Alarm.id;
      this.Alarm.propagateRelationTypes = null;
      this.Alarm.createRules.CRITICAL.schedule = null;
      this.Alarm.createRules.CRITICAL.alarmDetails = null;
      this.Alarm.createRules.CRITICAL.condition.spec.type = 'SIMPLE';
      this.deviceProfile.alarms.splice(this.index, 1, this.Alarm);
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
      this.Alarm.id = this.alarmId;
      this.alarmId = this.Alarm.id;
      this.Alarm.propagateRelationTypes = null;
      this.Alarm.createRules.CRITICAL.schedule = null;
      this.Alarm.createRules.CRITICAL.alarmDetails = null;
      this.Alarm.createRules.CRITICAL.condition.spec.type = 'SIMPLE';
      this.deviceProfile.alarms.splice(this.index, 1, this.Alarm);
    }
  }


}
