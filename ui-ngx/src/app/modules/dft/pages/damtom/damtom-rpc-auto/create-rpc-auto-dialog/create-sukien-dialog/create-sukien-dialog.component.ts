import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {DeviceDto, DialogAction, KieuDuLieuSuKien, SuKien} from '@modules/dft/models/rpc/rpc-auto.model';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

export class CreateSukienDialogData {
  sensorDevices: DeviceDto[];
  displayKey?: string;
  suKien?: SuKien;
  action: DialogAction;
}

@Component({
  selector: 'tb-create-sukien-dialog',
  templateUrl: './create-sukien-dialog.component.html',
  styleUrls: ['./create-sukien-dialog.component.scss']
})
export class CreateSukienDialogComponent implements OnInit {
  sensorDevices: DeviceDto[];

  telemetry = [
    {key: 'DO', display: 'DO (mg/l)'},
    {key: 'pH', display: 'pH'},
    {key: 'Salinity', display: 'Độ mặn (‰)'},
    {key: 'Temperature', display: 'Nhiệt độ (°C)'}
  ];

  myForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<CreateSukienDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public dialogData: CreateSukienDialogData
  ) {
  }

  ngOnInit(): void {
    this.sensorDevices = this.dialogData.sensorDevices;
    this.initForm();
    if (this.dialogData.action === DialogAction.EDIT){
      this.patchFormData(this.dialogData.suKien);
    }
  }

  initForm() {
    this.myForm = this.fb.group({
      duLieuCamBien: [this.telemetry[0].key],
      kieuDuLieu: [KieuDuLieuSuKien.BAT_KY],
      camBien: [''],
      toanTu: ['GREATER_OR_EQUAL'],
      nguongGiaTri: [''],
      gatewayId: ['']
    });

    this.myForm.get('camBien').valueChanges.subscribe((value) => {
      const gatewayId = this.sensorDevices?.find(el => el.id === value)?.gatewayId;
      this.myForm.get('gatewayId').patchValue(gatewayId);
    });
  }

  patchFormData(suKien: SuKien){
    this.myForm.patchValue(suKien);
  }

  getSensorDeviceByKey(){
    return this.sensorDevices
      .filter((el) => {
        return el.telemetry.includes(this.myForm.get('duLieuCamBien').value);
      });
  }

  getListTelemetry(){
    if (this.dialogData.displayKey === null){
      return this.telemetry;
    }
    return this.telemetry.filter(x => x.key === this.dialogData.displayKey);
  }

  getDialogTitle(): string{
    return this.dialogData.action === DialogAction.CREATE
    ? 'Thêm mới sự kiện' : 'Cập nhật sự kiện';
  }

  onSubmit(){
    if (!this.isFormValid()) {
      return;
    }
    console.log('create su kien dialog form value : ', this.myForm.value);
    const suKien: SuKien = this.myForm.value;
    this.dialogRef.close(suKien);
  }

  isFormValid(){
    if (!this.myForm.dirty){
      return false;
    }
    if (this.myForm.controls.kieuDuLieu.value === KieuDuLieuSuKien.BAT_KY){
      return this.myForm.controls.nguongGiaTri.value !== '';
    } else {
      return this.myForm.controls.nguongGiaTri.value !== '' && this.myForm.controls.camBien.value !== '';
    }
  }

}
