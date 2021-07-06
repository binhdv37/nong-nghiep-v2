import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {Router} from '@angular/router';
import {DftAdminSettingsService} from '@modules/dft/service/khachhang/camera.service';
import {Observable, Subject} from 'rxjs';

export interface CameraSetting {
  cameraSever: string;
  apiKey: string;
  signUpPath: string;
  deletePath: string;
}

@Component({
  selector: 'tb-camera-setting',
  templateUrl: './camera-setting.component.html',
  styleUrls: ['./camera-setting.component.scss', './settings-card.scss']
})
export class CameraSettingComponent implements OnInit {

  generalSettings: FormGroup;
  cameraSettings: CameraSetting;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  constructor(protected store: Store<AppState>,
              private router: Router,
              private dftAdminSettingsService: DftAdminSettingsService,
              public fb: FormBuilder) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.buildGeneralServerSettingsForm();
    this.dftAdminSettingsService.getSettingCamera().subscribe(
      (cameraSettings: CameraSetting) => {
        this.generalSettings.patchValue({cameraSever: cameraSettings.cameraSever});
        this.generalSettings.patchValue({apiKey: cameraSettings.apiKey});
        this.generalSettings.patchValue({signUpPath: cameraSettings.signUpPath});
        this.generalSettings.patchValue({deletePath: cameraSettings.deletePath});
      }
    );
  }

  buildGeneralServerSettingsForm() {
    this.generalSettings = this.fb.group({
      cameraSever: ['', [Validators.required]],
      apiKey: ['', [Validators.required]],
      signUpPath: ['', [Validators.required]],
      deletePath: ['', [Validators.required]]
    });
  }

  save(): void {
    this.cameraSettings = {
      cameraSever: this.generalSettings.get('cameraSever').value,
      apiKey: this.generalSettings.get('apiKey').value,
      signUpPath: this.generalSettings.get('signUpPath').value,
      deletePath: this.generalSettings.get('deletePath').value
    };
    this.dftAdminSettingsService.settingCamera(this.cameraSettings).subscribe(
      (cameraSettings) => {
        this.ngOnInit();
      }
    );
  }

  confirmForm(): FormGroup {
    return this.generalSettings;
  }

}
