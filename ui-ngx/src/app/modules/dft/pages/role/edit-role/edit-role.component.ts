import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {TranslateService} from '@ngx-translate/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Subject} from 'rxjs';
import {catchError, finalize, map, tap} from 'rxjs/operators';
import {TableAction} from '@modules/dft/models/action.model';
import {RoleService} from '@modules/dft/service/role.service';
import {Role} from '@modules/dft/models/role.model';
import {DialogService} from '@core/services/dialog.service';

export interface EditRoleDialogData {
  tabIndex: number; // tab mac dinh dc hien thi khi khoi tao ( 0 - tab1 ; 1 - tab2 )
  id: string;  // id cua role
  name: string; // ten role
  action: string; // TableAction.ADD, EDIT, DETAILS,...
}

export interface InitPermission {
  name: string;
  completed: boolean;
  child?: InitPermission[];
}

export interface CustomMap{
  key: string;
  value: string[];
}

@Component({
  selector: 'tb-edit-role',
  templateUrl: './edit-role.component.html',
  styleUrls: ['./edit-role.component.scss']
})
export class EditRoleComponent implements OnInit, OnDestroy {

  entityForm: FormGroup;
  isEditEntity = false;
  isAddEntity = false;
  isDetailEntity = false;

  isLoading$: Subject<boolean>;

  role: Role;
  roleId: string;
  titleForm: string;

  // binhdv
  // cac gia tri mac dinh :
  // key-permissions map data
  myMap: CustomMap[] =  [
    {key : 'QL_TK', value:  ['PAGES.USERS', 'PAGES.USERS.CREATE', 'PAGES.USERS.EDIT', 'PAGES.USERS.DELETE', 'PAGES.USERS.ACCESS.HISTORY']},
    {key: 'QL_VAITRO', value:  ['PAGES.ROLES', 'PAGES.ROLES.CREATE', 'PAGES.ROLES.EDIT', 'PAGES.ROLES.DELETE']},
    {key: 'QL_DAMTOM', value: ['']},
    {key: 'QL_TT_CHUNG', value: ['PAGES.DAMTOM', 'PAGES.DAMTOM.CREATE', 'PAGES.DAMTOM.EDIT', 'PAGES.DAMTOM.DELETE']},
    {key: 'GIAM_SAT', value: ['PAGES.DAMTOM', 'PAGES.GIAMSAT']},
    {key: 'DIEU_KHIEN', value: ['PAGES.DAMTOM', 'PAGES.DIEUKHIEN']},
    {key: 'DL_CAM_BIEN', value: ['PAGES.DAMTOM', 'PAGES.DLCAMBIEN']},
    {key: 'TL_LUATCB', value: ['PAGES.DAMTOM', 'PAGES.TLLUATCANHBAO']},
    {key: 'QL_CAMERA', value: ['PAGES.DAMTOM', 'PAGES.QLCAMERA', 'PAGES.QLCAMERA.CREATE', 'PAGES.QLCAMERA.EDIT', 'PAGES.QLCAMERA.DELETE']},
    {key: 'QL_THIETBI', value: ['PAGES.DAMTOM', 'PAGES.QLTHIETBI', 'PAGES.QLTHIETBI.CREATE', 'PAGES.QLTHIETBI.EDIT', 'PAGES.QLTHIETBI.DELETE']},
    {key: 'DATLICH_XUATBC', value: ['PAGES.REPORT.SCHEDULE', 'PAGES.REPORT.SCHEDULE.CREATE', 'PAGES.REPORT.SCHEDULE.EDIT', 'PAGES.REPORT.SCHEDULE.DELETE']},
    {key: 'BAO_CAO', value: ['']},
    {key: 'BC_TONGHOP', value: ['PAGES.BAOCAO', 'PAGES.BC_TONGHOP']},
    {key: 'BC_DLGIAMSAT', value: ['PAGES.BAOCAO', 'PAGES.BC_DLGIAMSAT']},
    {key: 'BC_CANHBAO', value: ['PAGES.BAOCAO', 'PAGES.BC_CANHBAO']},
    {key: 'BC_KETNOI_CAMBIEN', value: ['PAGES.BAOCAO', 'PAGES.BC_KETNOI_CAMBIEN']},
    {key: 'BC_GUITT_CB', value: ['PAGES.BAOCAO', 'PAGES.BC_GUI_TTCB']}
  ];

  initPermissions: InitPermission[] = [
    {
      name: 'QL_TK',
      completed: false
    },
    {
      name: 'QL_VAITRO',
      completed: false,
    },
    {
      name: 'QL_DAMTOM',
      completed: false,
      child: [
        {name: 'QL_TT_CHUNG', completed: false},
        {name: 'GIAM_SAT', completed: false},
        {name: 'DIEU_KHIEN', completed: false},
        {name: 'DL_CAM_BIEN', completed: false},
        {name: 'TL_LUATCB', completed: false},
        {name: 'QL_CAMERA', completed: false},
        {name: 'QL_THIETBI', completed: false},
      ]
    },
    {
      name: 'DATLICH_XUATBC',
      completed: false,
    },
    {
      name: 'BAO_CAO',
      completed: false,
      child: [
        {name: 'BC_TONGHOP', completed: false},
        {name: 'BC_DLGIAMSAT', completed: false},
        {name: 'BC_CANHBAO', completed: false},
        {name: 'BC_KETNOI_CAMBIEN', completed: false},
        {name: 'BC_GUITT_CB', completed: false},
      ]
    }
  ];

  // permissions nhận đc của role
  receivedPermissions: string[] = [];

  // permissions để update, có đc sau khi lọc keyArr
  updatePermissions: string[] = [];

  // mảng các key đc tích
  keyArr: string[] = []; // mảng các key đc tích

  // check xem mảng arr có contain tất cả element của mảng target hay k
  checker = (arr, target) => target.every(v => arr.includes(v));

  updateCheckedKeys(value: string): void{
    // update key arr :
    if (this.keyArr.includes(value)){
      this.keyArr = this.keyArr.filter(x => x !== value);
    } else{
      this.keyArr.push(value);
    }
  }

  constructor(protected store: Store<AppState>,
              protected translate: TranslateService,
              protected fb: FormBuilder,
              protected roleService: RoleService,
              private dialogService: DialogService,
              public dialogRef: MatDialogRef<EditRoleComponent>,
              @Inject(MAT_DIALOG_DATA) public data: EditRoleDialogData) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit() {
    this.buildForm();
    this.titleForm = this.getTitleAndView();
    if (this.data.id) {
      this.roleId = this.data.id;
    }
    if (this.roleId && this.roleId.length > 0) {
      this.initFormData(this.roleId);
    } else{
      this.role = {
        name: '',
        note: '',
        permissions: []
      };
    }
  }


  buildForm() {
    this.entityForm = this.fb.group(
      {
        name: ['', Validators.compose([Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')])],
        note: ['', Validators.compose([Validators.maxLength(4000)])]
      }
    );
  }


  initFormData(id: string) {
    this.isLoading$.next(true);
    this.roleService.getRoleById(id)
      .pipe(
        map((data: Role) => {
          this.role = data;
          this.entityForm.patchValue({ name: data.name });
          this.entityForm.patchValue({ note: data.note });

          // binhdv
          // init permission list
          for (const p of this.role.permissions){
            this.receivedPermissions.push(p.name);
          }

          // khởi tạo mảng các key đc tích : keyArr.
          for (const m of this.myMap){
            if (m.value.every(v => this.receivedPermissions.includes(v))) {
              this.keyArr.push(m.key);
            }
          }

          // thay đổi giá trị các trường completed trong initPermissions cho phù hợp với các key khởi tạo :
          for (const p of this.initPermissions){
            if (this.keyArr.includes(p.name)) { p.completed = true; }
            if (p.child !== null && p.child !== undefined) {
              for (const c of p.child) {
                if (this.keyArr.includes(c.name)) { c.completed = true; }
              }
              // check xem all child.completed == true => father.completed == true
              if (p.child.every(x => x.completed === true)) { p.completed = true; }
            }
          }
        }),
        finalize(() => {
          this.isLoading$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          return null;
        })
      ).subscribe();
  }

  onSubmitForm() {
    this.role.name = this.entityForm.get('name').value;
    this.role.note = this.entityForm.get('note').value;
    // binhdv
    // update giá trị cho updatePermissions dựa vào keyArr
    for (const k of this.keyArr){
      // array permission ung vs key :
      for (const m of this.myMap){
        if (m.key === k) {
          this.updatePermissions = this.updatePermissions.concat(m.value);
          break;
        }
      }

    }

    // loại bỏ các permission trùng tên:
    this.updatePermissions = this.removeDuplicate(this.updatePermissions);

    // update giá trị cho this.role.permissions dựa vào updatePermissions :
    this.role.permissions = [];
    this.updatePermissions
      .forEach( p => {
        this.role.permissions.push({
          name: p
        });
      });
    this.save(this.role);
  }


  removeDuplicate(arr){
    const unique  = [];
    arr.forEach(element => {
      if (!unique.includes(element)){
        unique.push(element);
      }
    });
    return unique;
  }


  save(role: Role) {
    this.isLoading$.next(true);
    this.roleService.saveRole(role)
      .pipe(
        tap((data: Role) => {
          if (data.errorInfo !== null && data.errorInfo !== undefined){
            // save khong thanh cong
            this.dialogService.alert('', data.errorInfo.message, 'Ok', false);
            return;
          }else if (this.roleId === null || this.roleId === undefined){
            // Them mới thành công
            console.log('save success');
            this.dialogRef.close(TableAction.ADD_ENTITY);
          } else {
            // cập nhật thành công
            console.log('update success');
            this.dialogRef.close(TableAction.EDIT_ENTITY);
          }
        }),
        finalize(() => {
          this.isLoading$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          return null;
        })
      ).subscribe();
  }

  ngOnDestroy(): void {
    this.isLoading$.next(false);
  }


  getTitleAndView() {
    let result = this.translate.instant('dft.role.dialog.add-title');
    if (!this.data.id && this.data.action === TableAction.ADD_ENTITY) {
      this.isAddEntity = true;
      return result;
    }

    if (this.data.id && this.data.action === TableAction.EDIT_ENTITY) {
      result = this.translate.instant('dft.role.dialog.update-title') + ' ' + this.data.name;
      this.isEditEntity = true;
      return result;
    }

    if (this.data.id && this.data.action === TableAction.DETAIL_ENTITY) {
      result = this.translate.instant('dft.role.dialog.detail-title') + ' ' + this.data.name;
      this.isDetailEntity = true;
      const controls = this.entityForm.controls;
      controls.name.disable();
      controls.note.disable();
      return result;
    }
  }
}
