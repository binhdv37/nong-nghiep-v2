import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, ValidationErrors, Validators} from '@angular/forms';
import {CreateAlarmFormData} from '@modules/dft/models/luat-canhbao/luatcb.model';
import {getMinMaxNguong} from '@modules/dft/pages/damtom/damtom-luat/damtom-luat.component';

@Component({
  selector: 'tb-create-alarm-tab',
  templateUrl: './create-alarm-tab.component.html',
  styleUrls: ['./create-alarm-tab.component.scss']
})
export class CreateAlarmTabComponent implements OnInit {

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
    private fb: FormBuilder
  ) {
  }

  ngOnInit(): void {
    this.getMinMaxValue();
    this.initForm();
    this.myForm.valueChanges.subscribe(() => {
      this.formData = this.myForm.value;
      this.formData.key = this.key;
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
    if (!this.myForm.valid){
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

}
