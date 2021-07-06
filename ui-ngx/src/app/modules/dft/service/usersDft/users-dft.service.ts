import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PageLink} from '@shared/models/page/page-link';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {Observable} from 'rxjs';
import {PageData} from '@shared/models/page/page-data';
import {UsersDft} from '@modules/dft/models/usersDft/users-dft.model';

@Injectable({
  providedIn: 'root'
})
export class UsersDftService {

  constructor(private httpClient: HttpClient) { }

  public getAllUsersDft(pageLink: PageLink, config?: RequestConfig): Observable<PageData<UsersDft>> {
    return this.httpClient.get<PageData<UsersDft>>(`/api/list-users${pageLink.toQuery()}`, defaultHttpOptionsFromConfig(config));
  }

  public getUsersDft(usersId: string, config?: RequestConfig): Observable<UsersDft> {
    return this.httpClient.get<UsersDft>(`/api/users/${usersId}`, defaultHttpOptionsFromConfig(config));
  }

  public saveUsersDft(newUsersDft: UsersDft, action: string, config?: RequestConfig): Observable<UsersDft> {
    return this.httpClient.post<UsersDft>(`/api/save-users/${action}`, newUsersDft, defaultHttpOptionsFromConfig(config));
  }

  public deleteUsersDft(usersId: string, config?: RequestConfig): Observable<UsersDft> {
    return this.httpClient.delete<UsersDft>(`/api/users/${usersId}`, defaultHttpOptionsFromConfig(config));
  }

}
