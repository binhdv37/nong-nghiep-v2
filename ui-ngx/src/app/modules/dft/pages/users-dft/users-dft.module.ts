import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UsersDftComponent} from './users-dft.component';
import {SharedModule} from '@shared/shared.module';
import {UsersDftRoutingModule} from '@modules/dft/pages/users-dft/users-dft-routing.module';
import {CreateUsersDftComponent} from './create-users-dft.component';
import {UsersDftDetailsComponent} from './users-dft-details.component';
import {EditUsersDftComponent} from './edit-users-dft.component';
import {MatDialogModule} from '@angular/material/dialog';
import { NotificationUsersComponent } from './notification-users/notification-users.component';


@NgModule({
  declarations: [
    UsersDftComponent,
    CreateUsersDftComponent,
    UsersDftDetailsComponent,
    EditUsersDftComponent,
    NotificationUsersComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    UsersDftRoutingModule,
    MatDialogModule
  ]
})
export class UsersDftModule {
}
