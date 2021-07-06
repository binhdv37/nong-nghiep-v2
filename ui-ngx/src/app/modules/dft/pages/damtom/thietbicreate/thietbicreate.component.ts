import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ToastrService} from 'ngx-toastr';
import {Router} from '@angular/router';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {DialogData} from '@modules/dft/pages/users-dft/edit-users-dft.component';

@Component({
  selector: 'tb-thietbicreate',
  templateUrl: './thietbicreate.component.html',
  styleUrls: ['./thietbicreate.component.scss']
})
export class ThietbicreateComponent implements OnInit {

  createForm: FormGroup;
  submitted = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private damTomService: DamTomService,
    private toast: ToastrService,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private dialogRef: MatDialogRef<ThietbicreateComponent>
  ) {
  }

  ngOnInit(): void {
    this.createForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      note: [''],
      active: [true],
      damtomId: [this.data.id]
    });
  }
  onSubmit(): void {
    this.submitted = true;
    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      console.log('value', value);
      this.damTomService.createDevice(value).subscribe(rs => {
        console.log(123, rs);
        // @ts-ignore
        if (rs === 1) {
          this.toast.error('Tên bộ thiết bị đã tồn tại!', '', {
            positionClass: 'toast-bottom-right',
            timeOut: 3000,
          });
        } else {
          this.toast.success('Thêm mới thành công!', '', {
            positionClass: 'toast-bottom-right',
            timeOut: 3000,
          });
          this.dialogRef.close();
        }
      });
    }
  }

  backToIndex(): void {
    this.submitted = false;
    this.createForm.reset();
    this.dialogRef.close();
  }


}
