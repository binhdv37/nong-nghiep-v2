import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateReportSchedule } from './create-report-schedule.component';

describe('CreateUsersDftComponent', () => {
  let component: CreateReportSchedule;
  let fixture: ComponentFixture<CreateReportSchedule>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateReportSchedule ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateReportSchedule);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
