import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {BcMultiData} from "@modules/dft/models/bao-cao/bao-cao.model";

const API_LINK = '/api/bc-dlgiamsat';

@Injectable({
  providedIn: 'root'
})
export class BcDlgiamsatService {

  constructor(private http: HttpClient) { }

  public getBcDlgiamsatData(key: string, startTs: number, endTs: number): Observable<BcMultiData[]> {
      let query = `?key=${key}&startTs=${startTs}&endTs=${endTs}`;
      return this.http.get<BcMultiData[]>(`${API_LINK}${query}`);
  }

}
