import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {AuthService} from '@core/auth/auth.service';
import {MatDialogRef} from '@angular/material/dialog';
import {DialogComponent} from '@shared/components/dialog.component';
import {DialogService} from '@core/services/dialog.service';

const CURRENT_PASSWORD_DOES_NOT_MATCH = 'Current password does not match';
const NEW_PASSWORD_INVALID = 'New password is invalid';
const NEW_PASSWORD_SHOULD_DIFFERENT_OLD_PASSWORD = 'New password should be different from existing';

@Component({
  selector: 'tb-dft-change-password-dialog',
  templateUrl: './dft-change-password-dialog.component.html',
  styleUrls: ['./dft-change-password-dialog.component.scss']
})
export class DftChangePasswordDialogComponent extends DialogComponent<DftChangePasswordDialogComponent> implements OnInit {

  changePassword: FormGroup;

  constructor(protected store: Store<AppState>,
              protected router: Router,
              private translate: TranslateService,
              private authService: AuthService,
              public dialogService: DialogService,
              public dialogRef: MatDialogRef<DftChangePasswordDialogComponent>,
              public fb: FormBuilder) {
    super(store, router, dialogRef);
  }

  ngOnInit(): void {
    this.buildChangePasswordForm();
  }

  buildChangePasswordForm() {
    this.changePassword = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^[^\\sáàảãạÁÀẢÃẠăắằẳẵặĂẮẰẲẴẶâấầẩẫậÂẤẦẨẪẬéèẻẽẹÉÈẺẼẸêếềểễệÊẾỀỂỄỆíìỉĩịÍÌỈĨỊýỳỷỹỵÝỲỶỸỴóòỏõọÓÒỎÕỌôồốổỗộÔỒỐỔỖỘơớờởỡợƠỚỜỞỠỢúùủũụÚÙỦŨỤưứừửữựƯỨỪỬỮỰ]{6,}$')]],
      newPassword2: ['', [Validators.required, Validators.maxLength(255)]]
    }, {validator: this.checkPasswords('newPassword', 'newPassword2')});
  }

  checkPasswords(password: string, rePassword: string) {
    return (group: FormGroup) => {
      // tslint:disable-next-line:one-variable-per-declaration
      const passwordInput = group.controls[password],
        passwordConfirmationInput = group.controls[rePassword];
      if (passwordInput.value !== passwordConfirmationInput.value) {
        return passwordConfirmationInput.setErrors({notEquivalent: true});
      } else {
        return passwordConfirmationInput.setErrors(null);
      }
    };
  }

  onChangePassword(): void {
    if (this.changePassword.valid) {
      this.authService.dftCustomChangePassword(
        this.changePassword.get('currentPassword').value,
        this.changePassword.get('newPassword').value
      ).subscribe(
        () => {
          this.dialogRef.close(true);
        },
        (err) => {
          console.log(err);
          if (err.status === 400 && err.error.message === CURRENT_PASSWORD_DOES_NOT_MATCH){
            this.dialogService.alert('', 'Mật khẩu hiện tại không đúng', 'Ok', false);
          }
          else if (err.status === 400 && err.error.message === NEW_PASSWORD_INVALID){
            this.dialogService.alert('', 'Mật khẩu mới không hợp lệ', 'Ok', false);
          }
          else if (err.status === 400 && err.error.message === NEW_PASSWORD_SHOULD_DIFFERENT_OLD_PASSWORD){
            this.dialogService.alert('', 'Mật khẩu mới nên khác với mật khẩu cũ', 'Ok', false);
          }
          else{
            this.dialogService.alert('', 'Hệ thống gặp sự cố, xin vui lòng thử lại sau', 'Ok', false);
          }
        });
    }
  }

}
