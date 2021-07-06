import {Component, Inject, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Subject} from 'rxjs';
import {
  DamtomDto,
  DeviceDto,
  DialogAction,
  DieuKhien,
  KieuDuLieuSuKien,
  SuKien,
  TypeRpc
} from '@modules/dft/models/rpc/rpc-auto.model';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {CreateSukienDialogComponent} from '@modules/dft/pages/damtom/damtom-rpc-auto/create-rpc-auto-dialog/create-sukien-dialog/create-sukien-dialog.component';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {catchError, finalize, tap} from 'rxjs/operators';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';
import {GroupRpc} from '@modules/dft/models/rpc/group-rpc.model';
import {TelemetryKey} from '@modules/dft/models/alarm-history/telemetryKey.constant';
import {CreateDieukhienDialogComponent} from '@modules/dft/pages/damtom/damtom-rpc-auto/create-rpc-auto-dialog/create-dieukhien-dialog/create-dieukhien-dialog.component';
import {RpcSettingService} from '@modules/dft/service/rpc/rpc-setting.service';
import {RpcAlarmService} from '@modules/dft/service/rpc/rpc-alarm.service';
import {ToastrService} from 'ngx-toastr';

export class CreateRpcAutoDialogData {
  damtomId: string;
}

@Component({
  selector: 'tb-create-rpc-auto-dialog',
  templateUrl: './create-rpc-auto-dialog.component.html',
  styleUrls: ['./create-rpc-auto-dialog.component.scss']
})
export class CreateRpcAutoDialogComponent implements OnInit {
  isLoading$: Subject<boolean>;

  myForm: FormGroup;

  damtom: DamtomDto;

  listDevice: DeviceDto[] = []; // all damtom device
  listGroupRpc: GroupRpc[] = []; // all damtom group rpc

  listSuKien: SuKien[] = [];
  listDieuKhien: DieuKhien[] = [];

  telemetry = [TelemetryKey.TEMPERATURE, TelemetryKey.SALINITY, TelemetryKey.PH, TelemetryKey.DO];

  telemetryMap = [
    {key: 'DO', display: 'DO (mg/l)'},
    {key: 'pH', display: 'pH'},
    {key: 'Salinity', display: 'Độ mặn (‰)'},
    {key: 'Temperature', display: 'Nhiệt độ (°C)'}
  ];

  constructor(
    private toastrService: ToastrService,
    private rpcAlarmService: RpcAlarmService,
    private rpcSettingService: RpcSettingService,
    private deviceRpcService: DeviceRpcService,
    private damtomService: DamTomService,
    private fb: FormBuilder,
    public dialog: MatDialog,
    public dialogRef: MatDialogRef<CreateRpcAutoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public dialogData: CreateRpcAutoDialogData
  ) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
    this.initForm();
    this.getAllDamtomData();
  }

  initForm() {
    this.myForm = this.fb.group({
      alarmType: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      operation: ['AND'],
      dkBoSung: this.fb.array([]),
      duration: this.fb.group({
        hour: [0],
        minute: [0],
        second: [0]
      }),
      startTime: this.fb.group({
        hour: [0],
        minute: [0]
      }),
      endTime: this.fb.group({
        hour: [0],
        minute: [0]
      }),
      daysOfWeek: this.fb.group({
        t2: [true],
        t3: [true],
        t4: [true],
        t5: [true],
        t6: [true],
        t7: [true],
        cn: [true]
      }),
      active: [true]
    });

    this.myForm.get('operation').valueChanges.subscribe((value) => {
      // remove listSuKien
      this.listSuKien = [];
    });
  }

  get dkBoSung() {
    return this.myForm.controls.dkBoSung as FormArray;
  }

  addDkBoSung() {
    const dkBoSungForm = this.fb.group({
      thietBi: [''],
      active: true
    });
    // thietBi: ten thiet bi

    this.dkBoSung.push(dkBoSungForm);
  }

  deleteDkBoSung(index: number) {
    this.dkBoSung.removeAt(index);
  }

  async getAllDamtomData() {
    this.listDevice = [];
    this.listGroupRpc = [];

    // get damtom:
    await this.getDamtom(this.dialogData.damtomId);

    if (this.damtom === null || this.damtom === undefined) {
      return;
    }
    console.log(this.damtom);

    // get all damtom device :
    for (const gateway of this.damtom.gateways) {
      await this.getGatewayInfo(gateway.id);
    }

    // get telemetry of device :
    for (const device of this.listDevice) {
      const resp = await this.damtomService.getDeviceTelemetryType(device.id).toPromise();
      device.telemetry = resp;
    }

    // get all damtom group rpc :
    this.getDamtomGroupRpc();
  }

  getDamtom(damtomId: string) {
    this.isLoading$.next(true);
    return this.damtomService.getDamtomDto(damtomId).pipe(
      tap(resp => {
        this.damtom = resp;
      }),
      catchError(err => {
        this.isLoading$.next(false);
        console.log(err);
        return null;
      }),
      finalize(() => {
        this.isLoading$.next(false);
      })
    ).toPromise();
  }

  getGatewayInfo(gatewayId: string) {
    this.isLoading$.next(true);
    return this.damtomService.getGateWayInfo(gatewayId).pipe(
      tap(resp => {
        console.log('gateway resp : ', resp);
        resp.listDevices.forEach((x) => {
          x.gatewayId = resp.gateway?.id;
          this.listDevice.push(x);
        });
      }),
      catchError(err => {
        this.isLoading$.next(false);
        console.log(err);
        return null;
      }),
      finalize(() => {
        this.isLoading$.next(false);
      })
    ).toPromise();
  }

  getDamtomGroupRpc() {
    this.isLoading$.next(true);
    this.deviceRpcService.getAllGroupRpc(this.dialogData.damtomId).pipe(
      tap(resp => {
        console.log('group rpc list : ', resp);
        this.listGroupRpc = resp;
      }),
      catchError(err => {
        this.isLoading$.next(false);
        console.log(err);
        return null;
      }),
      finalize(() => {
        this.isLoading$.next(false);
      })
    ).subscribe();
  }

  getRpcDevice() {
    return this.listDevice
      .filter((device) => {
        let match = false;
        device.telemetry.forEach((key) => {
          this.telemetry.forEach((telemetry) => {
            if (telemetry === key) {
              match = true;
            }
          });
        });
        return !match;
      });
  }

  getSensorDevice() {
    return this.listDevice
      .filter((device) => {
        let match = false;
        device.telemetry.forEach((key) => {
          this.telemetry.forEach((telemetry) => {
            if (telemetry === key) {
              match = true;
            }
          });
        });
        return match;
      });
  }

  onSubmitForm() {
    console.log('submit create rpc auto form data :');
    console.log(this.myForm.value);
    this.saveRpcAuto();
  }

  async saveRpcAuto() {
    const req = this.getDefaultReqBody();

    const formValue = this.myForm.value;

    // set damtom id :
    req.damtomId = this.dialogData.damtomId;

    // set ten dk tu dong:
    req.deviceProfileAlarm.alarmType = formValue.alarmType;

    // loop su kien, set data to object
    if (formValue.operation === 'AND') {
      for (const suKien of this.listSuKien) {
        req.deviceProfileAlarm.createRules.MAJOR.condition.condition.push({
          key: {
            key: suKien.duLieuCamBien,
            type: 'TIME_SERIES'
          },
          valueType: 'NUMERIC',
          predicate: {
            type: 'NUMERIC',
            operation: suKien.toanTu,
            value: {
              defaultValue: Number(suKien.nguongGiaTri)
            }
          }
        });

        if (suKien.kieuDuLieu === KieuDuLieuSuKien.CU_THE) {
          req.deviceProfileAlarm.dftAlarmRule.gatewayIds.push(suKien.gatewayId);
        }
      }
    } else {
      // or: ( cung 1 key )
      const condition = {
        key: {
          type: 'TIME_SERIES',
          key: this.listSuKien[0].duLieuCamBien
        },
        valueType: 'NUMERIC',
        predicate: {
          type: 'COMPLEX',
          operation: 'OR',
          predicates: []
        }
      };

      // set predicate:
      for (const suKien of this.listSuKien) {
        const predicate = {
          type: 'NUMERIC',
          operation: suKien.toanTu,
          value: {
            defaultValue: suKien.nguongGiaTri
          }
        };

        condition.predicate.predicates.push(predicate);

        if (suKien.kieuDuLieu === KieuDuLieuSuKien.CU_THE) {
          req.deviceProfileAlarm.dftAlarmRule.gatewayIds.push(suKien.gatewayId);
        }
      }
      req.deviceProfileAlarm.createRules.MAJOR.condition.condition.push(condition);
    }

    // them dieu kien bo sung:
    for (const dkBoSung of formValue.dkBoSung) {
      if (dkBoSung.thietBi !== '') {
        req.deviceProfileAlarm.createRules.MAJOR.condition.condition.push({
          key: {
            type: 'TIME_SERIES',
            key: dkBoSung.thietBi
          },
          valueType: 'NUMERIC',
          predicate: {
            type: 'NUMERIC',
            operation: 'EQUAL',
            value: {
              defaultValue: dkBoSung.active ? 1 : 0
            }
          }
        });
      }
    }

    // set spec dựa vào khoảng th gian sk xảy ra:
    const duration = this.durationToSecond(formValue.duration);
    if (duration === 0) {
      req.deviceProfileAlarm.createRules.MAJOR.condition.spec = {
        type: 'SIMPLE'
      };
    } else {
      req.deviceProfileAlarm.createRules.MAJOR.condition.spec = {
        type: 'DURATION',
        unit: 'SECONDS',
        value: duration
      };
    }

    // set schedule:
    // get list day of week:
    const listDayOfWeek = []; // list 1, 2, 3,...
    const daysOfWeek = formValue.daysOfWeek;
    for (const property in daysOfWeek) {
      if (property === 't2' && daysOfWeek[property] === true) {
        listDayOfWeek.push(1);
      }
      if (property === 't3' && daysOfWeek[property] === true) {
        listDayOfWeek.push(2);
      }
      if (property === 't4' && daysOfWeek[property] === true) {
        listDayOfWeek.push(3);
      }
      if (property === 't5' && daysOfWeek[property] === true) {
        listDayOfWeek.push(4);
      }
      if (property === 't6' && daysOfWeek[property] === true) {
        listDayOfWeek.push(5);
      }
      if (property === 't7' && daysOfWeek[property] === true) {
        listDayOfWeek.push(6);
      }
      if (property === 'cn' && daysOfWeek[property] === true) {
        listDayOfWeek.push(7);
      }
    }

    if (listDayOfWeek.length > 0) {
      req.deviceProfileAlarm.createRules.MAJOR.schedule = {
        type: 'SPECIFIC_TIME',
        timezone: 'Asia/Bangkok',
        daysOfWeek: listDayOfWeek,
        startsOn: this.hmToMillis(formValue.startTime),
        endsOn: this.hmToMillis(formValue.endTime)
      };
    }

    // add thong tin dieu khien:
    for (const dieuKhien of this.listDieuKhien) {
      if (dieuKhien.typeRpc === TypeRpc.GROUP_RPC) {
        req.deviceProfileAlarm.dftAlarmRule.groupRpcIds.push(dieuKhien.groupRpcId);
        break;
      }

      // th list dieu khien:
      dieuKhien.deviceName = this.getDeviceNameById(dieuKhien.deviceId);

      console.log('save rpc settting... ');
      this.isLoading$.next(true);
      let rpcSetting: any;
      await this.rpcSettingService.saveRpcSetting(dieuKhien).pipe(
        tap(resp => {
          rpcSetting = resp;
        }),
        catchError(err => {
          console.log(err);
          this.isLoading$.next(false);
          return null;
        }),
        finalize(() => {
          this.isLoading$.next(false);
        })
      ).toPromise();
      req.deviceProfileAlarm.dftAlarmRule.rpcSettingIds.push(rpcSetting.id);
    }

    // add active :
    req.deviceProfileAlarm.dftAlarmRule.active = formValue.active;

    // save :
    console.log('request body', req);
    this.isLoading$.next(true);
    this.rpcAlarmService.saveRpcAlarm(req).subscribe(resp => {
        this.toastrService.success('Thêm mới thành công!', '', {
          positionClass: 'toast-bottom-right',
          timeOut: 3000,
        });
        this.dialogRef.close();
      },
      err => {
        console.log(err);
        this.isLoading$.next(false);
      },
      () => {
        this.isLoading$.next(false);
      });
  }

  durationToSecond(duration: any) {
    const hour = duration.hour;
    const minute = duration.minute;
    const second = duration.second;
    return hour * 3600 + minute * 60 + second;
  }

  /*
    - time : {hour: number, minute: number}
   */
  hmToMillis(time: any){
    return time.hour * 60 * 60 * 1000 + time.minute * 60 * 1000;
  }


  getDefaultReqBody() {
    return {
      damtomId: '',
      deviceProfileAlarm: {
        alarmType: '',
        createRules: {
          MAJOR: {
            condition: {
              condition: [],
              spec: {}
            },
            schedule: null
          }
        },
        propagate: false,
        dftAlarmRule: {
          rpcAlarm: true,
          active: false,
          viaEmail: false,
          viaSms: false,
          viaNotification: false,
          gatewayIds: [],
          groupRpcIds: [],
          rpcSettingIds: []
        }
      }
    };
  }


  isFormValid() {
    if (this.myForm.controls.alarmType.value.trim() === '') {
      return false;
    }
    if (this.listSuKien.length === 0 || this.listDieuKhien.length === 0) {
      return false;
    }
    return true;
  }

  allowAddDieuKhien(){
    return !(this.listDieuKhien.length > 0 && this.listDieuKhien[0].typeRpc === TypeRpc.GROUP_RPC);
  }

  allowAddGroupRpc(action: DialogAction){
    if (action === DialogAction.CREATE){
      return !(this.listDieuKhien.length > 0 && this.listDieuKhien[0].typeRpc === TypeRpc.RPC);
    } else {
      return !(this.listDieuKhien.length > 1 && this.listDieuKhien[0].typeRpc === TypeRpc.RPC);
    }
  }

  getTenSuKien(suKien: SuKien) {
    if (suKien.kieuDuLieu === KieuDuLieuSuKien.BAT_KY) {
      return `${this.telemetryMap.find(x => x.key === suKien.duLieuCamBien).display}`;
    } else {
      return `${this.telemetryMap.find(x => x.key === suKien.duLieuCamBien).display}`
        + ` của ${this.getDeviceNameById(suKien.camBien)}`;
    }
  }

  getDeviceNameById(id: string) {
    return this.listDevice.find(x => x.id === id).name;
  }

  getGroupRpcNameById(id: string) {
    return this.listGroupRpc.find(x => x.groupRpcId === id).name;
  }

  getDisplayKey(action: DialogAction){
    let key = null;
    if (action === DialogAction.CREATE
      && this.myForm.controls.operation.value === 'OR'
      && this.listSuKien.length > 0){
        // find key
        key = this.listSuKien[0].duLieuCamBien;
    }
    if (action === DialogAction.EDIT
      && this.myForm.controls.operation.value === 'OR'
      && this.listSuKien.length > 1){
      // find key
      key = this.listSuKien[0].duLieuCamBien;
    }
    return key;
  }

  openCreateSuKienDialog() {
    const dialogRef = this.dialog.open(CreateSukienDialogComponent, {
      data: {
        sensorDevices: this.getSensorDevice(),
        displayKey: this.getDisplayKey(DialogAction.CREATE),
        action: DialogAction.CREATE
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      // if (result !== null && result !== undefined && result !== '') {
      if (typeof (result) === 'object') {
        // update suKien array
        this.listSuKien.push(result);
        console.log('current listSuKien: ', this.listSuKien);
      }
    });
  }

  openEditSuKienDialog(index: number, suKien: SuKien) {
    const dialogRef = this.dialog.open(CreateSukienDialogComponent, {
      data: {
        suKien,
        sensorDevices: this.getSensorDevice(),
        displayKey: this.getDisplayKey(DialogAction.EDIT),
        action: DialogAction.EDIT
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (typeof (result) === 'object') {
        // update suKien array
        this.listSuKien[index] = result;
        console.log('current listSuKien: ', this.listSuKien);
      }
    });
  }

  deleteSuKien(index: number) {
    this.listSuKien.splice(index, 1);
  }

  openCreateDieuKhienDialog() {
    const dialogRef = this.dialog.open(CreateDieukhienDialogComponent, {
      data: {
        groupRpcList: this.listGroupRpc,
        rpcDeviceList: this.getRpcDevice(),
        allowAddGroupRpc: this.allowAddGroupRpc(DialogAction.CREATE),
        action: DialogAction.CREATE
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (typeof (result) === 'object') {
        // update suKien array
        this.listDieuKhien.push(result);
        console.log('list dk : ', this.listDieuKhien);
      }
    });
  }

  openEditDieuKhienDialog(index: number, dieuKhien: DieuKhien) {
    const dialogRef = this.dialog.open(CreateDieukhienDialogComponent, {
      data: {
        dieuKhien,
        groupRpcList: this.listGroupRpc,
        rpcDeviceList: this.getRpcDevice(),
        allowAddGroupRpc: this.allowAddGroupRpc(DialogAction.EDIT),
        action: DialogAction.EDIT
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (typeof (result) === 'object') {
        // update suKien array
        this.listDieuKhien[index] = result;
        console.log('current list dk: ', this.listSuKien);
      }
    });
  }

  deleteDieuKhien(index: number) {
    this.listDieuKhien.splice(index, 1);
  }


}
