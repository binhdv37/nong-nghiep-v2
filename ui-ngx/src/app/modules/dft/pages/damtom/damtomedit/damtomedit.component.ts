import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {DialogData} from '@modules/dft/pages/users-dft/edit-users-dft.component';
import {SelectionModel} from '@angular/cdk/collections';
import {UsersDft} from '@modules/dft/models/usersDft/users-dft.model';
import {Router} from '@angular/router';
import {DamtomuserComponent} from '@modules/dft/pages/damtom/damtomuser/damtomuser.component';
import {MatTableDataSource} from '@angular/material/table';
import {ToastrService} from 'ngx-toastr';
import {DialogService} from '@core/services/dialog.service';

@Component({
  selector: 'tb-damtomedit',
  templateUrl: './damtomedit.component.html',
  styleUrls: ['./damtomedit.component.scss']
})
export class DamtomeditComponent implements OnInit {
  createForm: FormGroup;
  value;
  staffsArr = new MatTableDataSource<UsersDft>();
  // @ts-ignore
  staffSave: [{}] = [];
  displayedColumns: string[] = ['stt', 'firstName', 'lastName', 'email'];
  selection = new SelectionModel<UsersDft>(true, []);
  submitted = false;

  constructor(
    private damTomService: DamTomService,
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<DamtomeditComponent>,
    private router: Router,
    private toast: ToastrService,
    private dialogService: DialogService,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {
  }

  ngOnInit(): void {
    this.createForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(255), this.noWhitespaceValidator]],
      address: ['', [Validators.maxLength(255)]],
      note: ['', [Validators.maxLength(4000)]],
      active: [true],
      staffs: [''],
      id: []
    });
    this.getData();
  }

  noWhitespaceValidator(control: FormControl) {
    const isWhitespace = (control && control.value && control.value.toString() || '').trim().length === 0;
    const isValid = !isWhitespace;
    return isValid ? null : { whitespace: true };
  }

  getData() {
    this.damTomService.getDamTom(this.data.id).subscribe(rs => {
      console.log(321, rs);
      this.value = rs;
      this.setValue();
      this.staffsArr = this.value.staffs;
    });
  }

  setValue() {
    this.createForm.patchValue({name: this.value.name});
    this.createForm.patchValue({address: this.value.address});
    this.createForm.patchValue({note: this.value.note});
    this.createForm.patchValue({active: this.value.active});
    this.createForm.patchValue({id: this.data.id});
  }

  backToIndex(): void {
    this.submitted = false;
    this.createForm.reset();
    this.router.navigateByUrl('/dam-tom');
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.createForm.valid) {
      if (!!this.staffsArr.data) {
        // @ts-ignore
        for (const nhanvien of this.staffsArr.data) {
          // @ts-ignore
          if (!!nhanvien.staff) {
            // @ts-ignore
            this.staffSave.push(nhanvien.staff.id);
          } else {
            // @ts-ignore
            this.staffSave.push(nhanvien.id.id);
          }
        }
      } else {
        // @ts-ignore
        for (const nhanvien of this.staffsArr) {
          // @ts-ignore
          if (!!nhanvien.staff) {
            // @ts-ignore
            this.staffSave.push(nhanvien.staff.id);
          } else {
            // @ts-ignore
            this.staffSave.push(nhanvien.id.id);
          }
        }
      }
      this.createForm.controls.staffs.setValue(this.staffSave);
      const value = this.createForm.getRawValue();
      this.damTomService.saveDamTom(value).subscribe(rs => {
        console.log(rs);
        // @ts-ignore
        if (rs.message === 1) {
          this.toast.success('Cập nhật thành công!', '', {
            positionClass: 'toast-bottom-right',
            timeOut: 3000,
          });
          this.dialogRef.close();
          this.router.navigateByUrl('/dam-tom');
        }
        // @ts-ignore
        if (rs === 2) {
          this.toast.error('Tên Đầm tôm đã tồn tại, lưu không thành công', '', {
            positionClass: 'toast-bottom-right',
            timeOut: 3000,
          });
        }
      });
    } else {
    }
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(DamtomuserComponent, {
      data: {staffsArr: this.staffsArr}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (!!result) {
        this.staffsArr.data = result;
        this.staffsArr = new MatTableDataSource<UsersDft>(this.staffsArr.data);
        console.log('staff', this.staffsArr.data);
      }
    });
  }

  delete() {
    this.dialogService.confirm(
      'Bạn có chắc chắn không?',
      'Danh sách người dùng quản lý đã chọn sẽ bị xóa khỏi đầm tôm ',
      'Hủy', 'Xóa',
      true
    ).subscribe(rs => {
      if (rs) {
        if (!!this.staffsArr.data) {
          for (const value of this.selection.selected) {
            this.staffsArr.data.splice(this.staffsArr.data.indexOf(value), 1);
          }
          this.staffsArr = new MatTableDataSource<UsersDft>(this.staffsArr.data);
          this.selection = new SelectionModel<UsersDft>(true, []);
        } else {
          for (const value of this.selection.selected) {
            // @ts-ignore
            this.staffsArr.splice(this.staffsArr.indexOf(value), 1);
          }
          // @ts-ignore
          this.staffsArr = new MatTableDataSource<UsersDft>(this.staffsArr);
          this.selection = new SelectionModel<UsersDft>(true, []);
        }
      }
    });

  }

  get rfc() {
    return this.createForm.controls;
  }

  masterToggle() {
    if (this.isAllSelected()) {
      this.selection.clear();
    } else {
      if (!!this.staffsArr.data) {
        this.staffsArr.data.forEach(row => this.selection.select(row));
      } else {
        // @ts-ignore
        this.staffsArr.forEach(row => this.selection.select(row));
      }
    }
  }

  isAllSelected() {
    console.log(258, this.staffsArr);
    const numSelected = this.selection.selected.length;
    if (!!this.staffsArr.data) {
      const numRows = this.staffsArr.data.length;
      return numSelected === numRows;
    } else {
      // @ts-ignore
      const numRows = this.staffsArr.length;
      return numSelected === numRows;
    }

  }

  showValue(element) {
    if (!!element.lastName) {
      return element.lastName;
    }
    if (!!element.staff) {
      if (!!element.staff.lastName) {
        return element.staff.lastName;
      } else {
        return '';
      }
    }
    return '';
  }
}
