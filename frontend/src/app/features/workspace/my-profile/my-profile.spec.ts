import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { MyProfile } from './my-profile';
import { OrganizationApiService } from '../../../core/services/organization-api.service';
import { EmployeeResponse } from '../../../core/models/organization.model';

const employee: EmployeeResponse = {
  id: 'e1',
  organizationId: 'o1',
  userId: 'u1',
  email: 'agent1@nexusone.dev',
  jobTitle: 'Support Agent',
  departmentId: 'd1',
  teamId: null,
  managerId: 'e2',
  createdAt: '2026-01-01T00:00:00Z',
};

const manager: EmployeeResponse = { ...employee, id: 'e2', email: 'manager@nexusone.dev', managerId: null };

describe('MyProfile', () => {
  let getMyEmployee: ReturnType<typeof vi.fn>;
  let listDepartments: ReturnType<typeof vi.fn>;
  let getEmployee: ReturnType<typeof vi.fn>;
  let getDirectReports: ReturnType<typeof vi.fn>;

  function configure(): void {
    TestBed.configureTestingModule({
      imports: [MyProfile],
      providers: [
        {
          provide: OrganizationApiService,
          useValue: { getMyEmployee, listDepartments, getEmployee, getDirectReports },
        },
      ],
    });
  }

  beforeEach(() => {
    getMyEmployee = vi.fn().mockReturnValue(of(employee));
    listDepartments = vi.fn().mockReturnValue(of([{ id: 'd1', organizationId: 'o1', name: 'Support', createdAt: '' }]));
    getEmployee = vi.fn().mockReturnValue(of(manager));
    getDirectReports = vi.fn().mockReturnValue(of([]));
  });

  it('loads the current employee, their department, manager, and direct reports', () => {
    configure();
    const fixture = TestBed.createComponent(MyProfile);
    fixture.detectChanges();

    expect(getMyEmployee).toHaveBeenCalled();
    expect(fixture.componentInstance['employee']()).toEqual(employee);
    expect(listDepartments).toHaveBeenCalledWith('o1');
    expect(fixture.componentInstance['departmentName']()).toBe('Support');
    expect(getEmployee).toHaveBeenCalledWith('e2');
    expect(fixture.componentInstance['manager']()).toEqual(manager);
    expect(getDirectReports).toHaveBeenCalledWith('e1');
  });

  it('sets notFound when the API returns a 404', () => {
    getMyEmployee = vi.fn().mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    configure();
    const fixture = TestBed.createComponent(MyProfile);
    fixture.detectChanges();

    expect(fixture.componentInstance['notFound']()).toBe(true);
    expect(fixture.componentInstance['loading']()).toBe(false);
  });

  it('sets a generic error on non-404 failures', () => {
    getMyEmployee = vi.fn().mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    configure();
    const fixture = TestBed.createComponent(MyProfile);
    fixture.detectChanges();

    expect(fixture.componentInstance['error']()).toBe('Failed to load your profile. Is organization-service running?');
  });
});
