import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckBoxTreeComponent } from './check-box-tree.component';

describe('CheckBoxTreeComponent', () => {
  let component: CheckBoxTreeComponent;
  let fixture: ComponentFixture<CheckBoxTreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CheckBoxTreeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckBoxTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
