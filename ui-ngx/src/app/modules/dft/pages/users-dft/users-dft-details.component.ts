import {Component, Inject, OnInit} from '@angular/core';
import {UsersDft} from '@modules/dft/models/usersDft/users-dft.model';
import {UsersDftService} from '@modules/dft/service/usersDft/users-dft.service';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {PageLink} from '@shared/models/page/page-link';
import {PageData} from '@shared/models/page/page-data';
import {Role} from '@modules/dft/models/role.model';
import {RoleService} from '@modules/dft/service/role.service';
import {NotificationUsersComponent} from '@modules/dft/pages/users-dft/notification-users/notification-users.component';

const INTERNAL_001 = 'Fail while processing in thingsboard.';
const OK_001 = 'Get user successful in thingsboard.';
const BAD_001 = 'Fail while get user from thingsboard.';

export interface DialogData {
  id: string;
}

@Component({
  selector: 'tb-users-dft-details',
  templateUrl: './users-dft-details.component.html',
  styleUrls: ['./users-dft-details.component.scss']
})

export class UsersDftDetailsComponent implements OnInit {

  currentUser: UsersDft;
  pageDataRole: PageData<Role>;
  roleList: Role[] = [];
  pageLink: PageLink;
  defaultPageSize = 50;
  sortOrder: SortOrder;
  listRoleId: string[] = [];
  listUserRoleId: string[] = [];

  constructor(private usersDftService: UsersDftService,
              private roleService: RoleService,
              private matDialog: MatDialog,
              private dialogRef: MatDialogRef<UsersDftDetailsComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData) {
  }

  ngOnInit(): void {
    this.usersDftService.getUsersDft(this.data.id).subscribe(
      next => {
        this.currentUser = next;
        if (this.currentUser.responseCode === 200 && this.currentUser.responseMessage === OK_001) {
          console.log(this.currentUser);
          this.initRole();
        } else if (this.currentUser.responseCode === 400 && this.currentUser.responseMessage === BAD_001) {
          this.dialogRef.close();
          this.matDialog.open(NotificationUsersComponent, {
            data: {action: 'details', message: 'Tài khoản không tồn tại'}
          });
        } else if (this.currentUser.responseCode === 500 && this.currentUser.responseMessage === INTERNAL_001) {
          this.dialogRef.close();
          this.matDialog.open(NotificationUsersComponent, {
            data: {action: 'details', message: 'Tài khoản không tồn tại'}
          });
        } else {
          this.dialogRef.close();
          this.matDialog.open(NotificationUsersComponent, {
            data: {action: 'details', message: 'Tài khoản không tồn tại'}
          });
          console.log(this.currentUser.responseMessage);
        }
        console.log(this.currentUser);
      }, error => {
        console.log(error);
        this.currentUser = null;
      }
    );
  }

  initRole(): void {
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    };
    this.pageLink = new PageLink(this.defaultPageSize, 0, null, this.sortOrder);
    this.roleService.getTenantRoles(this.pageLink).subscribe(
      next => {
        this.pageDataRole = next;
        this.roleList = next.data;
        console.log(this.roleList);
      }, error => {
        console.log(error);
      }, () => {
        console.log('completed');
      });
  }

  compareRoleId(role: Role): boolean {
    const listRoleId = [];
    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < this.currentUser.roleEntity?.length; i++) {
      listRoleId.push(this.currentUser.roleEntity[i].id);
    }
    return listRoleId.indexOf(role.id) >= 0;
  }
}
