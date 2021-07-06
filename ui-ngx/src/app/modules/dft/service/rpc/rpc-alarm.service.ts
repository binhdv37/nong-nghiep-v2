import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {Observable} from 'rxjs';
import {AlarmDtoV2} from '@modules/dft/models/luat-canhbao/luatcb.model';
import {RpcAlarm} from '@modules/dft/models/rpc/rpc-auto.model';

const LUAT_DIEU_KHIEN_API = '/api/damtom-rpc-alarm';

@Injectable({
  providedIn: 'root'
})
export class RpcAlarmService {

  constructor(private http: HttpClient) {
  }

  public getAllRpcAlarm(damtomId: string, config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${LUAT_DIEU_KHIEN_API}/${damtomId}`, defaultHttpOptionsFromConfig(config));
  }

  public getRpcAlarm(damtomId: string, alarmType: string, config?: RequestConfig): Observable<RpcAlarm> {
    return this.http.get<RpcAlarm>(`${LUAT_DIEU_KHIEN_API}?damTomId=${damtomId}&alarmType=${alarmType}`, defaultHttpOptionsFromConfig(config));
  }

  public saveRpcAlarm(alarmDtoV2: AlarmDtoV2, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`${LUAT_DIEU_KHIEN_API}`, alarmDtoV2, defaultHttpOptionsFromConfig(config));
  }

  public deleteRpcAlarm(damtomId: string, alarmId: string, config?: RequestConfig): Observable<any> {
    return this.http.delete<any>(`${LUAT_DIEU_KHIEN_API}?damTomId=${damtomId}&alarmId=${alarmId}`, defaultHttpOptionsFromConfig(config));
  }
}
