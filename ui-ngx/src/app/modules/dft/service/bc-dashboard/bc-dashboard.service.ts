import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@app/core/public-api';
import {DamTom} from '@modules/dft/models/damtom.model';

const BC_DASHBOARD_API = '/api/bao-cao/dashboard';
const DAMTOM_API = '/api/dam-tom';

@Injectable({
  providedIn: 'root'
})
export class BcDashboardService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getListDamTomActive(config?: RequestConfig): Observable<DamTom[]> {
    return this.http.get<DamTom[]>(`${DAMTOM_API}/get-all/active`, defaultHttpOptionsFromConfig(config));
  }

  public checkAuthViewDashboard(config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${BC_DASHBOARD_API}`, defaultHttpOptionsFromConfig(config));
  }


}
