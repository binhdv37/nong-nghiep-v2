import {AfterViewInit, Component, Inject, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {UsersDftService} from '@modules/dft/service/usersDft/users-dft.service';
import {ActivatedRoute, Router} from '@angular/router';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {AdditionalInfo, UsersDft} from '@modules/dft/models/usersDft/users-dft.model';
import {Role} from '@modules/dft/models/role.model';
import {PageData} from '@shared/models/page/page-data';
import {PageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {RoleService} from '@modules/dft/service/role.service';
import {NotificationUsersComponent} from '@modules/dft/pages/users-dft/notification-users/notification-users.component';
import {Observable, Subject} from 'rxjs';
import {DialogService} from '@core/services/dialog.service';
import {ToastrService} from 'ngx-toastr';

export interface DialogData {
  id: string;
  index: number;
}

const INTERNAL_001 = 'Fail while processing in thingsboard.';
const OK_002 = 'Save user successful in thingsboard.';
const BAD_002 = 'Email already exists in thingsboard.';
const BAD_003 = 'Phone number already exists in thingsboard.';

@Component({
  selector: 'tb-edit-users-dft',
  templateUrl: './edit-users-dft.component.html',
  styleUrls: ['./edit-users-dft.component.scss']
})
export class EditUsersDftComponent implements OnInit, AfterViewInit {

  editForm: FormGroup;
  currentUser: UsersDft;
  savedUser: UsersDft;
  // tslint:disable-next-line:new-parens
  additionalInfo: AdditionalInfo = new class implements AdditionalInfo {
    description: string;
  };
  submitted = false;
  roleList: Role[] = [];
  pageDataRole: PageData<Role>;
  pageLink: PageLink;
  defaultPageSize = 50;
  sortOrder: SortOrder;
  activeTab = 0;
  isLoading$: Observable<boolean>;
  mainSource$: Subject<boolean>;

  constructor(private usersDftService: UsersDftService,
              private formBuilder: FormBuilder,
              private roleService: RoleService,
              private router: Router,
              private toastrService: ToastrService,
              private matDialog: MatDialog,
              private dialogService: DialogService,
              private route: ActivatedRoute,
              private dialogRef: MatDialogRef<EditUsersDftComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit(): void {
    this.mainSource$.next(true);
    this.editForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      email: ['', [Validators.required, Validators.maxLength(320), Validators.pattern('^[a-zA-Z0-9]+([._-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+([.-]?[a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}$')]],
      lastName: ['', [Validators.maxLength(45), Validators.pattern('^\\+?[0-9]{7,44}$')]], // sdt
      enabled: [false],
      roleId: new FormArray([]),
      additionalInfo: ['', [Validators.maxLength(4000)]]
    });
    this.initRole();
    this.setValueToForm();
    if (this.data.index === 1) {
      this.activeTab = 1;
    } else {
      this.activeTab = 0;
    }
  }

  setValueToForm(): void {
    this.usersDftService.getUsersDft(this.data.id).subscribe(
      next => {
        this.currentUser = next;
        this.editForm = this.formBuilder.group({
          firstName: [next.firstName, [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
          email: [{value: next.email, disabled: true}, [Validators.required, Validators.maxLength(320),
            // Validators.pattern('[a-z0-9_.-]+(?:\\.[a-z0-9_.-])*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?')]],
            Validators.pattern('^[a-zA-Z0-9]+([._-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+([.-]?[a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}$')]],
          lastName: [next.lastName, [Validators.maxLength(45), Validators.pattern('^\\+?[0-9]{7,44}$')]],
          enabled: [next.enabled],
          roleId: new FormArray([]),
          additionalInfo: [next.additionalInfo.description, [Validators.maxLength(4000)]]
        });
        if (!!next.roleEntity) {
          const listUserRoleId: FormArray = this.editForm.get('roleId') as FormArray;
          this.currentUser.roleEntity.forEach(role => {
            listUserRoleId.push(new FormControl(role.id));
          });
        }
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
      }, error => {
        console.log(error);
      }, () => {
        console.log('completed');
      });
  }

  onSubmit(): void {
    this.mainSource$.next(true);
    this.submitted = true;
    if (this.editForm.valid) {
      const action = 'edit';
      const value = this.editForm.getRawValue();
      this.currentUser.firstName = value.firstName;
      this.currentUser.email = value.email;
      this.currentUser.lastName = value.lastName;
      this.currentUser.password = value.password;
      this.currentUser.enabled = value.enabled;
      this.currentUser.roleId = value.roleId;
      this.additionalInfo.description = value.additionalInfo;
      this.currentUser.additionalInfo = this.additionalInfo;
      this.usersDftService.saveUsersDft(this.currentUser, action).subscribe(
        next => {
          this.checkResponse(next);
        });
    }
  }

  checkResponse(response: UsersDft) {
    this.savedUser = response;
    if (this.savedUser.responseCode === 500 && this.savedUser.responseMessage === INTERNAL_001) {
      this.matDialog.open(NotificationUsersComponent, {
        data: {action: 'edit', message: 'Hệ thống hiện tại đang bị lỗi, thử lại sau ít phút'}
      });
    } else if (this.savedUser.responseCode === 400 && this.savedUser.responseMessage === BAD_002) {
      this.matDialog.open(NotificationUsersComponent, {
        data: {action: 'edit', message: 'Địa chỉ email đã tồn tại'}
      });
    } else if (this.savedUser.responseCode === 400 && this.savedUser.responseMessage === BAD_003) {
      this.matDialog.open(NotificationUsersComponent, {
        data: {action: 'edit', message: 'Số điện thoại đã tồn tại'}
      });
    } else if (this.savedUser.responseCode === 200 && this.savedUser.responseMessage === OK_002) {
      this.dialogRef.close();
      this.openSnackBar('Cập nhật tài khoản thành công');
    } else {
      console.log(this.savedUser.responseMessage);
    }
  }

  backToIndex(): void {
    this.submitted = false;
    this.editForm.reset();
    this.router.navigateByUrl('/account');
  }

  numberOnly(event): boolean {
    const charCode = (event.which) ? event.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
      return false;
    }
    return true;
  }

  ngAfterViewInit(): void {}

  onCheckboxChange(e): void {
    const listRoleId: FormArray = this.editForm.get('roleId') as FormArray;

    if (e.checked) {
      listRoleId.push(new FormControl(e.source.value));
    } else {
      console.log(e.source);
      const index = listRoleId.controls.findIndex(x => x.value === e.source.value);
      console.log(index);
      listRoleId.removeAt(index);
    }
    console.log(listRoleId);
  }

  compareRoleId(role: Role): boolean {
    const listRoleId = [];
    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < this.currentUser?.roleEntity?.length; i++) {
      listRoleId.push(this.currentUser.roleEntity[i].id);
    }
    return listRoleId.indexOf(role.id) >= 0;
  }

  openSnackBar(message: string): void {
    this.toastrService.success(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }

}
