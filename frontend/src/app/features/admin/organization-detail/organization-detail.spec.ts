import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { OrganizationDetail } from './organization-detail';
import { OrganizationApiService } from '../../../core/services/organization-api.service';
import { DepartmentResponse, EmployeeResponse, OrganizationResponse, TeamResponse } from '../../../core/models/organization.model';

const organization: OrganizationResponse = { id: 'o1', name: 'Acme Corp', createdAt: '2026-01-01T00:00:00Z' };
const department: DepartmentResponse = { id: 'd1', organizationId: 'o1', name: 'Support', createdAt: '2026-01-01T00:00:00Z' };
const employee: EmployeeResponse = {
  id: 'e1',
  organizationId: 'o1',
  userId: 'u1',
  email: 'a@b.com',
  jobTitle: null,
  departmentId: null,
  teamId: null,
  managerId: null,
  createdAt: '2026-01-01T00:00:00Z',
};
const team: TeamResponse = { id: 'team-1', departmentId: 'd1', name: 'Tier 1', createdAt: '2026-01-01T00:00:00Z' };

describe('OrganizationDetail', () => {
  let getOrganization: ReturnType<typeof vi.fn>;
  let listDepartments: ReturnType<typeof vi.fn>;
  let listEmployeesInOrg: ReturnType<typeof vi.fn>;
  let listTeams: ReturnType<typeof vi.fn>;
  let createDepartment: ReturnType<typeof vi.fn>;
  let createTeam: ReturnType<typeof vi.fn>;
  let createEmployee: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    getOrganization = vi.fn().mockReturnValue(of(organization));
    listDepartments = vi.fn().mockReturnValue(of([department]));
    listEmployeesInOrg = vi.fn().mockReturnValue(of([employee]));
    listTeams = vi.fn().mockReturnValue(of([team]));
    createDepartment = vi.fn().mockReturnValue(of({ ...department, id: 'd2', name: 'Engineering' }));
    createTeam = vi.fn().mockReturnValue(of(team));
    createEmployee = vi.fn().mockReturnValue(of(employee));

    await TestBed.configureTestingModule({
      imports: [OrganizationDetail],
      providers: [
        provideRouter([]),
        {
          provide: OrganizationApiService,
          useValue: { getOrganization, listDepartments, listEmployeesInOrg, listTeams, createDepartment, createTeam, createEmployee },
        },
      ],
    }).compileComponents();
  });

  it('loads the organization, its departments, and employees when the id input is set', () => {
    const fixture = TestBed.createComponent(OrganizationDetail);
    fixture.componentRef.setInput('id', 'o1');
    fixture.detectChanges();

    expect(getOrganization).toHaveBeenCalledWith('o1');
    expect(listDepartments).toHaveBeenCalledWith('o1');
    expect(listEmployeesInOrg).toHaveBeenCalledWith('o1');
    expect(fixture.componentInstance['organization']()).toEqual(organization);
    expect(fixture.componentInstance['departments']()).toEqual([department]);
  });

  it('expands a department and lazily loads its teams', () => {
    const fixture = TestBed.createComponent(OrganizationDetail);
    fixture.componentRef.setInput('id', 'o1');
    fixture.detectChanges();

    fixture.componentInstance['toggleDepartment']('d1');

    expect(listTeams).toHaveBeenCalledWith('d1');
    expect(fixture.componentInstance['teamsByDepartment']()['d1']).toEqual([team]);
  });

  it('adds a new department', () => {
    const fixture = TestBed.createComponent(OrganizationDetail);
    fixture.componentRef.setInput('id', 'o1');
    fixture.detectChanges();
    fixture.componentInstance['onNewDepartmentNameInput']('Engineering');

    fixture.componentInstance['addDepartment']();

    expect(createDepartment).toHaveBeenCalledWith('o1', { name: 'Engineering' });
    expect(fixture.componentInstance['departments']().some((d) => d.name === 'Engineering')).toBe(true);
  });

  it('resolves a department name for display', () => {
    const fixture = TestBed.createComponent(OrganizationDetail);
    fixture.componentRef.setInput('id', 'o1');
    fixture.detectChanges();

    expect(fixture.componentInstance['departmentName']('d1')).toBe('Support');
    expect(fixture.componentInstance['departmentName'](null)).toBe('—');
  });
});
