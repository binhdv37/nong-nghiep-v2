import { TestBed } from '@angular/core/testing';

import { UsersDftService } from './users-dft.service';

describe('UsersDftService', () => {
  let service: UsersDftService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UsersDftService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
