import { Component, OnInit } from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {AuthService} from "@core/auth/auth.service";
import {DialogService} from "@core/services/dialog.service";
import {Router} from "@angular/router";
import {ActionNotificationShow} from "@core/notification/notification.actions";
import {PageComponent} from "@shared/components/page.component";
import {Store} from "@ngrx/store";
import {AppState} from "@core/core.state";

@Component({
  selector: 'tb-dft-reset-password-request',
  templateUrl: './dft-reset-password-request.component.html',
  styleUrls: ['./dft-reset-password-request.component.scss']
})
export class DftResetPasswordRequestComponent extends PageComponent implements OnInit {

  requestPasswordRequest = this.fb.group({
    email: ['', [Validators.required, Validators.maxLength(320), Validators.pattern('^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{1,})+$')]]
  });

  constructor(
    protected store: Store<AppState>,
    private authService: AuthService,
    private dialogService: DialogService,
    private router: Router,
    public fb: FormBuilder
  ) {
    super(store);
  }

  ngOnInit(): void {
  }

  sendResetPasswordRequest() {
    if (this.requestPasswordRequest.valid) {
      this.authService.dftResetPasswordRequest(this.requestPasswordRequest.get('email').value).subscribe(
        (resp) => {
          console.log(resp);
          this.dialogService.alert('Đặt lại mật khẩu thành công!', 'Kiểm tra email để nhận mật khẩu mới!', 'ok', false)
            .subscribe(resp => {
              this.router.navigate(['/login']);
            });
        },
        // binh dv
        (error) => {
          console.log(error);
          if(error.status === 400 && error.error == 1) {
            this.store.dispatch(new ActionNotificationShow({
              message: 'Email không tồn tại!',
              type: 'error'
            }));
          }
          else if (error.status === 400 && error.error == 2){
            this.store.dispatch(new ActionNotificationShow({
              message: 'Tài khoản sysadmin - Bạn không có quyền thực hiện hành động này',
              type: 'error'
            }));
          }
          else{
            this.store.dispatch(new ActionNotificationShow({
              message: 'Hệ thống gặp sự cố. Xin thử lại sau!',
              type: 'error'
            }));
          }
        }
      );
    }
  }

}
