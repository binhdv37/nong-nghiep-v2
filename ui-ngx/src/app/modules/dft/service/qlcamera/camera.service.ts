import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PageLink} from '@shared/models/page/page-link';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {Observable} from 'rxjs';
import {PageData} from '@shared/models/page/page-data';
import {DamtomCamera} from '@modules/dft/models/qlcamera.model';

const CAMERA_API = '/api/cameras';

@Injectable({
  providedIn: 'root'
})
export class CameraService {

  constructor(private http: HttpClient) { }

  public getDamtomCameras(damtomId: string, pageLink: PageLink, config?: RequestConfig): Observable<PageData<DamtomCamera>> {
    return this.http.get<PageData<DamtomCamera>>(`${CAMERA_API}${pageLink.toQuery()}&damtomId=${damtomId}`, defaultHttpOptionsFromConfig(config));
  }

  public getAllDamtomCameras(damtomId: string, config?: RequestConfig): Observable<DamtomCamera[]> {
    return this.http.get<DamtomCamera[]>(`${CAMERA_API}/getAll?damtomId=${damtomId}`);
  }

  public getCameraById(cameraId: string, config?: RequestConfig): Observable<DamtomCamera> {
    return this.http.get<DamtomCamera>(`${CAMERA_API}/${cameraId}`, defaultHttpOptionsFromConfig(config));
  }

  // create or update
  public saveCamera(camera: DamtomCamera, config?: RequestConfig): Observable<DamtomCamera> {
    return this.http.post<DamtomCamera>(`${CAMERA_API}`, camera, defaultHttpOptionsFromConfig(config));
  }

  public deleteCameraById(cameraId: string, config?: RequestConfig) {
    return this.http.delete(`${CAMERA_API}/${cameraId}`, defaultHttpOptionsFromConfig(config));
  }
}
