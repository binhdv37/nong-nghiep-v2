import { X } from '@angular/cdk/keycodes';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { DialogService } from '@app/core/public-api';
import { TableAction } from '@app/modules/dft/models/action.model';
import { DamTom, ReportScheduleCreateOrUpdate } from '@app/modules/dft/models/lap-lich-bao-cao/ReportSchedule.model';
import { UsersDft } from '@app/modules/dft/models/usersDft/users-dft.model';
import { LapLichBaoCaoService } from '@app/modules/dft/service/lap-lich-bao-cao/lap-lich-bao-cao.service';
import { PageData } from '@shared/models/page/page-data';

import { PageLink } from '@shared/models/page/page-link';
import { Direction, SortOrder } from '@shared/models/page/sort-order';
import { ToastrService } from 'ngx-toastr';
import { Subject } from 'rxjs';

const ALL_DAM_TOM_UUID  = "5323da80-8205-47ac-9c9e-b4abf6958fbf";

@Component({
  selector: 'tb-edit-report-schedule',
  templateUrl: './create-report-schedule.component.html',
  styleUrls: ['./create-report-schedule.component.scss']
})
export class CreateReportSchedule implements OnInit {

  isLoading$: Subject<boolean>;
  hide = true;
  pageLink: PageLink;
  defaultPageSize = 50;
  sortOrder: SortOrder;
  createForm: FormGroup;
  submitted = false;
  damTomList: DamTom[];
  staffOfDamTom: UsersDft[] = [];
  AllStaff: UsersDft[] = [];
  isReceviedDay = false;
  newReportSchedule: ReportScheduleCreateOrUpdate = { active: true, cron: '', damTomId: '', reportName: '', scheduleName: '', users: [], note: '' };

  minutes = '0';
  hour = '22';
  day = '*';
  month = '*';
  dayWeek = '*'
  cronSchedule: string = '';

  DayInWeek = [
    { 'key': 1, 'value': 'Thứ 2' },
    { 'key': 2, 'value': 'Thứ 3' },
    { 'key': 3, 'value': 'Thứ 4' },
    { 'key': 4, 'value': 'Thứ 5' },
    { 'key': 5, 'value': 'Thứ 6' },
    { 'key': 6, 'value': 'Thứ 7' },
    { 'key': 0, 'value': 'Chủ nhật' }
  ]

  dayInMonth = [
    { 'key': 1, 'value': 'Ngày 1' },
    { 'key': 2, 'value': 'Ngày 2' },
    { 'key': 3, 'value': 'Ngày 3' },
    { 'key': 4, 'value': 'Ngày 4' },
    { 'key': 5, 'value': 'Ngày 5' },
    { 'key': 6, 'value': 'Ngày 6' },
    { 'key': 7, 'value': 'Ngày 6' },
    { 'key': 8, 'value': 'Ngày 6' },
    { 'key': 9, 'value': 'Ngày 6' },
    { 'key': 10, 'value': 'Ngày 10' },
    { 'key': 11, 'value': 'Ngày 11' },
    { 'key': 12, 'value': 'Ngày 12' },
    { 'key': 13, 'value': 'Ngày 13' },
    { 'key': 14, 'value': 'Ngày 14' },
    { 'key': 15, 'value': 'Ngày 15' },
    { 'key': 16, 'value': 'Ngày 16' },
    { 'key': 17, 'value': 'Ngày 17' },
    { 'key': 18, 'value': 'Ngày 18' },
    { 'key': 19, 'value': 'Ngày 19' },
    { 'key': 20, 'value': 'Ngày 20' },
    { 'key': 21, 'value': 'Ngày 21' },
    { 'key': 22, 'value': 'Ngày 22' },
    { 'key': 23, 'value': 'Ngày 23' },
    { 'key': 24, 'value': 'Ngày 24' },
    { 'key': 25, 'value': 'Ngày 25' },
    { 'key': 26, 'value': 'Ngày 26' },
    { 'key': 27, 'value': 'Ngày 27' },
    { 'key': 28, 'value': 'Ngày 28' },
    { 'key': 29, 'value': 'Ngày 29' },
    { 'key': 30, 'value': 'Ngày 30' },
    { 'key': 30, 'value': 'Ngày 31' }
  ]


  listReportEnableDamTomSelect = ['SYNTHESIS_REPORT', 'WARNING_REPORT', 'SENSOR_CONNECTION_REPORT'];
  ReportNameMap = [
    { key: 'WARNING_REPORT', value: 'Báo cáo cảnh báo' },
    { key: 'MONITORING_DATA_REPORT', value: 'Báo cáo dữ liệu giám sát' },
    { key: 'SENSOR_CONNECTION_REPORT', value: 'Báo cáo kết nối cảm biến' },
    { key: 'NOTIFICATION_DATA_REPORT', value: 'Báo cáo thông báo' },
    { key: 'SYNTHESIS_REPORT', value: 'Báo cáo tổng hợp' }
  ]

  constructor(private reportScheduleService: LapLichBaoCaoService,
    private formBuilder: FormBuilder,
    private toastrService: ToastrService,
    private dialogService : DialogService,
    private dialogRef: MatDialogRef<CreateReportSchedule>,
    private matDialog: MatDialog) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit(): void {
    this.fetchDataAllUser();
    this.createForm = this.formBuilder.group({
      reportScheduleName: ['', [Validators.required, Validators.maxLength(255), Validators.pattern('^(?!\\s*$).+')]],
      reportName: ['', [Validators.required, Validators.maxLength(320), Validators.pattern('^(?!\\s*$).+')]],
      damtom: ['', [Validators.required]], // sdt
      reportRecipients: ['', [Validators.required]],
      receivedhour: ['', [Validators.required]],
      enabled: [true],
      receivedDate: [],
      repeat: ['day', [Validators.required]],
      note: ['', [Validators.maxLength(4000),]]
    });
    this.sortOrder = {
      property: 'name',
      direction: Direction.ASC
    };
    // this.createForm.get('damtom').disable();
    this.pageLink = new PageLink(this.defaultPageSize, 0, null, this.sortOrder);
    this.intitData();
  }


  async onSubmit(): Promise<void> {
    this.isLoading$.next(true);
    this.submitted = true;
    if (this.createForm.valid) {

      let value = this.createForm.getRawValue();

      this.hour = value.receivedhour.split(':')[0];
      this.minutes = value.receivedhour.split(':')[1];
      if (value.repeat === 'day') {
        this.dayWeek = '*';
        this.month = '*';
        this.day = '*'
      }
      else if (value.repeat === 'month') {
        this.dayWeek = '*';
        this.month = '*';
        // if value = month  value.receivedDate = 1 -> 30
        this.day = value.receivedDate;
      }
      else if (value.repeat === 'week') {
        // if value = month  value.receivedDate = 1 -> 7
        this.dayWeek = value.receivedDate;
        this.month = '*';
        this.day = '*';
      }
      console.log(value);

      this.newReportSchedule.active = value.enabled;
      if(value.damtom !== undefined){
        if(value.damtom.id !== undefined){
          if(value.damtom.id.id !== undefined){
             this.newReportSchedule.damTomId = value.damtom.id.id ;
          }
        }
      }
      else{
        this.newReportSchedule.damTomId= ALL_DAM_TOM_UUID;
      }
      this.newReportSchedule.damTomId === "" ? this.newReportSchedule.damTomId=ALL_DAM_TOM_UUID : 0;
      this.newReportSchedule.note = value.note.trim();
      this.newReportSchedule.reportName = value.reportName;
      this.newReportSchedule.scheduleName = value.reportScheduleName.trim();
      if(this.newReportSchedule.damTomId != ALL_DAM_TOM_UUID){
        if (!!value.reportRecipients) {
          this.newReportSchedule.users = [];
          value.reportRecipients.forEach(recipient => {
            this.newReportSchedule.users.push(recipient.id);
          });
        }
      }
      else{
        if (!!value.reportRecipients) {


          this.newReportSchedule.users = [];
          value.reportRecipients.forEach(recipient => {
            console.log(recipient);

            this.newReportSchedule.users.push(recipient.id.id);
          });
        }
      }

      this.cronSchedule = `0 ${this.minutes} ${this.hour} ${this.day} ${this.month} ${this.dayWeek}`;
      this.newReportSchedule.cron = this.cronSchedule;
      console.log(this.newReportSchedule);

      let isExist = await this.checkNameExit(null,this.newReportSchedule.scheduleName);
      if(isExist){
        this.dialogService.alert('', "Tên lịch xuất báo cáo đã tồn tại", 'Ok', false);
        this.isLoading$.next(false);
        return;
      }
      this.reportScheduleService.createReportSchedule(this.newReportSchedule)
        .subscribe(rpSchedule => {
          console.log(rpSchedule);
          this.dialogRef.close(TableAction.ADD_ENTITY);
        },
        err=>{

        },
        ()=>{
          this.isLoading$.next(false);
        })
    }
  }

  checkResponse(response) {

  }

  backToIndex(): void {
    this.submitted = false;
    this.createForm.reset();
  }

  intitData() {
    this.fetchDamTomsData();
  }

  fetchDamTomsData() {
    this.isLoading$.next(true);
    this.pageLink
    this.reportScheduleService.getListDamTom(this.pageLink)
      .subscribe((pageData: PageData<any>) => {
        if (pageData.hasNext) {
          this.pageLink.pageSize += 100;
          this.fetchDamTomsData();
        } else {
          // this.damTomList = pageData.data.find(damtom=>damtom.active===true);
          this.damTomList = this.getListDamTomActive(pageData);
        }
      },
        err => {
          console.log(err);
        },
        ()=>{
          this.isLoading$.next(false);
        }
      )
  }

  getListDamTomActive(pageData) {
    let listDamTom = [];
    pageData.data.forEach(damtom => {
      if (damtom.active) {
        listDamTom.push(damtom);
      }
    });
    return listDamTom;
  }
  onChangeReportType(value) {
    console.log(value.value);

    if (!this.listReportEnableDamTomSelect.includes(value.value) && value.value !== "") {
      this.createForm.controls['damtom'].disable();
      this.createForm.get('damtom').setValue('');
      this.createForm.get('damtom').markAsPristine();
      this.fetchDataAllUser();
      return;
    }
    this.createForm.controls['damtom'].enable();

  }


  selectDamTomChange(value) {
    this.isLoading$.next(true);
    console.log("----------------------");
    console.log("change");
    this.createForm.controls['reportRecipients'].patchValue('');
    if (value.value === undefined) {

    } else {
      this.staffOfDamTom = [];
      let damtomSelected = value.value;
      console.log(damtomSelected);
      this.reportScheduleService.getDamTom(damtomSelected.id.id)
        .subscribe(damtom => {
          if (!!damtom && !!damtom.staffs) {
            damtom.staffs.forEach(staffs => {
              if(this.AllStaff.find(user=>user.id.id===staffs.staff.id)){
                this.staffOfDamTom.push(staffs.staff);
              }
              console.log(staffs);
            });
          }
        },
          err => {
          },
          ()=>{
            this.isLoading$.next(false);
          })
    }
  }


  userSortOrder = {
    property: 'createdTime',
    direction: Direction.DESC
  }

  userPageLink = new PageLink(100, 0, '', this.userSortOrder);
 fetchDataAllUser(){
  this.staffOfDamTom = [];
  this.reportScheduleService.getAllUsersDft(this.userPageLink).subscribe((pageData:PageData<UsersDft>)=>{
    if (pageData.hasNext) {
      this.pageLink.pageSize += 100;
      this.fetchDamTomsData();
    } else {
      this.staffOfDamTom = (pageData.data).filter(user=>user.enabled===true);
      this.AllStaff = this.staffOfDamTom;
    }
  },err=>{},
  ()=>{this.isLoading$.next(false);}
  )
 }

  dayData;
  selectrepeatChange(value) {
    if (value.value === 'day') {
      this.isReceviedDay = false;
      return;
    }
    this.isReceviedDay = true;
    value.value === "month" ? this.dayData = this.dayInMonth : this.dayData = this.DayInWeek;
  }

  checkNameExit(scheduleId,scheduleName){
    return new Promise(
      (reslove,reject)=>{
        this.reportScheduleService.isScheduleNameExist(scheduleId,scheduleName).subscribe(
          isExist=>{
            reslove(isExist);
          },
          err=>{reject(err)})

      }
    )

  }

}
