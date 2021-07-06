import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {GroupRpc} from '@modules/dft/models/rpc/group-rpc.model';
import {DeviceDto, DialogAction, DieuKhien, TypeRpc} from '@modules/dft/models/rpc/rpc-auto.model';

export class CreateDieukhienDialogData{
  dieuKhien?: DieuKhien;
  groupRpcList: GroupRpc[];
  rpcDeviceList: DeviceDto[];
  allowAddGroupRpc: boolean;
  action: DialogAction;
}

@Component({
  selector: 'tb-create-dieukhien-dialog',
  templateUrl: './create-dieukhien-dialog.component.html',
  styleUrls: ['./create-dieukhien-dialog.component.scss']
})
export class CreateDieukhienDialogComponent implements OnInit {

  groupRpcList: GroupRpc[];
  rpcDeviceList: DeviceDto[];

  myForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<CreateDieukhienDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public dialogData: CreateDieukhienDialogData
  ) { }

  ngOnInit(): void {
    this.setInitData();
    this.initForm();
    if (this.dialogData.action === DialogAction.EDIT){
      this.patchFormData(this.dialogData.dieuKhien);
    }
  }

  setInitData(){
    this.groupRpcList = this.dialogData.groupRpcList;
    this.rpcDeviceList = this.dialogData.rpcDeviceList;
  }

  initForm(){
    this.myForm = this.fb.group({
      typeRpc: [this.dialogData.allowAddGroupRpc === false ? TypeRpc.RPC : TypeRpc.GROUP_RPC],
      groupRpcId: [''],
      deviceId: [''],
      valueControl: [true], // bat or tat
      delayTime: this.fb.group({
        hour: [0],
        minute: [0],
        second: [0]
      }),
      callbackOption: [true], // thực hiện trong
      timeCallback: this.fb.group({
        hour: [0],
        minute: [0],
        second: [0]
      }),
      loopOption: [false],
      loopTimeStep: this.fb.group({
        hour: [0],
        minute: [0],
        second: [0]
      }),
      loopCount: [1]
    });
  }

  patchFormData(dieuKhien: DieuKhien){
    this.myForm.patchValue({
      typeRpc: dieuKhien.typeRpc,
      groupRpcId: dieuKhien.groupRpcId,
      deviceId: dieuKhien.deviceId,
      valueControl: dieuKhien.valueControl === 1,
      delayTime: this.millisToHms(dieuKhien.delayTime),
      callbackOption: dieuKhien.callbackOption,
      timeCallback: this.millisToHms(dieuKhien.timeCallback),
      loopOption: dieuKhien.loopOption,
      loopTimeStep: this.millisToHms(dieuKhien.loopTimeStep),
      loopCount: dieuKhien.loopCount
    });
  }

  // millisecond to hour, minute, second
  millisToHms(millis: number){
    const d = millis / 1000; // convert to second
    const h = Math.floor(d / 3600);
    const m = Math.floor(d % 3600 / 60);
    const s = Math.floor(d % 3600 % 60);
    return {
      hour: h,
      minute: m,
      second: s
    };
  }

  isFormValid(){
    if (!this.myForm.dirty){
      return false;
    }
    if (this.myForm.controls.typeRpc.value === TypeRpc.GROUP_RPC){
      return !(this.myForm.controls.groupRpcId.value === '');
    } else {
      return !(this.myForm.controls.deviceId.value === '');
    }
  }

  getDialogTitle(){
    return this.dialogData.action === DialogAction.CREATE
    ? 'Thêm mới điều khiển' : 'Cập nhật điều khiển';
  }

  getCallbackOption(){
    return this.myForm.controls.callbackOption.value;
  }

  getLoopOption(){
    return this.myForm.controls.loopOption.value;
  }

  onSubmit(){
    console.log('create dieu khien form value : ', this.myForm.value);
    const formValue = this.myForm.value;
    const dieuKhien: DieuKhien = {
      typeRpc: formValue.typeRpc,
      groupRpcId: formValue.groupRpcId,
      deviceId: formValue.deviceId,
      valueControl: formValue.valueControl ? 1 : 0, // 1 => on, 0 => off
      delayTime: this.getDelayTime(formValue),
      callbackOption: formValue.callbackOption,
      timeCallback: this.getTimeCallBack(formValue),
      loopOption: formValue.loopOption,
      loopTimeStep: this.getLoopTimeStep(formValue),
      loopCount: formValue.loopCount
    };
    this.dialogRef.close(dieuKhien);
  }

  getDelayTime(formValue: any){
    const hour = formValue.delayTime.hour;
    const minute = formValue.delayTime.minute;
    const second = formValue.delayTime.second;
    return hour * 3600000 + minute * 60000 + second * 1000;
  }

  getTimeCallBack(formValue: any){
    const hour = formValue.timeCallback.hour;
    const minute = formValue.timeCallback.minute;
    const second = formValue.timeCallback.second;
    return hour * 3600000 + minute * 60000 + second * 1000;
  }

  getLoopTimeStep(formValue: any){
  const hour = formValue.loopTimeStep.hour;
  const minute = formValue.loopTimeStep.minute;
  const second = formValue.loopTimeStep.second;
  return hour * 3600000 + minute * 60000 + second * 1000;
}

}
