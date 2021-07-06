import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PageLink} from "@shared/models/page/page-link";
import {defaultHttpOptionsFromConfig, RequestConfig} from "@core/http/http-utils";
import {Observable} from "rxjs";
import {PageData} from "@shared/models/page/page-data";
import {Role} from "@modules/dft/models/role.model";


const ROLE_API = '/api/roles';

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  constructor(
    private http: HttpClient
  ) { }

  public getTenantRoles(pageLink: PageLink, config?: RequestConfig): Observable<PageData<Role>> {
    return this.http.get<PageData<Role>>(`${ROLE_API}${pageLink.toQuery()}`, defaultHttpOptionsFromConfig(config));
  }

  public getRoleById(roleId: string, config?: RequestConfig): Observable<Role> {
    return this.http.get<Role>(`${ROLE_API}/${roleId}`, defaultHttpOptionsFromConfig(config));
  }


  // create or update
  public saveRole(role: Role, config?: RequestConfig): Observable<Role> {
    return this.http.post<Role>(`${ROLE_API}`, role, defaultHttpOptionsFromConfig(config));
  }

  public deleteRoleById(roleId: string, config?: RequestConfig) {
    return this.http.delete(`${ROLE_API}/${roleId}`, defaultHttpOptionsFromConfig(config));
  }

}
