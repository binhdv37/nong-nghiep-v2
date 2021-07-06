import { Injectable } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, FormControl } from '@angular/forms';
import { timer } from 'rxjs';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { TableAction } from '../../models/action.model';
import { KhachHangService } from './khachhang.service';

@Injectable({
  providedIn: 'root'
})
export class KhachHangValidators {

  constructor(private khachHangService: KhachHangService) { }

  maKhachHangValidator(time: number = 500): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      const query = `type=${TableAction.ADD_ENTITY}&maKhachHang=${encodeURI(control.value)}`;
      return timer(time).pipe(
        switchMap(() => this.khachHangService.validateKhachHang(query)),
        map(res => {
          if (res) {
            return { uniqueMaKhachHang: true };
          }
        })
      );
    };
  }

  phoneNumberValidator(time: number = 500): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      const query = `type=${TableAction.ADD_ENTITY}&phoneNumber=${encodeURI(control.value)}`;
      return timer(time).pipe(
        switchMap(() => this.khachHangService.validateKhachHang(query)),
        map(res => {
          if (res) {
            return { uniquePhoneNumber: true };
          }
        })
      );
    };
  }

  emailValidator(time: number = 500): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      const query = `type=${TableAction.ADD_ENTITY}&email=${encodeURI(control.value)}`;
      return timer(time).pipe(
        switchMap(() => this.khachHangService.validateKhachHang(query)),
        map(res => {
          if (res) {
            return { uniqueEmail: true };
          }
        })
      );
    };
  }

  editMaKhachHangValidator(id: string, time: number = 500): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      const query = `type=${TableAction.EDIT_ENTITY}&maKhachHang=${encodeURI(control.value)}&id=${id}`;
      return timer(time).pipe(
        switchMap(() => this.khachHangService.validateKhachHang(query)),
        map(res => {
          if (res) {
            return { uniqueMaKhachHang: true };
          }
        })
      );
    };
  }

  editPhoneNumberValidator(id: string, time: number = 500): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      const query = `type=${TableAction.EDIT_ENTITY}&phoneNumber=${encodeURI(control.value)}&id=${id}`;
      return timer(time).pipe(
        switchMap(() => this.khachHangService.validateKhachHang(query)),
        map(res => {
          if (res) {
            return { uniquePhoneNumber: true };
          }
        })
      );
    };
  }

  noWhitespaceValidator(control: FormControl) {
    const isSpace = (control.value || '').match(/\s/g);
    return isSpace ? { whitespace: true } : null;
  }

  noMoreWhitespaceValidator(control: FormControl) {
    const isSpace = (control.value || '').match(/\s\s+/g);
    return isSpace ? { whitespace: true } : null;
  }
}
