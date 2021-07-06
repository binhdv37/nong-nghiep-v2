import {Component, Inject, OnInit} from '@angular/core';
import {Devicelabel} from '@modules/dft/models/devicelabel.model';
import {catchError, finalize, tap} from 'rxjs/operators';
import {Observable, of, Subject} from 'rxjs';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';
import {DftDeviceService} from '@modules/dft/service/device.service';
import {TranslateService} from '@ngx-translate/core';
import {ToastrService} from 'ngx-toastr';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';

export interface ChangeLabelDialogData {
  deviceId: string;
  label: string;
}

@Component({
  selector: 'tb-change-label-dialog',
  templateUrl: './change-label-dialog.component.html',
  styleUrls: ['./change-label-dialog.component.scss']
})
export class ChangeLabelDialogComponent implements OnInit {

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  entityForm: FormGroup;

  constructor(private deviceRpcService: DeviceRpcService,
              private dftDeviceService: DftDeviceService,
              protected translate: TranslateService,
              protected store: Store<AppState>,
              protected fb: FormBuilder,
              private toastService: ToastrService,
              public dialog: MatDialog,
              public dialogRef: MatDialogRef<ChangeLabelDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ChangeLabelDialogData) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.buildForm();
  }

  buildForm() {
    this.entityForm = this.fb.group(
      {
        label: [this.data.label , [Validators.maxLength(255), Validators.required]],
      }
    );
  }

  onSubmitForm() {
    const changeName: Devicelabel = this.entityForm.getRawValue();
    this.changeLableName(this.data.deviceId, changeName);
  }

  changeLableName(deviceId: string, deviceLabel: Devicelabel) {
    this.dftDeviceService.editDeviceName(deviceId, deviceLabel).pipe(
      tap((data) => {
        this.dialogRef.close(deviceLabel.label);
      }),
      finalize(() => {
        this.mainSource$.next(false);
      }),
      catchError((error) => {
        console.log(error);
        const msg = this.translate.instant('Đổi tên thiết bị thất bại');
        this.openSnackBarError(msg);
        return of({});
      })
    ).subscribe();
  }

  openSnackBarError(message: string) {
    this.toastService.error(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }

}
