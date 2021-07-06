import {TableAction} from './../../../../models/action.model';
import {catchError, finalize, tap} from 'rxjs/operators';
import {TranslateService} from '@ngx-translate/core';
import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {Store} from '@ngrx/store';
import {AppState} from '@app/core/core.state';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {KhachHang} from '@app/modules/dft/models/khachhang.model';
import {Observable, of, Subject} from 'rxjs';
import {KhachHangService} from '@app/modules/dft/service/khachhang/khachhang.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {KhachHangValidators} from '@app/modules/dft/service/khachhang/khachhang.validator';
import {saveAs} from 'file-saver';
import {ToastrService} from 'ngx-toastr';
import {DialogService} from '@app/core/public-api';
import moment from 'moment';
import {DftAdminSettingsService} from '@modules/dft/service/khachhang/camera.service';

export interface DialogData {
  id: string;
  name: string;
  action: string;
}

@Component({
  selector: 'tb-edit-khachhang',
  templateUrl: './edit-khachhang.component.html',
  styleUrls: ['./edit-khachhang.component.scss']
})
export class EditKhachHangComponent implements OnInit, OnDestroy {

  entityForm: FormGroup;
  isEditEntity = false;
  isAddEntity = false;
  isDetailsEntity = false;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  minDate = new Date(1900, 0, 1);
  maxDate = new Date();

  khachHang: KhachHang;
  khachHangId: string;
  titleForm: string;

  files: File[] = [];
  dsTaiLieu: Array<string>[] = [];
  dsTaiLieuRemove: Array<string> = [];
  selectable = true;
  removable = true;

  totalFileSizeUpload = 0;

  constructor(protected store: Store<AppState>,
              protected translate: TranslateService,
              protected fb: FormBuilder,
              protected khachHangService: KhachHangService,
              protected khachHangValidator: KhachHangValidators,
              private cameraService: DftAdminSettingsService,
              private toastrSerive: ToastrService,
              private dialogService: DialogService,
              public dialogRef: MatDialogRef<EditKhachHangComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.buildForm();
    this.titleForm = this.getTitleAndView();
    if (!!this.data.id) {
      this.khachHangId = this.data.id;
    }
    if (!!this.khachHangId && this.khachHangId.length > 0) {
      this.initFormData(this.khachHangId);
    }
  }

  buildForm() {
    this.entityForm = this.fb.group(
      {
        maKhachHang: new FormControl('', [Validators.required, Validators.maxLength(50),
          Validators.pattern('^[a-zA-z 0-9@#\$%\^\&*\(\)+=._]*$')], !!this.data.id ?
          this.khachHangValidator.editMaKhachHangValidator(this.data.id) : this.khachHangValidator.maKhachHangValidator()),
        tenKhachHang: ['', [Validators.required, Validators.maxLength(255)]],
        email: new FormControl('', [Validators.required,
            Validators.pattern('^[a-zA-Z0-9._-]{1,64}@(?=.{2,255}$)(?:[a-z0-9]((?:[a-zA-Z0-9-]*[a-zA-Z0-9])?[.]{1})+[a-zA-Z]{2,})$')],
          !!this.data.id ? null : this.khachHangValidator.emailValidator()),
        soDienThoai: new FormControl('', [Validators.required, Validators.maxLength(45),
            Validators.pattern('^[+]?[0-9]{1,45}$')
          ],
          !!this.data.id ? this.khachHangValidator.editPhoneNumberValidator(this.data.id)
            : this.khachHangValidator.phoneNumberValidator()),
        ngayBatDau: [moment.utc().toDate(), []],
        active: [true, []],
        ghiChu: new FormControl('', [Validators.maxLength(4000)]),
      }
    );
  }

  initFormData(id: string) {
    this.mainSource$.next(true);
    this.khachHangService.getKhachHang(id)
      .pipe(
        tap((data: KhachHang) => {
          this.khachHang = data;
          this.entityForm.patchValue({maKhachHang: data.maKhachHang});
          this.entityForm.patchValue({tenKhachHang: data.tenKhachHang});
          this.entityForm.patchValue({email: data.email});
          this.entityForm.patchValue({soDienThoai: data.soDienThoai});
          this.entityForm.patchValue({
            ngayBatDau: data.ngayBatDau <= 0 ?
              null : moment(new Date(data.ngayBatDau)).toDate()
          });
          this.entityForm.patchValue({active: data.active});
          this.entityForm.patchValue({ghiChu: data.ghiChu});
          this.dsTaiLieu = data.dsTaiLieu;
        }),
        finalize(() => {
          this.mainSource$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          return of({});
        })
      ).subscribe();
  }

  onSubmitForm() {
    this.khachHang = this.entityForm.getRawValue();
    this.khachHang.ngayBatDau = Date.parse(this.entityForm.get('ngayBatDau').value);
    if (!!this.khachHangId && this.khachHangId.length > 0) {
      this.update(this.khachHangId, this.khachHang);
    } else {
      this.save(this.khachHang);
    }
  }

  save(khachHang: KhachHang) {
    this.mainSource$.next(true);
    this.khachHangService.saveKhachHang(khachHang)
      .pipe(
        tap((data: KhachHang) => {
          if (this.files.length > 0 || !!this.files) {
            this.uploadFiles(data.id);
          }
          this.createAccountShinobi(khachHang.email);
          this.dialogRef.close(TableAction.ADD_ENTITY);
        }),
        finalize(() => {
          this.mainSource$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          this.openSnackBar(this.translate.instant('dft.admin.khachhang.create-failed'));
          return of({});
        })
      ).subscribe();
  }

  createAccountShinobi(email: string) {
    this.cameraService.saveShinobiAccount(email)
      .pipe(
        tap(userCam => {
          if (userCam.ok) {
          } else {
            this.mainSource$.next(false);
            this.openSnackBar('Tạo tài khoản camera thất bại');
            return of({});
          }
        }),
        finalize(() => {
          this.mainSource$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          this.openSnackBar('Tạo tài khoản camera thất bại');
          return of({});
        })
      ).subscribe();
  }

  update(id: string, khachHang: KhachHang) {
    this.mainSource$.next(true);
    this.khachHang.id = id;
    this.khachHang.dsTaiLieu = this.dsTaiLieu;
    this.khachHangService.updateKhachHang(khachHang, id)
      .pipe(
        tap((data: KhachHang) => {
          if (this.files.length > 0 || !!this.files) {
            this.uploadFiles(data.id);
            if (this.dsTaiLieuRemove.length > 0) {
              this.removeFiles(data.id);
            }
          }
          this.dialogRef.close(TableAction.EDIT_ENTITY);
        }),
        finalize(() => {
          this.mainSource$.next(false);
        }),
        catchError((error) => {
          this.openSnackBar(this.translate.instant('dft.admin.khachhang.update-failed'));
          console.log(error);
          return of({});
        })
      ).subscribe();
  }

  uploadFiles(id: string) {
    this.mainSource$.next(true);
    const formData = new FormData();
    Array.from(this.files).forEach(file => {
      formData.append('taiLieu', file);
    });

    this.khachHangService.uploadTaiLieu(id, formData)
      .pipe(
        tap((data: KhachHang) => {
          this.files = [];
        }),
        finalize(() => {
          this.dsTaiLieu = [];
          this.dsTaiLieuRemove = [];
          this.totalFileSizeUpload = 0;
          this.mainSource$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          return of({});
        })
        // tslint:disable-next-line: deprecation
      ).subscribe();
  }

  removeFiles(id: string) {
    this.mainSource$.next(true);
    const formData = new FormData();

    this.khachHangService.removeTaiLieu(id, this.dsTaiLieuRemove)
      .pipe(
        tap((data: KhachHang) => {
        }),
        finalize(() => {
          this.dsTaiLieuRemove = [];
          this.totalFileSizeUpload = 0;
          this.mainSource$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          this.openSnackBar('Xóa tài liệu thất bại');
          return of({});
        })
      ).subscribe();
  }

  ngOnDestroy(): void {
    this.mainSource$.next(false);
  }

  getTitleAndView() {
    let result = this.translate.instant('dft.admin.khachhang.add');
    if (!this.data.id && this.data.action === TableAction.ADD_ENTITY) {
      this.isAddEntity = true;
      return result;
    }

    if (this.data && this.data.action === TableAction.EDIT_ENTITY) {
      result = this.translate.instant('dft.admin.khachhang.update', {0: this.data.name});
      const controls = this.entityForm.controls;
      controls.email.disable();
      this.isEditEntity = true;
      return result;
    }

    if (this.data && this.data.action === TableAction.DETAIL_ENTITY) {
      result = result = this.translate.instant('dft.admin.khachhang.details', {0: this.data.name});
      this.isDetailsEntity = true;
      const controls = this.entityForm.controls;
      controls.maKhachHang.disable();
      controls.tenKhachHang.disable();
      controls.ghiChu.disable();
      controls.email.disable();
      controls.soDienThoai.disable();
      controls.active.disable();
      controls.ngayBatDau.disable();
      return result;
    }
  }

  onSelectFiles(event) {
    console.log(event);
    event.addedFiles.forEach(file => {
      if (file.size > 10485760) {
        this.dialogService.alert('Dung lượng tệp qua lớn',
          'Kích thước tệp tải lên không được vượt quá 10MB',
          this.translate.instant('dft.admin.khachhang.yes'));
      } else {
        this.totalFileSizeUpload += file.size;
        console.log(this.totalFileSizeUpload);
        if (this.totalFileSizeUpload > 10485760) {
          this.dialogService.alert('Dung lượng tệp qua lớn',
            'Tổng dung lượng một lần tải lên không được vượt quá 10MB',
            this.translate.instant('dft.admin.khachhang.yes'));
        } else {
          this.files.push(file);
          this.dsTaiLieu.push(file.name);
        }
      }
    });
  }

  onRemoveFile(event) {
    console.log(event);
    this.dsTaiLieuRemove.push(event);
    this.files.splice(this.files.indexOf(event), 1);
    this.dsTaiLieu.splice(this.files.indexOf(event.name), 1);
  }


  onRemoveAllFile() {
    this.files = [];
    this.dsTaiLieu = [];
  }

  downloadFile(fileName: string) {
    this.khachHangService
      .downloadTaiLieu(this.khachHangId, fileName)
      .pipe(
        tap((data: Blob) => {
          saveAs(data, fileName);
        }),
        finalize(() => {
          this.mainSource$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          return of({});
        })
      ).subscribe();
  }


  openSnackBar(message: string) {
    this.toastrSerive.error(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }

}
