import {Component, OnDestroy, OnInit} from '@angular/core';
import {BcDashboardService} from '@modules/dft/service/bc-dashboard/bc-dashboard.service';
import {catchError, finalize, tap} from 'rxjs/operators';
import {of} from 'rxjs';
import apiUsageDashboard from '!raw-loader!./bc-dashboard-json.raw';
import {Dashboard} from '@shared/models/dashboard.models';

@Component({
  selector: 'tb-bc-dashboard',
  templateUrl: './bc-dashboard.component.html',
  styleUrls: ['./bc-dashboard.component.scss']
})
export class BcDashboardComponent implements OnInit, OnDestroy {

  listDamTom: any[] = [];

  listUsageDashboard: Dashboard[];

  constructor(private bcDashboardService: BcDashboardService) {
    this.listUsageDashboard = [];
  }

  ngOnInit() {
    this.checkAuthViewDashboard();
  }

  checkAuthViewDashboard() {
    this.bcDashboardService.checkAuthViewDashboard()
      .pipe(
        tap(data => {
          this.getListDashboardByDamTom();
        }),
        finalize(() => {}),
        catchError(err => {
          console.log(err);
          return of({});
        }),
      )
      .subscribe();
  }

  getListDashboardByDamTom() {
    this.bcDashboardService.getListDamTomActive()
      .pipe(
        tap(data => {
          this.listDamTom = data;
          this.listDamTom.forEach(damTom => {
            const usageDashboard = JSON.parse(apiUsageDashboard);
            usageDashboard.configuration.entityAliases.entityAliasId.filter.rootEntity.id = damTom.asset.id;
            usageDashboard.name = damTom.name;
            usageDashboard.title = damTom.name;
            usageDashboard.configuration.states.default.name = damTom.name;
            this.listUsageDashboard.push(usageDashboard);
            console.log(this.listUsageDashboard);
          });
        }),
        finalize(() => {}),
        catchError(err => {
          console.log(err);
          return of({});
        })
      ).subscribe();
  }

  ngOnDestroy() {
    this.listUsageDashboard = [];
  }

}
