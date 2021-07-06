import {AfterViewInit, Component, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {ToastrService} from 'ngx-toastr';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {DialogData} from '@modules/dft/pages/users-dft/edit-users-dft.component';
import {TranslateService} from '@ngx-translate/core';
import {MatTableDataSource} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';

@Component({
  selector: 'tb-thietbiwiew',
  templateUrl: './thietbiwiew.component.html',
  styleUrls: ['./thietbiwiew.component.scss']
})
export class ThietbiwiewComponent implements OnInit, AfterViewInit {

  damtomId;
  gateWayId;
  value;

  createForm: FormGroup;
  submitted = false;
  displayedColumnsdt: string[] = ['name', 'type'];

  dataSource = new MatTableDataSource<any>();

  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(
    private formBuilder: FormBuilder,
    private damTomService: DamTomService,
    protected translate: TranslateService,
    private toast: ToastrService,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private dialogRef: MatDialogRef<ThietbiwiewComponent>
  ) {
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  ngOnInit(): void {
    this.createForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      note: [''],
      active: [true],
      damtomId: [''],
      id: [''],
      credentialsId: ['']
    });
    // @ts-ignore
    this.damtomId = this.data.id.damtomId;
    // @ts-ignore
    this.gateWayId = this.data.id.id;
    this.damTomService.getGateWay(this.gateWayId).subscribe(rs => {
      this.value = rs;
      this.createForm.patchValue({name: rs.gateway.device.name});
      this.createForm.patchValue({active: rs.gateway.active});
      this.createForm.patchValue({note: rs.gateway.device.additionalInfo.description});
      this.createForm.patchValue({damtomId: this.damtomId});
      this.createForm.patchValue({id: this.gateWayId});
      this.createForm.patchValue({credentialsId: rs.credentialsId});
      this.createForm.disable();
      this.dataSource.data = this.value.listDevices;
    });
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      console.log('value', value);
      this.damTomService.editDevice(value).subscribe(error => {
        console.log(123, error);
      });
      this.dialogRef.close();
      // this.router.navigateByUrl('/dam-tom');
      this.toast.success('Thêm mới thành công!', '', {
        positionClass: 'toast-bottom-right',
        timeOut: 3000,
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
}
