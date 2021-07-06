import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {Observable} from 'rxjs';
import {DieuKhien} from '@modules/dft/models/rpc/rpc-auto.model';

const API = '/api/rpc-setting';

@Injectable({
  providedIn: 'root'
})
export class RpcSettingService {

  constructor(private http: HttpClient) {
  }

  public getRpcSetting(id: string, config?: RequestConfig): Observable<DieuKhien>{
    return this.http.get<DieuKhien>(`${API}/${id}`, defaultHttpOptionsFromConfig(config));
  }

  public saveRpcSetting(dieuKhien: DieuKhien, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`${API}`, dieuKhien, defaultHttpOptionsFromConfig(config));
  }

}
