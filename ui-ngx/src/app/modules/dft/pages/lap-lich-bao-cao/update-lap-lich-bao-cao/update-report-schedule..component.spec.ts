import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateReportSchedule } from './update-report-schedule.component';

describe('DetailUsersDftComponent', () => {
  let component: UpdateReportSchedule;
  let fixture: ComponentFixture<UpdateReportSchedule>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UpdateReportSchedule ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateReportSchedule);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
