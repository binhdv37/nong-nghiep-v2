import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {TranslateService} from '@ngx-translate/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {ToastrService} from 'ngx-toastr';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Observable, of, Subject} from 'rxjs';
import {TableAction} from '@modules/dft/models/action.model';
import {RpcSettingComponent} from '@modules/dft/pages/damtom/damtom-group-rpc/damtom-group-rpc-edit/rpc-setting/rpc-setting.component';
import {catchError, finalize, tap} from 'rxjs/operators';
import {DeviceRpcService} from '@modules/dft/service/rpc/device-rpc.service';
import {DeviceSetting, GroupRpc} from '@modules/dft/models/rpc/group-rpc.model';
import {MatTable} from '@angular/material/table';
import {DeviceRpc} from '@modules/dft/models/rpc/device-rpc.model';
import {cloneDeep} from 'lodash';
import {deepClone} from '@core/utils';
import {SelectionModel} from '@angular/cdk/collections';
import {GroupRpcValidators} from '@modules/dft/service/rpc/group-rpc.validator';
import {DialogService} from '@core/services/dialog.service';


export interface DialogData {
  id: string;
  damTomId: string;
  name: string;
  action: string;
  deviceRpcList: DeviceRpc[];
}

@Component({
  selector: 'tb-damtom-group-rpc-edit',
  templateUrl: './damtom-group-rpc-edit.component.html',
  styleUrls: ['./damtom-group-rpc-edit.component.scss']
})
export class DamtomGroupRpcEditComponent implements OnInit {

  @ViewChild(MatTable, {static: true}) table: MatTable<any>;

  public get tableAction(): typeof TableAction {
    return TableAction;
  }

  entityForm: FormGroup;
  isEditEntity = false;
  isAddEntity = false;
  isDetailsEntity = false;

  damTomId: string;
  groupRpcId: string;
  groupRpc: GroupRpc;
  titleForm: string;
  deviceRpcList: DeviceRpc[];


  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  dataSource: DeviceSetting[] = [];
  selection = new SelectionModel<DeviceSetting>(true, []);
  displayedColumns: string[] = ['select', 'deviceName', 'valueControl',
    'delayTime', 'timeCallback', 'loopTimeStep', 'loopCount'];

  constructor(private groupRpcService: DeviceRpcService,
              private groupRpcValidators: GroupRpcValidators,
              protected store: Store<AppState>,
              protected translate: TranslateService,
              protected fb: FormBuilder,
              private toastService: ToastrService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              public dialogRef: MatDialogRef<DamtomGroupRpcEditComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    console.log(this.data);
    this.deviceRpcList = cloneDeep(this.data.deviceRpcList);
    this.buildForm();
    this.titleForm = this.getTitleAndView();
    this.damTomId = this.data.damTomId;
    if (!!this.data.id) {
      this.groupRpcId = this.data.id;
    }
    if (!!this.groupRpcId && this.groupRpcId.length > 0) {
      console.log(this.data);
      this.initFormData(this.groupRpcId);
    }
  }

  buildForm() {
    this.entityForm = this.fb.group(
      {
        name: new FormControl('', [Validators.required, Validators.maxLength(255)],
          !!this.data.id ? this.groupRpcValidators.editGroupRpcNameValidator(this.data.damTomId, this.data.id)
            : this.groupRpcValidators.groupRpcNameValidator(this.data.damTomId))
      }
    );
  }

  initFormData(id: string) {
    this.mainSource$.next(true);
    this.groupRpcService.getGroupRpcById(id)
      .pipe(
        tap((data: GroupRpc) => {
          this.groupRpc = data;
          this.entityForm.patchValue({name: data.name});
          this.dataSource = data.rpcSettingList;
        }),
        finalize(() => {
          this.mainSource$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          return of({});
        })
        // tslint:disable-next-line: deprecation
      ).subscribe();
  }

  onSubmitForm() {
    this.groupRpc = this.entityForm.getRawValue();
    this.groupRpc.damTomId = this.data.damTomId;
    this.groupRpc.rpcSettingList = this.dataSource;
    if (this.groupRpc.rpcSettingList == null || this.groupRpc.rpcSettingList.length === 0) {
      this.dialogService.alert('Thao tác điều khiển chưa được thiết lập!',
        'Bạn cần thiết lập điều khiển cho bộ điều khiển',
        this.translate.instant('dft.admin.khachhang.yes'));
      return;
    }

    if (!!this.groupRpcId && this.groupRpcId.length > 0) {
      this.groupRpc.groupRpcId = this.groupRpcId;
      this.save(this.data.damTomId, this.groupRpc, TableAction.EDIT_ENTITY);
    } else {
      this.save(this.data.damTomId, this.groupRpc, TableAction.ADD_ENTITY);
    }
  }

  save(damtomId: string, groupRpc: GroupRpc, action: string) {
    this.groupRpcService.saveGroupRpc(groupRpc)
      .pipe(
        tap((data: GroupRpc) => {
          this.dialogRef.close(action);
        }),
        finalize(() => {
          this.mainSource$.next(false);
        }),
        catchError((error) => {
          console.log(error);
          if (action === TableAction.ADD_ENTITY) {
            const message = this.translate.instant('dft.group-rpc.create-failed');
            this.openSnackBar(message);
          } else if (action === TableAction.EDIT_ENTITY) {
            const message = this.translate.instant('dft.group-rpc.update-failed');
            this.openSnackBar(message);
          }
          return of({});
        })
        // tslint:disable-next-line: deprecation
      ).subscribe();
  }

  getTitleAndView() {
    let result = this.translate.instant('dft.group-rpc.add');
    if (!this.data.id && this.data.action === TableAction.ADD_ENTITY) {
      this.isAddEntity = true;
      return result;
    } else if (this.data && this.data.action === TableAction.EDIT_ENTITY) {
      result = this.translate.instant('dft.group-rpc.update', {0: this.data.name});
      this.isEditEntity = true;
      return result;
    } else if (this.data && this.data.action === TableAction.DETAIL_ENTITY) {
      result = result = this.translate.instant('dft.group-rpc.details', {0: this.data.name});
      this.isDetailsEntity = true;
      const controls = this.entityForm.controls;
      controls.name.disable();
      // controls.ghiChu.disable();
      return result;
    }
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.length;
    return numSelected === numRows;
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.forEach(row => this.selection.select(row));
  }

  deleteSelection() {
    this.selection.selected.forEach(selection => {
      const numberIndex = this.dataSource.indexOf(selection);
      this.dataSource.splice(numberIndex, 1);
    });
    this.selection.clear();
    this.table.renderRows();
  }

  openEditDialog(deviceRpcList: DeviceRpc[], dataSource: DeviceSetting[]): void {
    const rpcDeviceListClone = deepClone(deviceRpcList);
    if (dataSource !== null && dataSource.length > 0) {
      dataSource.forEach(rpcSetting => {
        rpcDeviceListClone.forEach(deviceRpc => {
          if (deviceRpc.deviceId === rpcSetting.deviceId) {
            const numberIndex = rpcDeviceListClone.indexOf(deviceRpc);
            rpcDeviceListClone.splice(numberIndex, 1);
          }
        });
      });
    }

    const dialogRef = this.dialog.open(RpcSettingComponent, {
      data: {deviceRpcList: rpcDeviceListClone}
    });

    dialogRef.afterClosed().subscribe((result: DeviceSetting) => {
      if (result === undefined || !result.hasOwnProperty('deviceId')) {
        return;
      } else if (result.deviceId.length > 0 || result.deviceId !== null) {
        this.dataSource.push(result);
        this.table.renderRows();
      }
    });
  }

  openSnackBar(message: string) {
    this.toastService.error(message, null, {
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      positionClass: 'toast-bottom-right',
    });
  }

  getFormathours(input): string {
    // tslint:disable-next-line:one-variable-per-declaration
    let totalHours, totalMinutes, totalSeconds, hours, minutes, seconds;
    totalSeconds = input / 1000;
    totalMinutes = totalSeconds / 60;
    totalHours = totalMinutes / 60;

    seconds = Math.floor(totalSeconds) % 60;
    minutes = Math.floor(totalMinutes) % 60;
    hours = Math.floor(totalHours) % 60;

    if ( 0 <= hours && hours <= 9) {
      hours = '0' + hours;
    }
    if ( 0 <= minutes && minutes <= 9) {
      minutes = '0' + minutes;
    }
    if ( 0 <= seconds && seconds <= 9) {
      seconds = '0' + seconds;
    }
    return hours + ' giờ ' + minutes + ' phút ' + seconds + ' giây';
  }

}
