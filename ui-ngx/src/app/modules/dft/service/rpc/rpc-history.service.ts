import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {Observable} from 'rxjs';
import {RpcSchedule} from '@modules/dft/models/rpc/schedule-rpc.model';
import {PageLink, TimePageLink} from '@shared/models/page/page-link';
import {PageData} from '@shared/models/page/page-data';
import {KhachHang} from '@modules/dft/models/khachhang.model';
import {RpcCommand} from '@modules/dft/models/rpc/rpc-command.model';

const RPC_HISTORY_API = '/api/rpc-queue';

@Injectable({
  providedIn: 'root'
})
export class RpcHistoryService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getListRpcHistory(pageLink: TimePageLink, damTomId: string,
                           deviceId?: any, config?: RequestConfig): Observable<PageData<RpcCommand>> {
    let query = '';
    if (deviceId !== null && deviceId !== 0) {
      query = `&damtomId=${damTomId}&deviceId=${deviceId}`;
    } else {
      query = `&damtomId=${damTomId}`;
    }
    return this.http.get<PageData<RpcCommand>>(`${RPC_HISTORY_API}${pageLink.toQuery()}${query}`, defaultHttpOptionsFromConfig(config));
  }


}
