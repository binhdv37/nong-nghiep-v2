import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@app/core/public-api';
import {TimePageLink} from '@app/shared/public-api';
import {PageData} from '@shared/models/page/page-data';
import {Observable} from 'rxjs';
import {AlarmHistory} from '../../models/alarm-history/alarm-history.model';


const ALARM_HISTORY_API = '/api/alarm-history';

@Injectable({
  providedIn: 'root'
})
export class AlarmHistoryService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getListAlarmHistory(timePageLink: TimePageLink, damtomId: string, config?: RequestConfig): Observable<PageData<AlarmHistory>> {
    return this.http.get<PageData<AlarmHistory>>(`${ALARM_HISTORY_API}${timePageLink.toQuery()}&damtomId=${damtomId}`,
      defaultHttpOptionsFromConfig(config));
  }

  public clearAlarm(snapshotId: string, config?: RequestConfig): Observable<any> {
    return this.http.put<any>(`${ALARM_HISTORY_API}/${snapshotId}`,
      defaultHttpOptionsFromConfig(config));
  }

}
