import {Injectable} from '@angular/core';
import {AbstractControl, AsyncValidatorFn, FormControl} from '@angular/forms';
import {Observable, timer} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {RpcScheduleService} from '@modules/dft/service/rpc/rpc-schedule.service';

@Injectable({
  providedIn: 'root'
})
export class RpcScheduleValidator {

  constructor(private rpcSchuduleService: RpcScheduleService) { }

  rpcSchuduleNameValidator(damTomId: string, time: number = 500): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      return timer(time).pipe(
        switchMap(() => this.rpcSchuduleService.validateRpcSchduleName(damTomId, encodeURI(control.value))),
        map(res => {
          if (res) {
            return { uniqueRpcScheduleName: true };
          }
        })
      );
    };
  }

  editRpcScheduleNameValidator(damTomId: string, id: string, time: number = 500): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      return timer(time).pipe(
        switchMap(() => this.rpcSchuduleService.validateRpcSchduleName(damTomId, encodeURI(control.value), id)),
        map(res => {
          if (res) {
            return { uniqueRpcScheduleName: true };
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
