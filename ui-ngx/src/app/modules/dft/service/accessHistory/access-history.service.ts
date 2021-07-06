import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PageLink} from '@shared/models/page/page-link';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {Observable} from 'rxjs';
import {PageData} from '@shared/models/page/page-data';
import {AuditLog} from '@shared/models/audit-log.models';
import {AccessHistoryPage} from '@modules/dft/pages/access-history/access-history-page';

@Injectable({
  providedIn: 'root'
})
export class AccessHistoryService {

  constructor(private httpClient: HttpClient) { }

  public getAllAccessHistory(accessHistoryPage: AccessHistoryPage, config?: RequestConfig): Observable<PageData<AuditLog>> {
    return this.httpClient.get<PageData<AuditLog>>(`/api/users/logs/${accessHistoryPage.toQuery()}`, defaultHttpOptionsFromConfig(config));
  }

  public getAccessHistory(logId: string, config?: RequestConfig): Observable<AuditLog> {
    return this.httpClient.get<AuditLog>(`/api/users/logs/${logId}`, defaultHttpOptionsFromConfig(config));
  }

  public exportExcel(pageLink: PageLink, config?: RequestConfig): Observable<Blob> {
    return this.httpClient.get(`/api/users/logs/excel/${pageLink.toQuery()}`, {responseType: 'blob'});
  }
}
