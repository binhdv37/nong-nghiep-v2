import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@app/core/public-api';
import {Device} from '@app/shared/public-api';
import {Observable} from 'rxjs';

import {DeviceRpc} from '../../models/rpc/device-rpc.model';
import {GroupRpc} from '../../models/rpc/group-rpc.model';


const TB_DIEU_KHIEN_API = '/api/rpc-device';
const GROUP_RPC_API = '/api/group-rpc';
const RPC_COMMAND_API = '/api/rpc-queue';

@Injectable({
  providedIn: 'root'
})
export class DeviceRpcService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getAllRpcDevice(damtomId: string, config?: RequestConfig): Observable<DeviceRpc[]> {
    const params = new HttpParams()
      .set('damTomId', damtomId);
    return this.http.get<DeviceRpc[]>(`${TB_DIEU_KHIEN_API}`, {params});
  }

  public getAllGroupRpc(damtomId: string, config?: RequestConfig): Observable<GroupRpc[]> {
    const params = new HttpParams()
      .set('damTomId', damtomId);
    return this.http.get<GroupRpc[]>(`${GROUP_RPC_API}`, {params});
  }

  public getGroupRpcById(id: string, config?: RequestConfig): Observable<GroupRpc> {
    return this.http.get<GroupRpc>(`${GROUP_RPC_API}/${id}`, defaultHttpOptionsFromConfig(config));
  }

  public saveGroupRpc(groupRpc: GroupRpc, config?: RequestConfig): Observable<GroupRpc> {
    return this.http.post<GroupRpc>(`${GROUP_RPC_API}`, groupRpc, defaultHttpOptionsFromConfig(config));
  }

  public deleteGroupRpc(groupRpcId: string, config?: RequestConfig): Observable<any> {
    return this.http.delete<any>(`${GROUP_RPC_API}/${groupRpcId}`, defaultHttpOptionsFromConfig(config));
  }

  public validateGroupRpc(query: string, config?: RequestConfig): Observable<boolean> {
    return this.http.get<boolean>(`${GROUP_RPC_API}/validate?${query}`, defaultHttpOptionsFromConfig(config));
  }

  public sendOneWayRpcCommand(deviceId: string, requestBody: any, config?: RequestConfig): Observable<any> {
    return this.http.post<Device>(`/api/plugins/rpc/oneway/${deviceId}`, requestBody, defaultHttpOptionsFromConfig(config));
  }

  public sendTwoWayRpcCommand(deviceId: string, requestBody: any, config?: RequestConfig): Observable<any> {
    return this.http.post<Device>(`/api/plugins/rpc/twoway/${deviceId}`, requestBody, defaultHttpOptionsFromConfig(config));
  }

  public getLastestStatusDevice(deviceId: string, key: string, config?: RequestConfig) {
    return this.http.get<any>(`/api/plugins/telemetry/DEVICE/${deviceId}/values/timeseries?keys=${key}`,
      defaultHttpOptionsFromConfig(config));
  }

  public setManualCommandRpc(rpcRequest: any, config?: RequestConfig) {
    return this.http.post<any>(`${RPC_COMMAND_API}`, rpcRequest, defaultHttpOptionsFromConfig(config));
  }

  public startGroupRpcDevice(groupRpcId: any, config?: RequestConfig) {
    return this.http.post<any>(`${GROUP_RPC_API}/start/${groupRpcId}`, defaultHttpOptionsFromConfig(config));
  }

  public stopGroupRpcDevice(groupRpcId: any, config?: RequestConfig) {
    return this.http.post<any>(`${GROUP_RPC_API}/stop/${groupRpcId}`, defaultHttpOptionsFromConfig(config));
  }

  public statusGroupRpcDevice(groupRpcId: any, config?: RequestConfig) {
    return this.http.get<any>(`${GROUP_RPC_API}/status/${groupRpcId}`, defaultHttpOptionsFromConfig(config));
  }
}
