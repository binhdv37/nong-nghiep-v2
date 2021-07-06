import {Component, OnInit} from '@angular/core';
import {BaoCaoKetNoiService} from '@modules/dft/service/bc-ketnoi/bc-ket-noi.service';
import moment from 'moment';
import {Observable, Subject} from 'rxjs';
import {DamTom} from '@modules/dft/models/damtom.model';
import {MyPieChartData} from '@modules/dft/pages/baocao/bc-canhbao/bc-canhbao.component';
import {TranslateService} from '@ngx-translate/core';
import {BcDashboardService} from '@modules/dft/service/bc-dashboard/bc-dashboard.service';
import {MatTableDataSource} from '@angular/material/table';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {DialogService} from '@core/services/dialog.service';

@Component({
  selector: 'tb-bc-ketnoi',
  templateUrl: './bc-ketnoi.component.html',
  styleUrls: ['./bc-ketnoi.component.scss']
})
export class BcKetnoiComponent implements OnInit {
  selectedValue: any = 0;

  dataSource = new MatTableDataSource<any>();

  damtomList: DamTom[] = [];
  selectedDamtomName = 'Toàn bộ đầm';

  startTime: number;
  endTime: number;
  formDateRange: FormGroup;

  minDate = new Date(1900, 0, 1);
  maxDate = new Date();

  mainSource$: Subject<boolean>;
  isLoading$: Observable<boolean>;

  chartDatas: MyPieChartData[] = [
    {
      chartTitle: this.translate.instant('dft.bc-ketnoi.chart-title'),
      datas: []
    }
  ];

  constructor(private baoCaoKetNoiService: BaoCaoKetNoiService,
              private bcDashboardService: BcDashboardService,
              private fb: FormBuilder,
              private diaglogService: DialogService,
              public translate: TranslateService) {
    this.mainSource$ = new Subject<boolean>();
    this.isLoading$ = this.mainSource$.asObservable();
  }

  ngOnInit() {
    this.startTime = moment(Date.now()).subtract(4, 'days').startOf('day').valueOf();
    this.endTime = moment(Date.now()).endOf('day').valueOf();
    this.initForm();
    this.getAllDamtom();
  }

  initForm() {
    this.formDateRange = this.fb.group(
        {
          startRangeTime: new FormControl(moment(this.startTime)),
          endRangeTime: new FormControl(moment(this.endTime), Validators.required),
        },
        { validator: this.checkTimeRange });
  }


  checkTimeRange(group: FormGroup) {
    const startRangeTime = moment(Date.parse(group.controls.startRangeTime.value)).startOf('day').valueOf();
    const endRangeTime = moment(Date.parse(group.controls.endRangeTime.value)).endOf('day').valueOf();
    if ((Number.isNaN(endRangeTime) && Number.isNaN(startRangeTime))) {
      return group.controls.endRangeTime.setErrors(null);
    }
    return startRangeTime < endRangeTime ?
        group.controls.endRangeTime.setErrors(null)
        : group.controls.endRangeTime.setErrors({ timeRangeInvalid: true });
  }

  onSubmitForm() {
    this.startTime = moment(Date.parse(this.formDateRange.get('startRangeTime').value)).startOf('day').valueOf();
    this.endTime = moment(Date.parse(this.formDateRange.get('endRangeTime').value)).endOf('day').valueOf();
    if (this.startTime > this.endTime) {
      this.diaglogService.alert('Khoảng thời gian sai',
          'Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!',
          this.translate.instant('dft.admin.khachhang.yes'));
    } else {
      this.getChartDataByRangeTime(this.selectedValue, this.startTime, this.endTime);
      this.getTableDataByRangeTime(this.selectedValue, this.startTime, this.endTime);
    }
  }

  getAllDamtom() {
    this.mainSource$.next(true);
    this.bcDashboardService.getListDamTomActive().subscribe(
      resp => {
        this.damtomList = resp;
        this.getChartDataByRangeTime(null, this.startTime, this.endTime);
        this.getTableDataByRangeTime(null, this.startTime, this.endTime);
      },
      err => {
        console.log(err);
      },
      () => {
        this.mainSource$.next(false);
      }
    );
  }

  getChartDataByRangeTime(damtomId: string, startTs: number, endTs: number) {
    if (Number.isNaN(endTs)) {
      return;
    }
    this.mainSource$.next(true);
    this.baoCaoKetNoiService.getChartBcKetNoi(damtomId, startTs, endTs).subscribe(
      resp => {
        this.chartDatas[0].datas = resp;
      },
      err => {
        console.log(err);
      },
      () => {
        this.mainSource$.next(false);
      }
    );
  }

  getTableDataByRangeTime(damtomId: string, startTs: number, endTs: number) {
    if (Number.isNaN(endTs)) {
      return;
    }
    this.mainSource$.next(true);
    this.dataSource.data = [];
    this.baoCaoKetNoiService.getTableBcKetNoi(damtomId, startTs, endTs).subscribe(
      resp => {
        if (resp.length > 0) {
          this.dataSource.data = resp;
        }
      },
      err => {
        console.log(err);
      },
      () => {
        this.mainSource$.next(false);
      }
    );
  }

  changeSelectedDamtomName(damtomName: string){
    this.selectedDamtomName = damtomName;
    this.getChartDataByRangeTime(this.selectedValue, this.startTime, this.endTime);
    this.getTableDataByRangeTime(this.selectedValue, this.startTime, this.endTime);
  }

}
