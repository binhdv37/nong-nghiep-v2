import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RoleRoutingModule } from './role-routing.module';
import {RoleComponent} from '@modules/dft/pages/role/role.component';
import {SharedModule} from '@shared/shared.module';
import { EditRoleComponent } from './edit-role/edit-role.component';
import { CheckBoxTreeComponent } from './check-box-tree/check-box-tree.component';


@NgModule({
  declarations: [RoleComponent, EditRoleComponent, CheckBoxTreeComponent],
  imports: [
    CommonModule,
    RoleRoutingModule,
    SharedModule,
  ]
})
export class RoleModule { }
