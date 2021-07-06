import {Component, OnInit} from '@angular/core';
import {DamTomService} from '@modules/dft/service/damtom.service';
import {BcCanhbaoService} from '@modules/dft/service/bc-canhbao/bc-canhbao.service';
import {DamTom} from '@modules/dft/models/damtom.model';
import {Subject} from 'rxjs';
import {FormBuilder, FormGroup} from '@angular/forms';
import {DialogService} from '@core/services/dialog.service';
import {TranslateService} from '@ngx-translate/core';
import {BcSingleData} from '@modules/dft/models/bao-cao/bao-cao.model';

export class MyPieChartData {
  chartTitle: string;
  datas: BcSingleData[];
}

@Component({
  selector: 'tb-bc-canhbao',
  templateUrl: './bc-canhbao.component.html',
  styleUrls: ['./bc-canhbao.component.scss']
})
export class BcCanhbaoComponent implements OnInit {

  isLoading$: Subject<boolean>;
  isLoadingChart1$: Subject<boolean>;
  isLoadingChart2$: Subject<boolean>;

  // selected value - dam tom id
  selectedValue: any;

  // selected dam tom name
  selectedDamtomName = 'đầm tôm';

  damtomList: DamTom[] = [];


  formTimeQuery: FormGroup;
  startTs: number;
  endTs: number;

  minDate = new Date(1900, 0, 1);
  maxDate = new Date();


  chartDatas: MyPieChartData[] = [
    {
      chartTitle: 'Biểu đồ thống kê cảnh báo của đầm tôm',
      datas: []
    },
    {
      chartTitle: 'Biểu đồ thống kê cảnh báo giữa các đầm',
      datas: []
    }
  ];


  constructor(private damTomService: DamTomService,
              private bcCanhbaoService: BcCanhbaoService,
              private fb: FormBuilder,
              private diaglogService: DialogService,
              public translate: TranslateService
  ) {
    this.isLoading$ = new Subject<boolean>();
    this.isLoadingChart1$ = new Subject<boolean>();
    this.isLoadingChart2$ = new Subject<boolean>();
  }

  ngOnInit(): void {
    const x = new Date();
    x.setHours(0, 0, 0, 0);

    this.endTs = Date.now();
    this.startTs = x.getTime() - 345600000; // mac dinh load 5 ngay

    this.getAllDamtom();

    this.initForm();
  }

  getAllDamtom() {
    this.isLoading$.next(true);
    this.damTomService.getAllTenantActiveDamtom().subscribe(
      resp => {
        this.damtomList = resp;

        // set selected value :
        if (this.damtomList.length > 0) {
          this.selectedValue = this.damtomList[0].id;

          this.selectedDamtomName = this.damtomList[0].name;

          this.getDataByRangeTime(this.selectedValue, this.startTs, this.endTs);
        }

      },
      err => {
        console.log(err);
      },
      () => {
        this.isLoading$.next(false);
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

    this.changeChart1Title(this.selectedDamtomName);

    // get chart 1 data
    this.isLoadingChart1$.next(true);
    this.bcCanhbaoService.getDamtomBcCanhBaoData(damtomId, startTs, endTs).subscribe(
      resp => {
        this.chartDatas[0].datas = resp;
      },
      err => {
        console.log(err);
        this.isLoadingChart1$.next(false);
      },
      () => {
        this.isLoadingChart1$.next(false);
      }
    );


    // get chart 2 data
    this.isLoadingChart2$.next(true);
    this.bcCanhbaoService.getTenantBcCanhBaoData(startTs, endTs).subscribe(
      resp => {
        this.chartDatas[1].datas = resp;
      },
      err => {
        console.log(err);
        this.isLoadingChart2$.next(false);
      },
      () => {
        this.isLoadingChart2$.next(false);
      }
    );
  }

  changeChart1Title(damtomName: string){
    this.chartDatas[0].chartTitle = `Biểu đồ thống kê cảnh báo của ${damtomName}`;
  }

  changeSelectedDamtomName(damtomName: string){
    this.selectedDamtomName = damtomName;
  }

  // convert endts time
  convertEndTs(endTs: number) {
    const x = new Date(endTs);
    x.setHours(24, 0, 0, 0); // set x = thời điểm 0h ngày hôm sau

    return x.getTime() > Date.now() ? Date.now() : x.getTime();
  }
}
