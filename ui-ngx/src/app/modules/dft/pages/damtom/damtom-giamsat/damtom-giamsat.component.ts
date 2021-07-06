import {Component, Input, OnInit} from '@angular/core';
import {Dashboard} from '@app/shared/public-api';
import apiUsageDashboard from '!raw-loader!./damtom-dashboard-json.raw';

@Component({
  selector: 'tb-damtom-giamsat',
  templateUrl: './damtom-giamsat.component.html',
  styleUrls: ['./damtom-giamsat.component.scss']
})
export class DamTomGiamsatComponent implements OnInit {

  // tslint:disable-next-line: no-input-rename
  @Input('damTomId') damTomId: string;

  // tslint:disable-next-line: no-input-rename
  @Input('assertId') assertId: string;

  apiUsageDashboard: Dashboard;

  constructor() {
  }

  ngOnInit() {
    console.log(this.assertId);
    this.setUpDashBoard();
    // this.getDashboard();
  }

  setUpDashBoard() {
    this.apiUsageDashboard = JSON.parse(apiUsageDashboard);
    this.apiUsageDashboard.configuration.entityAliases.entityAliasId.filter.rootEntity.id = this.assertId;
  }

  // getDashboard() {
  //   this.dashboardService.getDashboard('62d073d0-8b8d-11eb-8742-3532ed36613a')
  //     .pipe(
  //       tap((data: Dashboard) => {
  //         data.configuration.settings = {
  //           stateControllerId: 'entity',
  //           showTitle: false,
  //           showDashboardsSelect: false,
  //           showEntitiesSelect: false,
  //           showDashboardTimewindow: false,
  //           showDashboardExport: false,
  //           toolbarAlwaysOpen: true,
  //           titleColor: 'rgba(0,0,0,0.870588)',
  //           showFilters: false
  //         };
  //         this.apiUsageDashboard = data;
  //       }),
  //       finalize(() => {}),
  //       catchError(() => null)
  //     ).subscribe();
  // }

}
