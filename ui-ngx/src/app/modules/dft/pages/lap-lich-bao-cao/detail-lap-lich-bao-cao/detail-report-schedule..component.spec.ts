import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailReportSchedule } from './detail-report-schedule.component';

describe('DetailUsersDftComponent', () => {
  let component: DetailReportSchedule;
  let fixture: ComponentFixture<DetailReportSchedule>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DetailReportSchedule ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DetailReportSchedule);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
