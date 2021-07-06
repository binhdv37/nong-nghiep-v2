import {AfterViewInit, Component, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {DialogData} from '@modules/dft/pages/users-dft/edit-users-dft.component';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {ToastrService} from 'ngx-toastr';
import {ChangeLabelDialogComponent} from '@modules/dft/pages/damtom/damtom-thietbi/change-label-dialog/change-label-dialog.component';
import {MatPaginator} from '@angular/material/paginator';
import {MatTableDataSource} from '@angular/material/table';

@Component({
  selector: 'tb-thietbiedit',
  templateUrl: './thietbiedit.component.html',
  styleUrls: ['./thietbiedit.component.scss']
})
export class ThietbieditComponent implements OnInit, AfterViewInit {
  damtomId;
  gateWayId;
  value;

  createForm: FormGroup;
  submitted = false;
  displayedColumnsdt: string[] = [ 'name', 'type', 'edit'];
  dataSource = new MatTableDataSource<any>();

  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(
    private formBuilder: FormBuilder,
    private damTomService: DamTomService,
    private toast: ToastrService,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private dialogRef: MatDialogRef<ThietbieditComponent>
  ) {
  }

  ngOnInit(): void {
    this.createForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      note: [''],
      active: [true],
      damtomId: [''],
      id: ['']
    });
    // @ts-ignore
    this.damtomId = this.data.id.damtomId;
    // @ts-ignore
    this.gateWayId = this.data.id.id;
    this.dataSource.data = [];
    this.damTomService.getGateWay(this.gateWayId).subscribe(rs => {
      this.value = rs;
      this.createForm.patchValue({name: rs.gateway.device.name});
      this.createForm.patchValue({active: rs.gateway.active});
      this.createForm.patchValue({note: rs.gateway.device.additionalInfo.description});
      this.createForm.patchValue({damtomId: this.damtomId});
      this.createForm.patchValue({id: this.gateWayId});
      this.dataSource.data = this.value.listDevices;
    });


  }

  onSubmit(): void {
    this.submitted = true;
    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      this.damTomService.editDevice(value).subscribe(rs => {
        if (rs === 1) {
          this.toast.error('Trùng tên thiết bị!', '', {
            positionClass: 'toast-bottom-right',
            timeOut: 3000,
          });
        } else {
          this.toast.success('Cập nhật thành công!', '', {
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

  cutString(name) {
    if (name.toLowerCase().includes('temperature')) {
      return 'Cảm biến nhiệt độ';
    } else if (name.toLowerCase().includes('ph')) {
      return 'Cảm biến pH';
    } else if (name.toLowerCase().includes('do')) {
      return 'Cảm biến DO';
    } else if (name.toLowerCase().includes('salinity')) {
      return 'Cảm biến độ mặn';
    } else if (name.toLowerCase().includes('rpc')) {
      return 'Thiết bị RPC';
    } else {
      return 'Không xác định';
    }
  }

  openDeviceSettingDialog(deviceId: string, label: string) {
    const dialogRef = this.dialog.open(ChangeLabelDialogComponent, {
      data: {deviceId, label}
    });
    dialogRef.afterClosed().subscribe(result => {
      this.ngOnInit();
    });
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }
}
