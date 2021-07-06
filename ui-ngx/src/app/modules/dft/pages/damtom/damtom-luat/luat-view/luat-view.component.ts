import {Component, Inject, OnInit} from '@angular/core';
import {LuatService} from '@modules/dft/service/luat.service';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {DialogData} from '@modules/dft/pages/users-dft/edit-users-dft.component';
import {
  AlarmDto,
  AppKeyFilterPredicateOperation,
  AppOperatorAndOr,
  ICondition, IEntityKey, IKeyFilter, IKeyFilterPredicate, Ipredicates,
  IServerity,
  ISpec, IValue,
  Telemetry
} from '@shared/models/alarmDto';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';
import {GroupRpc} from '@modules/dft/models/rpc/group-rpc.model';

@Component({
  selector: 'tb-luat-view',
  templateUrl: './luat-view.component.html',
  styleUrls: ['./luat-view.component.scss']
})
export class LuatViewComponent implements OnInit {

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
  };
  alarmId;
  damtomId;
  alarm;
  tenCanhBao = '';
  listGroupRPC: GroupRpc[] = [];

  constructor(
    private luatService: LuatService,
    private formBuilder: FormBuilder,
    private groupRpcService: DeviceRpcService,
    private dialogRef: MatDialogRef<LuatViewComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {
  }

  ngOnInit(): void {
    this.alarmId = this.data.id;
    this.damtomId = this.data.index;
    this.createForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      operatorAndOr: [AppOperatorAndOr.AND],
      notification: [''],
      sms: [''],
      email: [''],
      thucHienDieuKhien: [''],
      profile_Data: this.formBuilder.array([])
    });
    this.getAllBoDieuKhien();
    this.getdata();
    // tslint:disable-next-line:no-unused-expression
    this.createForm.disable();
  }

  getAllBoDieuKhien() {
    this.groupRpcService.getAllGroupRpc(this.damtomId).subscribe(data => {
      if (data !== null && data !== undefined) {
        this.listGroupRPC = data;
      }
    });
  }

  getdata() {
    this.luatService.getAlarmEdit(this.data.id).subscribe(rs => {
      console.log('rs', rs);
      this.deviceProfile = rs.deviceProfile.profileData;
      this.tenCanhBao = rs.name;
      this.setValue(rs);
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
    for (const alarm of rs.deviceProfile.profileData.alarms) {
      if (alarm.id === this.alarmId) {
        this.alarm = alarm;
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
      this.getDieuKienArray.disable();
    } else {
      for (const value of this.alarm.createRules.CRITICAL.condition.condition) {
        const dieukien = this.formBuilder.group({
          key: new FormControl(value.key.key, Validators.required),
          defaultValue: new FormControl(value.predicate.value.defaultValue),
          operation: new FormControl(value.predicate.operation),
        });
        this.getDieuKienArray.push(dieukien);
      }
      this.getDieuKienArray.disable();
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


}
