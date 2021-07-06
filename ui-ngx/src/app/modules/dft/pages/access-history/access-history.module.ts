import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AccessHistoryRoutingModule } from './access-history-routing.module';
import { AccessHistoryComponent } from './access-history/access-history.component';
import {SharedModule} from "@shared/shared.module";
import { DetailsAccessHistoryComponent } from './details-access-history/details-access-history.component';


@NgModule({
  declarations: [AccessHistoryComponent, DetailsAccessHistoryComponent],
  imports: [
    CommonModule,
    SharedModule,
    AccessHistoryRoutingModule
  ]
})
export class AccessHistoryModule { }
