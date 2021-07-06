import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {Observable} from 'rxjs';
import {DamTom} from '@modules/dft/models/damtom.model';
import {AlarmDeleteDto, AlarmDto} from '@shared/models/alarmDto';
import {
  AlarmDtoV2,
  CheckAlarmNameExistDto,
  DeviceProfileAlarm
} from '@modules/dft/models/luat-canhbao/luatcb.model';


const DAMTOM_API = '/api/alarm-dt';
const DAMTOM_API2 = '/api/group-rpc';

@Injectable({
  providedIn: 'root'
})
export class LuatService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getAlarm(damtomId: string, config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${DAMTOM_API}/${damtomId}`, defaultHttpOptionsFromConfig(config));
  }

  public saveAlarm(alarmDto: AlarmDto, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`${DAMTOM_API}`, alarmDto, defaultHttpOptionsFromConfig(config));
  }

  public getAlarmEdit(alarmId: string, config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${DAMTOM_API}/alarm/${alarmId}`, defaultHttpOptionsFromConfig(config));
  }

  public deleteAlarm(alarmDeleteDto: AlarmDeleteDto, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`${DAMTOM_API}/delete`, alarmDeleteDto, defaultHttpOptionsFromConfig(config));
  }

  public getThietBiDieuKhien(damtomId: string, config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${DAMTOM_API2}/${damtomId}`, defaultHttpOptionsFromConfig(config));
  }

  // new api :
  public getAllAlarmsV2(damtomId: string, config?: RequestConfig): Observable<DeviceProfileAlarm[]> {
    return this.http.get<DeviceProfileAlarm[]>(`/api/damtom-alarm/v2/${damtomId}`, defaultHttpOptionsFromConfig(config));
  }

  public getAlarmV2(damtomId: string, alarmType: string, config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`/api/damtom-alarm?damTomId=${damtomId}&alarmType=${alarmType}`, defaultHttpOptionsFromConfig(config));
  }

  public saveAlarmV2(alarmDtoV2: AlarmDtoV2, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`/api/dt-alarm-v2`, alarmDtoV2, defaultHttpOptionsFromConfig(config));
  }

  public deleteAlarmV2(damtomId: string, alarmId: string, config?: RequestConfig): Observable<any> {
    return this.http.delete<any>(`/api/dt-alarm-v2?damTomId=${damtomId}&alarmId=${alarmId}`, defaultHttpOptionsFromConfig(config));
  }

  public checkNameExistV2(dto: CheckAlarmNameExistDto, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`/api/dt-alarm-v2/alarm-type-exist`, dto,
      defaultHttpOptionsFromConfig(config));
  }

}
