import {Component, Inject, OnInit} from '@angular/core';
import {Subject} from 'rxjs';
import {LuatService} from '@modules/dft/service/luat.service';
import {ToastrService} from 'ngx-toastr';
import {DialogService} from '@core/services/dialog.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {
  EditAlarmDialogComponent,
  EditAlarmDialogData
} from '@modules/dft/pages/damtom/damtom-luat/edit-alarm-dialog/edit-alarm-dialog.component';

@Component({
  selector: 'tb-detail-alarm-dialog',
  templateUrl: './detail-alarm-dialog.component.html',
  styleUrls: ['./detail-alarm-dialog.component.scss']
})
export class DetailAlarmDialogComponent implements OnInit {
  isLoading$: Subject<boolean>;

  constructor(
    private luatService: LuatService,
    private toastrService: ToastrService,
    private dialogService: DialogService,
    public dialogRef: MatDialogRef<EditAlarmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public dialogData: EditAlarmDialogData
  ) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
  }

  keyToLabel(key: string) {
    switch (key) {
      case 'Humidity':
        return 'Độ ẩm';
      case 'Luminosity':
        return 'Ánh sáng';
      default:
        return 'Nhiệt độ';
    }
  }

}
