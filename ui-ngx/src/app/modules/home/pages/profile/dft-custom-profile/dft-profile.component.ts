import {Component, OnInit} from '@angular/core';
import {Authority} from '@shared/models/authority.enum';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {User} from '@shared/models/user.model';
import {environment as env} from '@env/environment';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ActivatedRoute} from '@angular/router';
import {UserService} from '@core/http/user.service';
import {AuthService} from '@core/auth/auth.service';
import {TranslateService} from '@ngx-translate/core';
import {MatDialog} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {ActionAuthUpdateUserDetails} from '@core/auth/auth.actions';
import {ActionSettingsChangeLanguage} from '@core/settings/settings.actions';
import {PageComponent} from '@shared/components/page.component';
import {HasConfirmForm} from '@core/guards/confirm-on-exit.guard';
import {DftChangePasswordDialogComponent} from '@home/pages/profile/dft-custom-profile/dft-change-password-dialog.component';

const NAME_CANT_BE_NULL = 'Full name can not be null';
const PHONE_CANT_BE_NULL = 'Phone number can not be null';
const NAME_CANT_BE_BLANK = 'Full name can not be blank';
const PHONE_IS_INVALID = 'Invalid phone number';
const PHONE_ALREADY_EXIST = 'Phone number already exist';


@Component({
  selector: 'tb-dft-profile',
  templateUrl: './dft-profile.component.html',
  styleUrls: ['./dft-profile.component.scss']
})
export class DftProfileComponent extends PageComponent implements OnInit, HasConfirmForm {

  authorities = Authority;
  profile: FormGroup;
  user: User;
  languageList = env.supportedLangs;

  constructor(protected store: Store<AppState>,
              private route: ActivatedRoute,
              private userService: UserService,
              private authService: AuthService,
              private translate: TranslateService,
              public dialog: MatDialog,
              public dialogService: DialogService,
              public fb: FormBuilder) {
    super(store);
  }

  ngOnInit() {
    this.buildProfileForm();
    this.userLoaded(this.route.snapshot.data.user);
  }

  buildProfileForm() {
    this.profile = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      lastName: ['', [Validators.maxLength(45), Validators.pattern('^\\+?[0-9]{7,44}$')]],
      language: ['']
    });
    this.profile.controls.email.disable();
  }

  save(): void {
    this.user = {...this.user, ...this.profile.value};
    if (!this.user.additionalInfo) {
      this.user.additionalInfo = {};
    }
    this.user.additionalInfo.lang = this.profile.get('language').value;
    this.userService.dftCustomSaveUser(this.user).subscribe(
      (user) => {
        this.userLoaded(user);
        this.store.dispatch(new ActionAuthUpdateUserDetails({ userDetails: {
            additionalInfo: {...user.additionalInfo},
            authority: user.authority,
            createdTime: user.createdTime,
            tenantId: user.tenantId,
            customerId: user.customerId,
            email: user.email,
            firstName: user.firstName,
            id: user.id,
            lastName: user.lastName,
          } }));
        this.store.dispatch(new ActionSettingsChangeLanguage({ userLang: user.additionalInfo.lang }));
      },
      // binhdv - check trùng sdt
      (err) => {
        if (err.status === 400 && err.error === NAME_CANT_BE_NULL){
          console.log(err);
          this.dialogService.alert('', 'Tên không được null', 'Ok', false);
        }
        else if (err.status === 400 && err.error === PHONE_CANT_BE_NULL){
          console.log(err);
          this.dialogService.alert('', 'Số điện thoại không được null', 'Ok', false);
        }
        else if (err.status === 400 && err.error === NAME_CANT_BE_BLANK){
          console.log(err);
          this.dialogService.alert('', 'Tên không được bỏ trống', 'Ok', false);
        }
        else if (err.status === 400 && err.error === PHONE_IS_INVALID){
          console.log(err);
          this.dialogService.alert('', 'Số điện thoại không hợp lệ', 'Ok', false);
        }
        else if (err.status === 400 && err.error === PHONE_ALREADY_EXIST){
          console.log(err);
          this.dialogService.alert('', 'Số điện thoại đã tồn tại', 'Ok', false);
        }
        else{
          console.log(err);
          this.dialogService.alert('', 'Hệ thống gặp sự cố, xin vui lòng thử lại sau!', 'Ok', false);
        }
      }
    );
  }

  changePassword(): void {
    this.dialog.open(DftChangePasswordDialogComponent, {
      disableClose: true,
      panelClass: ['tb-dialog', 'tb-fullscreen-dialog']
    });
  }

  userLoaded(user: User) {
    this.user = user;
    this.profile.reset(user);
    let lang;
    if (user.additionalInfo && user.additionalInfo.lang) {
      lang = user.additionalInfo.lang;
    } else {
      lang = this.translate.currentLang;
    }
    this.profile.get('language').setValue(lang);
  }

  confirmForm(): FormGroup {
    return this.profile;
  }

}
