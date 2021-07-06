import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@app/core/public-api';

const SETTING_CAMERA_SERVER_API = '/api/sys-admin';

@Injectable({
  providedIn: 'root'
})
export class DftAdminSettingsService {

  constructor(
    private http: HttpClient
  ) {
  }

  public settingCamera(cameraSetting: any, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`${SETTING_CAMERA_SERVER_API}/settings/camera`, cameraSetting, defaultHttpOptionsFromConfig(config));
  }

  public getSettingCamera(config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${SETTING_CAMERA_SERVER_API}/settings/camera`, defaultHttpOptionsFromConfig(config));
  }

  public saveShinobiAccount(email: string, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`${SETTING_CAMERA_SERVER_API}/camera-account/sign-up/${encodeURI(email)}`,
      defaultHttpOptionsFromConfig(config));
  }

  public deleteShinobiAccount(email: string, config?: RequestConfig): Observable<any> {
    return this.http.delete<any>(`${SETTING_CAMERA_SERVER_API}/camera-account/delete/${encodeURI(email)}`,
      defaultHttpOptionsFromConfig(config));
  }

  public getCameraServerUrl(): Observable<any>{
    return this.http.get(`${SETTING_CAMERA_SERVER_API}/settings/camera-server-url`, {responseType: 'text'});
  }
}
