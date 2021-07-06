import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {BcSingleData} from "@modules/dft/models/bao-cao/bao-cao.model";

const API_LINK = '/api/bc-canhbao';

@Injectable({
  providedIn: 'root'
})
export class BcCanhbaoService {

  constructor(private http: HttpClient) { }

  public getDamtomBcCanhBaoData(damtomId: string, startTs: number, endTs: number): Observable<BcSingleData[]>{
    let query = `?damtomId=${damtomId}&startTs=${startTs}&endTs=${endTs}`;
    return this.http.get<BcSingleData[]>(`${API_LINK}/dam-tom${query}`);
  }

  public getTenantBcCanhBaoData(startTs: number, endTs: number): Observable<BcSingleData[]>{
    let query = `?&startTs=${startTs}&endTs=${endTs}`;
    return this.http.get<BcSingleData[]>(`${API_LINK}/tenant${query}`);
  }


}
