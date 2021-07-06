import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Subject} from 'rxjs';
import {DamtomCamera} from '@modules/dft/models/qlcamera.model';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {TranslateService} from '@ngx-translate/core';
import {DialogService} from '@core/services/dialog.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {CameraService} from '@modules/dft/service/qlcamera/camera.service';
import {catchError, finalize, map, tap} from 'rxjs/operators';
import {TableAction} from '@modules/dft/models/action.model';

export interface EditCameraDialogData {
  damtomId: string;
  id: string;  // id cua camera
  name: string; // ten camera
  action: string; // TableAction.ADD, EDIT, DETAILS,...
}

@Component({
  selector: 'tb-edit-camera',
  templateUrl: './edit-camera.component.html',
  styleUrls: ['./edit-camera.component.scss']
})
export class EditCameraComponent implements OnInit {
  entityForm: FormGroup;
  isEditEntity = false;
  isAddEntity = false;
  isDetailEntity = false;

  isLoading$: Subject<boolean>;

  camera: DamtomCamera;
  cameraId: string;
  titleForm: string;

  constructor(
    protected store: Store<AppState>,
    protected translate: TranslateService,
    protected fb: FormBuilder,
    protected cameraService: CameraService,
    private dialogService: DialogService,
    public dialogRef: MatDialogRef<EditCameraComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EditCameraDialogData
  ) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit() {
    this.buildForm();
    this.titleForm = this.getTitleAndView();
    if (this.data.id) {
      this.cameraId = this.data.id;
    }
    if (this.cameraId && this.cameraId.length > 0) {
      this.initFormData(this.cameraId);
    } else{
      this.camera = {
        damtomId: this.data.damtomId,
        code: '',
        name: '',
        url: '',
        note: '',
        main: false
      };
    }
  }

  buildForm(){
    this.entityForm = this.fb.group({
      code: ['', Validators.compose([Validators.required, Validators.maxLength(50), Validators.pattern('^(?!\\s*$).+')])],
      name: ['', Validators.compose([Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')])],
      url: ['', Validators.compose([Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')])],
      note: ['', [Validators.maxLength(4000)]],
      main: [false]
    });
  }

  initFormData(id: string) {
    this.isLoading$.next(true);
    this.cameraService.getCameraById(id)
      .pipe(
        map((data: DamtomCamera) => {
          this.camera = data;
          this.entityForm.patchValue({ code: data.code });
          this.entityForm.patchValue({ name: data.name });
          this.entityForm.patchValue({ url: data.url });
          this.entityForm.patchValue({ note: data.note });
          this.entityForm.patchValue({ main: data.main });
        }),
        finalize(() => {
          this.isLoading$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          return null;
        })
      ).subscribe();
  }

  onSubmitForm() {
    // take form data
    this.camera = this.entityForm.getRawValue();
    // set damtomId
    this.camera.damtomId = this.data.damtomId;
    // set id
    if (this.cameraId) { this.camera.id = this.cameraId; }
    console.log(this.camera);
    this.save(this.camera);
  }

  save(camera: DamtomCamera) {
    this.isLoading$.next(true);
    this.cameraService.saveCamera(camera)
      .pipe(
        tap((data: DamtomCamera) => {
          if (data.errorDto != null){
            if (data.errorDto.code === 1) { this.dialogService.alert('', this.translate.instant('dft.qlcamera.notify.exist-cam-code'), 'Ok'); }
            else if (data.errorDto.code === 2) { this.dialogService.alert('', this.translate.instant('dft.qlcamera.notify.exist-cam-name'), 'Ok'); }
            else { this.dialogService.alert('', this.translate.instant('dft.qlcamera.notify.exist-cam-url'), 'Ok'); }
          }
          else if (this.cameraId === null || this.cameraId === undefined){
            // Them mới thành công
            console.log('save success');
            this.dialogRef.close(TableAction.ADD_ENTITY);
          } else {
            // cập nhật thành công
            console.log('update success');
            this.dialogRef.close(TableAction.EDIT_ENTITY);
          }
        }),
        finalize(() => {
          this.isLoading$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          return null;
        })
      ).subscribe();
  }

  ngOnDestroy(): void {
    this.isLoading$.next(false);
  }

  getTitleAndView() {
    let result = this.translate.instant('dft.qlcamera.dialog.add-title');
    if (!this.data.id && this.data.action === TableAction.ADD_ENTITY) {
      this.isAddEntity = true;
      return result;
    }

    if (this.data.id && this.data.action === TableAction.EDIT_ENTITY) {
      result = this.translate.instant('dft.qlcamera.dialog.edit-title') + ' ' + this.data.name;
      this.isEditEntity = true;
      return result;
    }

    if (this.data.id && this.data.action === TableAction.DETAIL_ENTITY) {
      result = this.translate.instant('dft.qlcamera.dialog.detail-title') + ' ' + this.data.name;
      this.isDetailEntity = true;
      const controls = this.entityForm.controls;
      this.entityForm.disable();
      return result;
    }
  }
}
