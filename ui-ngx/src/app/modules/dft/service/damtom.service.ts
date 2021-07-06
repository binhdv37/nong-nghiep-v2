import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {PageLink} from '@shared/models/page/page-link';
import {PageData} from '@shared/models/page/page-data';
import {DamTom, ThietBi} from '../models/damtom.model';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@app/core/public-api';
import {GetGatewayResponse} from '@modules/dft/models/rpc/rpc-auto.model';

const DAMTOM_API = '/api/dam-tom';

@Injectable({
  providedIn: 'root'
})
export class DamTomService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getListDamTom(pageLink: PageLink, config?: RequestConfig): Observable<PageData<DamTom>> {
    return this.http.get<PageData<DamTom>>(`${DAMTOM_API}${pageLink.toQuery()}`, defaultHttpOptionsFromConfig(config));
  }

  public getDamTom(damtomId: string, config?: RequestConfig): Observable<DamTom> {
    return this.http.get<DamTom>(`${DAMTOM_API}/${damtomId}`, defaultHttpOptionsFromConfig(config));
  }

  public saveDamTom(damtom: DamTom, config?: RequestConfig): Observable<DamTom> {
    return this.http.post<DamTom>(`${DAMTOM_API}`, damtom, defaultHttpOptionsFromConfig(config));
  }

  public deleteDamTom(damtomId: string, config?: RequestConfig) {
    return this.http.delete(`${DAMTOM_API}/${damtomId}`, defaultHttpOptionsFromConfig(config));
  }

  public createDevice(thietbi: ThietBi, config?: RequestConfig): Observable<DamTom> {
    return this.http.post<DamTom>(`${DAMTOM_API}` + '/device', thietbi, defaultHttpOptionsFromConfig(config));
  }

  public getListDevice(damtomId: string, pageLink: PageLink, config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${DAMTOM_API}/device${pageLink.toQuery()}&damtomId=${damtomId}`, defaultHttpOptionsFromConfig(config));
  }

  public deleteGateWay(gateWayId: string, config?: RequestConfig) {
    return this.http.delete(`${DAMTOM_API}/device/${gateWayId}`, defaultHttpOptionsFromConfig(config));
  }

  public getGateWay(gateWayId: string, config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${DAMTOM_API}/device/${gateWayId}`, defaultHttpOptionsFromConfig(config));
  }

  public editDevice(thietbi: ThietBi, config?: RequestConfig): Observable<any> {
    return this.http.post<any>(`${DAMTOM_API}` + '/device/edit', thietbi, defaultHttpOptionsFromConfig(config));
  }

  public getViewGiamSatDamTom(config?: RequestConfig): Observable<any> {
    return this.http.get<any>('/api/giam-sat', defaultHttpOptionsFromConfig(config));
  }

  // binhdv - get all tenant active damtom
  public getAllTenantActiveDamtom(config?: RequestConfig): Observable<DamTom[]> {
    return this.http.get<DamTom[]>(`${DAMTOM_API}/get-all/active`, defaultHttpOptionsFromConfig(config));
  }

  // binhdv new service for rpc auto
  public getDamtomDto(damtomId: string, config?: RequestConfig): Observable<any> {
    return this.http.get<any>(`${DAMTOM_API}/${damtomId}`, defaultHttpOptionsFromConfig(config));
  }

  getDeviceTelemetryType(id: string): Observable<any> {
    return this.http.get<any>(`/api/plugins/telemetry/DEVICE/${id}/keys/timeseries`);
  }

  public getGateWayInfo(gateWayId: string, config?: RequestConfig): Observable<GetGatewayResponse> {
    return this.http.get<any>(`${DAMTOM_API}/device/${gateWayId}`, defaultHttpOptionsFromConfig(config));
  }

}
