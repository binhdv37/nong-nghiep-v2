import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {Observable} from 'rxjs';
import {Devicelabel} from '@modules/dft/models/devicelabel.model';

const DAMTOM_DEVICE_API = '/api/dam-tom/device';


@Injectable({
  providedIn: 'root'
})
export class DftDeviceService {

  constructor(
    private http: HttpClient
  ) {
  }

  public editDeviceName(deviceId: string, deviceLabel: Devicelabel, config?: RequestConfig): Observable<any> {
    return this.http.put<any>(`${DAMTOM_DEVICE_API}/${deviceId}/change-label`,
      deviceLabel, defaultHttpOptionsFromConfig(config));
  }

}
