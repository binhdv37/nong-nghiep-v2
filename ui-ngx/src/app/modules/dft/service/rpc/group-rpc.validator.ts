import {Injectable} from '@angular/core';
import {AbstractControl, AsyncValidatorFn, FormControl} from '@angular/forms';
import {Observable, timer} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {TableAction} from '../../models/action.model';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';

@Injectable({
  providedIn: 'root'
})
export class GroupRpcValidators {

  constructor(private deviceRpcService: DeviceRpcService) { }

  groupRpcNameValidator(damTomId: string, time: number = 500): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      const query = `type=${TableAction.ADD_ENTITY}&damTomId=${damTomId}&groupRpcName=${encodeURI(control.value)}`;
      return timer(time).pipe(
        switchMap(() => this.deviceRpcService.validateGroupRpc(query)),
        map(res => {
          if (res) {
            return { uniqueGroupRpcName: true };
          }
        })
      );
    };
  }

  editGroupRpcNameValidator(damTomId: string, id: string, time: number = 500): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      const query = `type=${TableAction.EDIT_ENTITY}&damTomId=${damTomId}&groupRpcName=${encodeURI(control.value)}&id=${id}`;
      return timer(time).pipe(
        switchMap(() => this.deviceRpcService.validateGroupRpc(query)),
        map(res => {
          if (res) {
            return { uniqueGroupRpcName: true };
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
