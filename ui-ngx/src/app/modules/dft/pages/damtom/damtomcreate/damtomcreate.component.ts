import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { DamTomService } from '@modules/dft/service/damtom.service';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { DamtomuserComponent } from '@modules/dft/pages/damtom/damtomuser/damtomuser.component';
import { SelectionModel } from '@angular/cdk/collections';
import { UsersDft } from '@modules/dft/models/usersDft/users-dft.model';
import { MatTableDataSource } from '@angular/material/table';
import { DialogData } from '@modules/dft/pages/users-dft/edit-users-dft.component';
import { ToastrService } from 'ngx-toastr';
import { DialogService } from '@core/services/dialog.service';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'tb-damtomcreate',
  templateUrl: './damtomcreate.component.html',
  styleUrls: ['./damtomcreate.component.scss']
})
export class DamtomcreateComponent implements OnInit {
  createForm: FormGroup;
  submitted = false;
  staffsArr = new MatTableDataSource<UsersDft>();
  // @ts-ignore
  staffSave: [{}] = [];
  displayedColumns: string[] = ['stt', 'firstName', 'lastName', 'email'];
  selection = new SelectionModel<UsersDft>(true, []);

  constructor(
    private router: Router,
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer,
    private formBuilder: FormBuilder,
    private damTomService: DamTomService,
    private toast: ToastrService,
    private dialogService: DialogService,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private dialogRef: MatDialogRef<DamtomcreateComponent>) {
      this.matIconRegistry.addSvgIcon(
        'House',
        this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/House.svg')
      );
  }

  ngOnInit(): void {
    this.createForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(255), this.noWhitespaceValidator]],
      address: ['', [Validators.maxLength(255)]],
      note: ['', [Validators.maxLength(4000)]],
      active: [false],
      staffs: [''],
    });
  }

  noWhitespaceValidator(control: FormControl) {
    const isWhitespace = (control && control.value && control.value.toString() || '').trim().length === 0;
    const isValid = !isWhitespace;
    return isValid ? null : { whitespace: true };
  }

  backToIndex(): void {
    this.submitted = false;
    this.createForm.reset();
    this.router.navigateByUrl('/dam-tom');
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.createForm.valid) {
      console.log('chay vao dung');
      // for (const nhanvien of this.staffsArr.data) {
      //   // @ts-ignore
      //   this.staffSave.push(nhanvien.id.id);
      // }
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
        console.log(typeof rs);
        // @ts-ignore
        if (rs.message === 0) {
          this.toast.success('Thêm mới thành công!', '', {
            positionClass: 'toast-bottom-right',
            timeOut: 3000,
          });
          this.dialogRef.close();
          this.router.navigateByUrl('/dam-tom');
        }
        // @ts-ignore
        if (rs === 2) {
          this.toast.error('Tên nhà vườn đã tồn tại, lưu không thành công', '', {
            positionClass: 'toast-bottom-right',
            timeOut: 3000,
          });
        }
      });


    }
  }

  get rfc() {
    return this.createForm.controls;
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(DamtomuserComponent, {
      data: { staffsArr: this.staffsArr }
    });
    dialogRef.afterClosed().subscribe(result => {
      this.staffsArr.data = result;
    });
  }

  delete() {
    this.dialogService.confirm(
      'Bạn có chắc chắn không?',
      'Danh sách người dùng quản lý đã chọn sẽ bị xóa khỏi nhà vườn ',
      'Hủy', 'Xóa',
      true
    ).subscribe(rs => {
      if (rs) {
        for (const value of this.selection.selected) {
          this.staffsArr.data.splice(this.staffsArr.data.indexOf(value), 1);
        }
        this.staffsArr = new MatTableDataSource<UsersDft>(this.staffsArr.data);
        this.selection = new SelectionModel<UsersDft>(true, []);
      }
    });
  }
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.staffsArr.data.forEach(row => this.selection.select(row));
  }
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.staffsArr.data.length;
    return numSelected === numRows;
  }
}
