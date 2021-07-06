import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@app/core/public-api';
import {BoDuLieuCamBien} from '../../models/dlcb/bodulieucambien.model';
import { PageData } from '@app/shared/public-api';

const DLCB_API = '/api/du-lieu-cb';

@Injectable({
  providedIn: 'root'
})
export class DuLieuCamBienService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getListDuLieuCamBien(query: string, config?: RequestConfig): Observable<PageData<BoDuLieuCamBien>> {
    return this.http.get<PageData<BoDuLieuCamBien>>(`${DLCB_API}?${query}`, defaultHttpOptionsFromConfig(config));
  }
}
