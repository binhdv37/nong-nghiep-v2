import { Component, OnInit } from '@angular/core';
import { BcMultiData } from '@app/modules/dft/models/bao-cao/bao-cao.model';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Subject} from 'rxjs';
import {BcThongbaoService} from '@modules/dft/service/bc-thongbao/bc-thongbao.service';
import {TranslateService} from '@ngx-translate/core';
import {DialogService} from '@core/services/dialog.service';

@Component({
  selector: 'tb-bc-thongbao',
  templateUrl: './bc-thongbao.component.html',
  styleUrls: ['./bc-thongbao.component.scss']
})
export class BcThongbaoComponent implements OnInit {

  datas: BcMultiData[] = [];
  nameTable: string[] = ['Notification', 'Tin nhắn (SMS)', 'Email'];
  displayedColumns: string[] = ['Name', 'SoLanGuiThongBao', 'SoLanThanhCong', 'SoLanThatBai'];
  formTimeQuery: FormGroup;
  startTs: number;
  endTs: number;

  minDate = new Date(1900, 0, 1);
  maxDate = new Date();

  isLoading$: Subject<boolean>;

  constructor(
    private bcThongBaoService: BcThongbaoService,
    private dialogService: DialogService,
    private translate: TranslateService,
    private fb: FormBuilder
  ) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
    const x = new Date();
    x.setHours(0, 0, 0, 0);

    this.endTs = Date.now();
    this.startTs = x.getTime() - 345600000; // mac dinh load 5 ngay

    this.getDataByRangeTime(this.startTs, this.endTs);
    this.initForm();
  }

  initForm() {
    this.formTimeQuery = this.fb.group(
      {
        startRangeTime: [new Date(this.startTs)],
        endRangeTime: [new Date(this.endTs)]
      },
      { validator: this.checkTimeRange });
  }

  checkTimeRange(group: FormGroup) {
    const startRangeTime = Date.parse(group.controls.startRangeTime.value);
    const endRangeTime = Date.parse(group.controls.endRangeTime.value);

    return startRangeTime < endRangeTime
      ? group.controls.endRangeTime.setErrors(null)
      : group.controls.endRangeTime.setErrors({ timeRangeInvalid: true });
  }

  onSubmitForm() {
    this.startTs = Date.parse(this.formTimeQuery.get('startRangeTime').value);
    this.endTs = this.convertEndTs(Date.parse(this.formTimeQuery.get('endRangeTime').value));
    if (this.startTs > this.endTs) {
      this.dialogService.alert('Khoảng thời gian sai',
        'Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!',
        this.translate.instant('dft.admin.khachhang.yes'));
    } else {
      this.getDataByRangeTime(this.startTs, this.endTs);
    }
  }

  getDataByRangeTime(startTs: number, endTs: number) {
    this.isLoading$.next(true);
    this.bcThongBaoService.getBcThongBaoData(startTs, endTs).subscribe(
      res => {
        this.datas = res;
      },
      (err) => {
        console.log(err);
      },
      () => {
        this.isLoading$.next(false);
      }
    );
  }

  // convert endts time
  convertEndTs(endTs: number) {
    const x = new Date(endTs);
    x.setHours(24, 0, 0, 0); // set x = thời điểm 0h ngày hôm sau

    return x.getTime() > Date.now() ? Date.now() : x.getTime();
  }


}
