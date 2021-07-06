import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@app/core/public-api';

const SETTING_CAMERA_SERVER_API = '/api/sys-admin';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getSettingNotification(config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${SETTING_CAMERA_SERVER_API}/settings/notification`, defaultHttpOptionsFromConfig(config));
  }

  public settingNotification(cameraSetting: any, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`${SETTING_CAMERA_SERVER_API}/settings/notification`, cameraSetting, defaultHttpOptionsFromConfig(config));
  }

}
