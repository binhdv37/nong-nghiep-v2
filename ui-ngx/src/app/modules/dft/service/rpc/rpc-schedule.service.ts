import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {Observable} from 'rxjs';
import {RpcSchedule} from '@modules/dft/models/rpc/schedule-rpc.model';

const SCHEDULE_RPC_API = '/api/rpc-schedule';

@Injectable({
  providedIn: 'root'
})
export class RpcScheduleService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getAllRpcSchedule(damTomId: string, config?: RequestConfig): Observable<RpcSchedule[]> {
    const params = new HttpParams()
      .set('damTomId', damTomId);
    return this.http.get<RpcSchedule[]>(`${SCHEDULE_RPC_API}`, {params});
  }

  public getRpcScheduleById(id: string, config?: RequestConfig): Observable<RpcSchedule> {
    return this.http.get<RpcSchedule>(`${SCHEDULE_RPC_API}/${id}`, defaultHttpOptionsFromConfig(config));
  }

  public saveRpcSchedule(rpcSchedule: RpcSchedule, config?: RequestConfig): Observable<RpcSchedule> {
    return this.http.post<RpcSchedule>(`${SCHEDULE_RPC_API}`, rpcSchedule, defaultHttpOptionsFromConfig(config));
  }

  public updateRpcSchedule(rpcSchedule: RpcSchedule, rpcSchduleId: string, config?: RequestConfig): Observable<RpcSchedule> {
    return this.http.put<RpcSchedule>(`${SCHEDULE_RPC_API}/${rpcSchduleId}`, rpcSchedule, defaultHttpOptionsFromConfig(config));
  }

  public deleteRpcSchedule(RpcScheduleId: string, config?: RequestConfig): Observable<any> {
    return this.http.delete<any>(`${SCHEDULE_RPC_API}/${RpcScheduleId}`, defaultHttpOptionsFromConfig(config));
  }

  public validateRpcSchduleName(damTomId: string, rpcSchduleName: string,
                                id?: string, config?: RequestConfig): Observable<boolean> {
    const params = new HttpParams()
      .set('damTomId', damTomId)
      .set('rpcSchduleName', rpcSchduleName);
    if (id != null) {
      params.set('id', id);
    }
    return this.http.get<boolean>(`${SCHEDULE_RPC_API}/validate`, {params});
  }


}
