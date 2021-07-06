import { Component, OnInit } from '@angular/core';
import {DamTom} from '@modules/dft/models/damtom.model';
import {FormBuilder, FormGroup} from '@angular/forms';
import {DialogService} from '@core/services/dialog.service';
import {TranslateService} from '@ngx-translate/core';
import {Observable, Subject} from 'rxjs';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {BcTonghopService} from '@modules/dft/service/bc-tonghop/bc-tonghop.service';
import {BcSingleData} from '@modules/dft/models/bao-cao/bao-cao.model';
import {MyLineChartDataMap} from '@modules/dft/pages/baocao/bc-dlgiamsat/bc-dlgiamsat.component';
import {TelemetryKey} from "@modules/dft/models/alarm-history/telemetryKey.constant";

@Component({
  selector: 'tb-bc-tonghop',
  templateUrl: './bc-tonghop.component.html',
  styleUrls: ['./bc-tonghop.component.scss']
})
export class BcTonghopComponent implements OnInit {

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  // default seleted value - get data of all damtom
  defaultSelectedValue = 'ALL';

  // selected value - dam tom id
  selectedValue: any = 'ALL';

  damtomList: DamTom[] = [];

  formTimeQuery: FormGroup;
  startTs: number;
  endTs: number;

  minDate = new Date(1900, 0, 1);
  maxDate = new Date();

  // data - bar chart
  myBarChartDatas: BcSingleData[] = [];
  myBarChartTitle = 'Biểu đồ tổng số lần cảnh báo';

  // data - line chart
  myLineChartDatas: MyLineChartDataMap[] = [
    {
      key: TelemetryKey.HUMIDITY,
      chartTitle: 'Biểu đồ độ ẩm trung bình',
      data: []
    },
    {
      key: TelemetryKey.LUMINOSITY,
      chartTitle: 'Biểu đồ cường độ ánh sáng trung bình',
      data: []
    },
    {
      key: TelemetryKey.TEMPERATURE,
      chartTitle: 'Biểu đồ nhiệt độ trung bình',
      data: []
    }
  ];

  constructor(
    private diaglogService: DialogService,
    public translate: TranslateService,
    private fb: FormBuilder,
    private damTomService: DamTomService,
    private bcTonghopService: BcTonghopService
  ) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit(): void {
    const x = new Date();
    x.setHours(0, 0, 0, 0);

    this.endTs = Date.now();
    this.startTs = x.getTime() - 345600000; // mac dinh load 5 ngay

    this.getDataByRangeTime(this.selectedValue, this.startTs, this.endTs);

    this.getAllDamtom();

    this.initForm();
  }

  getAllDamtom() {
    this.mainSource$.next(true);
    this.damTomService.getAllTenantActiveDamtom().subscribe(
      resp => {
        this.damtomList = resp;

        // set selected value :
        // if (this.damtomList.length > 0) {
        //   this.selectedValue = this.damtomList[0].id;
        //
        //   this.getDataByRangeTime(this.selectedValue, this.startTs, this.endTs);
        // }

      },
      err => {
        console.log(err);
      },
      () => {
        this.mainSource$.next(false);
      }
    );
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
      this.getDataByRangeTime(this.selectedValue, this.startTs, this.endTs);
    }
  }

  getDataByRangeTime(damtomId: string, startTs: number, endTs: number) {
    this.getDamtomCanhBaoData(damtomId, startTs, endTs);
    this.getDamtomKeyDlcambienData(damtomId, startTs, endTs);
  }

  // get data for bar chart
  getDamtomCanhBaoData(damtomId: string, startTs: number, endTs: number) {
    this.mainSource$.next(true);
    this.bcTonghopService.getDamtomCanhBaoData(damtomId, startTs, endTs).subscribe(
      resp => {
        this.myBarChartDatas = resp;
      },
      error => {
        console.log(error);
      },
      () => {
        this.mainSource$.next(false);
      }
    );
  }

  // get data for line chart
  getDamtomKeyDlcambienData(damtomId: string, startTs: number, endTs: number) {
    for (const x of this.myLineChartDatas) {
      this.mainSource$.next(true);
      this.bcTonghopService.getDamtomKeyDlcambienData(damtomId, x.key, startTs, endTs).subscribe(
        resp => {
          x.data = resp;
        },
        error => {
          console.log(error);
        },
        () => {
          this.mainSource$.next(false);
        }
      );
    }
  }

  // convert endts time
  convertEndTs(endTs: number) {
    const x = new Date(endTs);
    x.setHours(24, 0, 0, 0); // set x = thời điểm 0h ngày hôm sau

    return x.getTime() > Date.now() ? Date.now() : x.getTime();
  }

}
