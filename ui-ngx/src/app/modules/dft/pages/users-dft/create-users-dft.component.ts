import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {UsersDftService} from '@modules/dft/service/usersDft/users-dft.service';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {PageData} from '@shared/models/page/page-data';
import {Role} from '@modules/dft/models/role.model';
import {RoleService} from '@modules/dft/service/role.service';
import {PageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {AdditionalInfo, UsersDft} from '@modules/dft/models/usersDft/users-dft.model';
import {NotificationUsersComponent} from '@modules/dft/pages/users-dft/notification-users/notification-users.component';
import {ToastrService} from 'ngx-toastr';

const INTERNAL_001 = 'Fail while processing in thingsboard.';
const OK_002 = 'Save user successful in thingsboard.';
const BAD_002 = 'Email already exists in thingsboard.';
const BAD_003 = 'Phone number already exists in thingsboard.';
const BAD_004 = 'Fail while save user from thingsboard.';

@Component({
  selector: 'tb-create-users-dft',
  templateUrl: './create-users-dft.component.html',
  styleUrls: ['./create-users-dft.component.scss']
})
export class CreateUsersDftComponent implements OnInit {

  hide = true;
  hideXacNhan = true;
  pageDataRole: PageData<Role>;
  roleList: Role[] = [];
  pageLink: PageLink;
  defaultPageSize = 50;
  sortOrder: SortOrder;
  createForm: FormGroup;
  submitted = false;
  savedUser: UsersDft;
  // tslint:disable-next-line:new-parens
  usersDft: UsersDft = new class implements UsersDft {
    // tslint:disable-next-line:new-parens
    additionalInfo: AdditionalInfo = new class implements AdditionalInfo {
      description: string;
    };
    createdTime: number;
    email: string;
    enabled: boolean;
    firstName: string;
    label: string;
    lastName: string;
    name: string;
    password: string;
    roleEntity: Role[];
    roleId: string[];
  };


  constructor(private usersDftService: UsersDftService,
              private formBuilder: FormBuilder,
              private toastrService: ToastrService,
              private roleService: RoleService,
              private dialogRef: MatDialogRef<CreateUsersDftComponent>,
              private matDialog: MatDialog) {
  }

  ngOnInit(): void {
    this.createForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      email: ['', [Validators.required, Validators.maxLength(320), Validators.pattern('^[a-zA-Z0-9]+([._-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+([.-]?[a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}$')]],
      lastName: ['', [Validators.maxLength(45), Validators.pattern('^\\+?[0-9]{7,44}$')]], // sdt
      // password: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?=.*[a-z])[^\s]{6,}$')]],
      password: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^[^\\sáàảãạÁÀẢÃẠăắằẳẵặĂẮẰẲẴẶâấầẩẫậÂẤẦẨẪẬéèẻẽẹÉÈẺẼẸêếềểễệÊẾỀỂỄỆíìỉĩịÍÌỈĨỊýỳỷỹỵÝỲỶỸỴóòỏõọÓÒỎÕỌôồốổỗộÔỒỐỔỖỘơớờởỡợƠỚỜỞỠỢúùủũụÚÙỦŨỤưứừửữựƯỨỪỬỮỰ]{6,}$')]],
      rePassword: ['', [Validators.required, Validators.maxLength(255)]],
      enabled: [false],
      roleId: new FormArray([]),
      additionalInfo: ['', [Validators.maxLength(4000)]]
    }, {validator: this.checkPasswords('password', 'rePassword')});
    this.sortOrder = {
      property: 'name',
      direction: Direction.ASC
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

  checkPasswords(password: string, rePassword: string) {
    return (group: FormGroup) => {
      // tslint:disable-next-line:one-variable-per-declaration
      const passwordInput = group.controls[password],
        passwordConfirmationInput = group.controls[rePassword];
      if (passwordInput.value !== passwordConfirmationInput.value) {
        return passwordConfirmationInput.setErrors({notEquivalent: true});
      } else {
        return passwordConfirmationInput.setErrors(null);
      }
    };
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.createForm.valid) {
      const action = 'create';
      const value = this.createForm.getRawValue();
      this.usersDft.firstName = value.firstName;
      this.usersDft.email = value.email;
      this.usersDft.lastName = value.lastName;
      this.usersDft.password = value.password;
      this.usersDft.enabled = value.enabled;
      this.usersDft.roleId = value.roleId;
      this.usersDft.additionalInfo.description = value.additionalInfo;
      console.log(this.usersDft);
      this.usersDftService.saveUsersDft(this.usersDft, action).subscribe(
        next => {
          this.checkResponse(next);
        });
    }
  }

  checkResponse(response: UsersDft) {
    this.savedUser = response;
    if (this.savedUser.responseCode === 500 && this.savedUser.responseMessage === INTERNAL_001) {
      this.matDialog.open(NotificationUsersComponent, {
        data: {action: 'create', message: 'Hệ thống hiện tại đang bị lỗi, thử lại sau ít phút'}
      });
    } else if (this.savedUser.responseCode === 400 && this.savedUser.responseMessage === BAD_002) {
      this.matDialog.open(NotificationUsersComponent, {
        data: {action: 'create', message: 'Địa chỉ email đã tồn tại'}
      });
    } else if (this.savedUser.responseCode === 400 && this.savedUser.responseMessage === BAD_003) {
      this.matDialog.open(NotificationUsersComponent, {
        data: {action: 'create', message: 'Số điện thoại đã tồn tại'}
      });
    } else if (this.savedUser.responseCode === 200 && this.savedUser.responseMessage === OK_002) {
      this.dialogRef.close();
      this.openSnackBar('Thêm mới tài khoản thành công');
    } else {
      console.log(this.savedUser.responseMessage);
    }
  }

  backToIndex(): void {
    this.submitted = false;
    this.createForm.reset();
  }

  get rfc() {
    return this.createForm.controls;
  }

  numberOnly(event): boolean {
    const charCode = (event.which) ? event.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
      return false;
    }
    return true;
  }

  onCheckboxChange(e): void {
    const listRoleId: FormArray = this.createForm.get('roleId') as FormArray;

    if (e.checked) {
      listRoleId.push(new FormControl(e.source.value));
    } else {
      const index = listRoleId.controls.findIndex(x => x.value === e.source.value);
      console.log(index);
      listRoleId.removeAt(index);
    }
    console.log(listRoleId);
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
