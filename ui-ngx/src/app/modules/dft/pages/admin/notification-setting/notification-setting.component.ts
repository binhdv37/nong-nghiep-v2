import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {Router} from '@angular/router';
import {Observable, Subject} from 'rxjs';
import { NotificationService } from '@app/modules/dft/service/khachhang/notification.service';

export interface NotificationSetting {
  firebaseApiUrl: string;
  firebaseAccessToken: string;
}

@Component({
  selector: 'tb-notification-setting',
  templateUrl: './notification-setting.component.html',
  styleUrls: ['./notification-setting.component.scss', './settings-card.scss']
})
export class NotificationSettingComponent implements OnInit {

  generalSettings: FormGroup;
  notificationSetting : NotificationSetting;

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  constructor(protected store: Store<AppState>,
              private router: Router,
              private dftAdminSettingsService: NotificationService,
              public fb: FormBuilder) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.buildGeneralServerSettingsForm();
    this.dftAdminSettingsService.getSettingNotification().subscribe(
      (notificationSettings: NotificationSetting) => {
        console.log(notificationSettings);

        this.generalSettings.patchValue({firebaseApiUrl: notificationSettings.firebaseApiUrl});
        this.generalSettings.patchValue({firebaseAccessToken: notificationSettings.firebaseAccessToken});
      }
    );
  }

  buildGeneralServerSettingsForm() {
    this.generalSettings = this.fb.group({
      firebaseApiUrl: ['', [Validators.required]],
      firebaseAccessToken: ['', [Validators.required]]
    });
  }

  save(): void {
    this.notificationSetting = {
      firebaseApiUrl: this.generalSettings.get('firebaseApiUrl').value,
      firebaseAccessToken: this.generalSettings.get('firebaseAccessToken').value
    };


    this.dftAdminSettingsService.settingNotification(this.notificationSetting).subscribe(
      (notificationSettings) => {
        this.ngOnInit();
      }
    );
  }


  confirmForm(): FormGroup {
    return this.generalSettings;
  }

}
