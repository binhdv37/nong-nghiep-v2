import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BcMultiData, BcSingleData} from '@modules/dft/models/bao-cao/bao-cao.model';

const API_LINK = '/api/bc-tonghop';

@Injectable({
  providedIn: 'root'
})
export class BcTonghopService {

  constructor(private httpClient: HttpClient) { }

  public getDamtomCanhBaoData(damtomId: string, startTs: number, endTs: number): Observable<BcSingleData[]>{
    let query;
    if (damtomId === 'ALL') {
       query = `?startTs=${startTs}&endTs=${endTs}`;
    } else{
      query = `?damtomId=${damtomId}&startTs=${startTs}&endTs=${endTs}`;
    }
    return this.httpClient.get<BcSingleData[]>(`${API_LINK}/canh-bao${query}`);
  }

  public getDamtomKeyDlcambienData(damtomId: string, key: string, startTs: number, endTs: number): Observable<BcMultiData[]>{
    let query;
    if (damtomId === 'ALL') {
      query = `?key=${key}&startTs=${startTs}&endTs=${endTs}`;
    } else{
      query = `?damtomId=${damtomId}&key=${key}&startTs=${startTs}&endTs=${endTs}`;
    }
    return this.httpClient.get<BcMultiData[]>(`${API_LINK}/dl-cambien${query}`);
  }
}
