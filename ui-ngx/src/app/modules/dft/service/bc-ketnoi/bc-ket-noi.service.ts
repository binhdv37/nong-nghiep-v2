import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@app/core/public-api';
import {BcSingleData} from '@modules/dft/models/bao-cao/bao-cao.model';

const BC_KETNOICB_API = '/api/bc-ket-noi';

@Injectable({
  providedIn: 'root'
})
export class BaoCaoKetNoiService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getChartBcKetNoi(damTomId: string, startTime: number, endTime: number, config?: RequestConfig): Observable<BcSingleData[]> {
    let query;
    if (damTomId) {
      query = `damTomId=${damTomId}&startTime=${startTime}&endTime=${endTime}`;
    }else {
      query = `startTime=${startTime}&endTime=${endTime}`;
    }
    return this.http.get<BcSingleData[]>(`${BC_KETNOICB_API}/chart?${query}`, defaultHttpOptionsFromConfig(config));
  }

  public getTableBcKetNoi(damTomId: string, startTime: number, endTime: number, config?: RequestConfig): Observable<any[]> {
    let query;
    if (damTomId) {
      query = `damTomId=${damTomId}&startTime=${startTime}&endTime=${endTime}`;
    }else {
      query = `startTime=${startTime}&endTime=${endTime}`;
    }
    return this.http.get<any[]>(`${BC_KETNOICB_API}/table?${query}`, defaultHttpOptionsFromConfig(config));
  }

}
