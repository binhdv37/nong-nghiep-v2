import {Component, OnInit} from '@angular/core';
import {BcDlgiamsatService} from '@modules/dft/service/bc-dlgiamsat/bc-dlgiamsat.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {DialogService} from '@core/services/dialog.service';
import {TranslateService} from '@ngx-translate/core';
import {Observable, Subject} from 'rxjs';
import {BcMultiData} from '@modules/dft/models/bao-cao/bao-cao.model';
import {TelemetryKey} from "@modules/dft/models/alarm-history/telemetryKey.constant";

export class MyLineChartDataMap {
  key: string;
  chartTitle: string;
  data: BcMultiData[];
}

@Component({
  selector: 'tb-bc-dlgiamsat',
  templateUrl: './bc-dlgiamsat.component.html',
  styleUrls: ['./bc-dlgiamsat.component.scss']
})
export class BcDlgiamsatComponent implements OnInit {

  dataMaps: MyLineChartDataMap[] = [
    {
      key: TelemetryKey.TEMPERATURE,
      chartTitle: 'Biểu đồ so sánh nhiệt độ giữa các nhà vườn',
      data: []
    },
    {
      key: TelemetryKey.HUMIDITY,
      chartTitle: 'Biểu đồ so sánh độ ẩm giữa các nhà vườn',
      data: []
    },
    {
      key: TelemetryKey.LUMINOSITY,
      chartTitle: 'Biểu đồ so sánh ánh sáng giữa các nhà vườn',
      data: []
    }
  ];

  formTimeQuery: FormGroup;
  startTs: number;
  endTs: number;

  minDate = new Date(1900, 0, 1);
  maxDate = new Date();
  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  constructor(
    private bcDlgiamsatService: BcDlgiamsatService,
    private diaglogService: DialogService,
    public translate: TranslateService,
    private fb: FormBuilder
  ) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
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
      {validator: this.checkTimeRange});
  }

  checkTimeRange(group: FormGroup) {
    const startRangeTime = Date.parse(group.controls.startRangeTime.value);
    const endRangeTime = Date.parse(group.controls.endRangeTime.value);
    return startRangeTime < endRangeTime ?
      group.controls.endRangeTime.setErrors(null)
      : group.controls.endRangeTime.setErrors({timeRangeInvalid: true});
  }

  onSubmitForm() {
    this.startTs = Date.parse(this.formTimeQuery.get('startRangeTime').value);
    this.endTs = this.convertEndTs(Date.parse(this.formTimeQuery.get('endRangeTime').value));
    if (this.startTs > this.endTs) {
      this.diaglogService.alert('Khoảng thời gian sai',
        'Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!',
        this.translate.instant('dft.admin.khachhang.yes'));
    } else {
      this.getDataByRangeTime(this.startTs, this.endTs);
    }
  }

  getDataByRangeTime(startTs: number, endTs: number) {
    this.mainSource$.next(true);
    for (let i = 0; i < this.dataMaps.length; i++) {
      this.bcDlgiamsatService.getBcDlgiamsatData(this.dataMaps[i].key, startTs, endTs).subscribe(
        resp => {
          this.dataMaps[i].data = resp;
        },
        (err) => {
          console.log(err);
        },
        () => {
          this.mainSource$.next(false);
        });
    }
  }

  // convert endts time
  convertEndTs(endTs: number) {
    const x = new Date(endTs);
    x.setHours(24, 0, 0, 0); // set x = thời điểm 0h ngày hôm sau

    return x.getTime() > Date.now() ? Date.now() : x.getTime();
  }


}
