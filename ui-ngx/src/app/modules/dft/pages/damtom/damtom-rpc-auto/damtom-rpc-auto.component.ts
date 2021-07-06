import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {Subject} from 'rxjs';
import {DeviceRpc} from '@modules/dft/models/rpc/device-rpc.model';
import {MatDialog} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {ToastrService} from 'ngx-toastr';
import {RpcAlarmService} from '@modules/dft/service/rpc/rpc-alarm.service';
import {CreateRpcAutoDialogComponent} from '@modules/dft/pages/damtom/damtom-rpc-auto/create-rpc-auto-dialog/create-rpc-auto-dialog.component';
import {escapedHTML} from '@modules/dft/service/utils.service';
import {EditRpcAutoDialogComponent} from '@modules/dft/pages/damtom/damtom-rpc-auto/edit-rpc-auto-dialog/edit-rpc-auto-dialog.component';

@Component({
  selector: 'tb-damtom-rpc-auto',
  templateUrl: './damtom-rpc-auto.component.html',
  styleUrls: ['./damtom-rpc-auto.component.scss']
})
export class DamtomRpcAutoComponent implements OnInit, AfterViewInit {

  @Input() damtomId: string;

  @Input() rpcDeviceList: DeviceRpc[];

  // @ViewChild(MatPaginator) paginator: MatPaginator;
  // @ViewChild(MatSort) sort: MatSort;

  dataSource: MatTableDataSource<any>;
  isLoading$: Subject<boolean>;
  // displayedColumns: string[] = ['actions', 'name', 'ruler'];
  displayedColumns: string[] = ['actions', 'name', 'active', 'createdTime'];
  defaultPageSize = 10;
  displayPagination = true;
  pageSizeOptions = [10, 20, 30, 40, 50, 100];

  toanBoLuat: any[] = [];

  constructor(
    public dialog: MatDialog,
    private dialogService: DialogService,
    private toast: ToastrService,
    private rpcAlarmService: RpcAlarmService
  ) { }

  ngOnInit(): void {
    this.getData();
  }

  ngAfterViewInit() {
    // $( '.mat-paginator-range-actions' ).append( '<div class="new-div"></div>' );
    // $('.mat-paginator-range-actions >button').appendTo( $('.new-div') );
  }

  getData(){
    this.rpcAlarmService.getAllRpcAlarm(this.damtomId).subscribe(rs => {
      if (!!rs) {
        this.dataSource = new MatTableDataSource(rs);
        // this.dataSource.paginator = this.paginator;
        // this.dataSource.sort = this.sort;
      }
      // this.toanBoLuat = this.dataSource.data;
      this.toanBoLuat = rs;
    });
  }

  refresh() {
    this.ngOnInit();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(CreateRpcAutoDialogComponent, {
      data: {
        damtomId: this.damtomId
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  openEditDialog(element) {
    const dialogRef = this.dialog.open(EditRpcAutoDialogComponent, {
      data: {
        damtomId: this.damtomId,
        alarmType: element.alarmType,
        alarmId: element.id
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      this.refresh();
    });
  }

  deleteRpcAuto(alarmId: string, alarmType: string){
    this.dialogService.confirm(
      'Bạn có chắc chắn không?',
      'Điều khiển tự động ' + escapedHTML(alarmType) + ' sẽ bị xóa',
      'Hủy', 'Xóa',
      true
    ).subscribe((result) => {
      if (result) {
        this.rpcAlarmService.deleteRpcAlarm(this.damtomId, alarmId).subscribe(rs => {
            this.toast.success('Xóa thành công!', '', {
              positionClass: 'toast-bottom-right',
              timeOut: 3000,
            });
            this.refresh();
          },
          error => {
            if (error.status === 400){
              this.dialogService.alert('', error.message, 'ok', false);
            } else {
              this.dialogService.alert('', 'Hệ thống gặp lỗi không xác định', 'ok', false);
            }
          }
        );
      }
    });
  }

  getAlarmActive(element: any){
    return element.dftAlarmRule.active;
  }

}
