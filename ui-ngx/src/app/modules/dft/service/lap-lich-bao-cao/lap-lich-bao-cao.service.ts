import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {PageData} from 'src/app/shared/models/page/page-data';
import {PageLink} from 'src/app/shared/models/page/page-link';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@core/http/http-utils';
import {DamTomSchedule, ReportSchedule, ReportScheduleCreateOrUpdate} from '@modules/dft/models/lap-lich-bao-cao/ReportSchedule.model';
import {UsersDft} from '@modules/dft/models/usersDft/users-dft.model';

@Injectable({
  providedIn: 'root'
})
export class LapLichBaoCaoService {


  constructor(private http: HttpClient) {
  }

  public createReportSchedule(newReportSchedule: ReportScheduleCreateOrUpdate, config?: RequestConfig): Observable<ReportSchedule> {
    return this.http.post<ReportSchedule>( `/api/report-schedule`, newReportSchedule
      , defaultHttpOptionsFromConfig(config));
  }

  // get List ReportSchedule
  public getListReportSchedule(pageLink: PageLink, config?: RequestConfig): Observable<PageData<ReportSchedule>> {
    return this.http.get<PageData<ReportSchedule>>( `/api/report-schedule${pageLink.toQuery()}`
      , defaultHttpOptionsFromConfig(config));
  }

  // get ReportSchedule
  public getReportSchedule(id: string, config?: RequestConfig): Observable<ReportSchedule> {
    return this.http.get<ReportSchedule>(`/api/report-schedule/${id}`
      , defaultHttpOptionsFromConfig(config));
  }


  // Update ReportSchedule
  public updateReportSchedule(id: string, updateReportSchedule: ReportScheduleCreateOrUpdate,
                              config?: RequestConfig): Observable<ReportSchedule> {
    return this.http.put<ReportSchedule>( `/api/report-schedule/${id}`, updateReportSchedule, defaultHttpOptionsFromConfig(config));
  }

  // Delete ReportSchedule
  public deleteReportSchedule(idReportSchedule: string, config?: RequestConfig) {
    return this.http.delete(`/api/report-schedule/${idReportSchedule}`, defaultHttpOptionsFromConfig(config));
  }

  // Get All Damtom cho select box
  public getListDamTom(pageLink: PageLink, config?: RequestConfig): Observable<PageData<DamTomSchedule>> {
    return this.http.get<PageData<DamTomSchedule>>(`/api/dam-tom${pageLink.toQuery()}`, defaultHttpOptionsFromConfig(config));
  }

  // Get Dam Tom
  public getDamTom(damtomId: string, config?: RequestConfig): Observable<DamTomSchedule> {
    return this.http.get<DamTomSchedule>(`/api/dam-tom/${damtomId}`, defaultHttpOptionsFromConfig(config));
  }


  // get user cho userSelectbox
  public getAllUser(config?: RequestConfig): Observable<UsersDft[]> {
    return this.http.get<UsersDft[]>(`/api/mb-users`, defaultHttpOptionsFromConfig(config));
  }


   //check schedule name ton tai
   public isScheduleNameExist(scheduleId: string,scheduleName : string,config?: RequestConfig): Observable<boolean> {
    let toQuery;
    if(scheduleId==null){
      toQuery = `?name=${scheduleName}`
    }
    else{
      toQuery = `?scheduleId=${scheduleId}&name=${scheduleName}`
    }
    return this.http.get<boolean>(`/api/report-schedule/check-name-exist`+toQuery, defaultHttpOptionsFromConfig(config));
  }
    //get All DamTom
   public getAllUsersDft(pageLink: PageLink, config?: RequestConfig): Observable<PageData<UsersDft>> {
    return this.http.get<PageData<UsersDft>>(`/api/list-users${pageLink.toQuery()}`, defaultHttpOptionsFromConfig(config));
  }

}
