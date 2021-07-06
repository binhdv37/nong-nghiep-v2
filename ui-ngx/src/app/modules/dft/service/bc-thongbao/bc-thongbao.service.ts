import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BcMultiData} from '@modules/dft/models/bao-cao/bao-cao.model';

const API_LINK = '/api/bc-gui-ttcb';

@Injectable({
  providedIn: 'root'
})
export class BcThongbaoService {

  constructor(private http: HttpClient) {}

  getBcThongBaoData(startTs: number, endTs: number): Observable<BcMultiData[]> {
    const query = `?startTs=${startTs}&endTs=${endTs}`;
    return this.http.get<BcMultiData[]>(`${API_LINK}${query}`);
  }

}
