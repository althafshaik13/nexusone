import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { OrganizationApiService } from './organization-api.service';
import { environment } from '../../../environments/environment';
import { DepartmentResponse, EmployeeResponse, OrganizationResponse, TeamResponse } from '../models/organization.model';

describe('OrganizationApiService', () => {
  let service: OrganizationApiService;
  let httpMock: HttpTestingController;
  const baseUrl = environment.apiBaseUrl;

  const employee: EmployeeResponse = {
    id: 'e1',
    organizationId: 'o1',
    userId: 'u1',
    email: 'agent1@nexusone.dev',
    jobTitle: 'Support Agent',
    departmentId: null,
    teamId: null,
    managerId: null,
    createdAt: '2026-01-01T00:00:00Z',
  };

  const organization: OrganizationResponse = { id: 'o1', name: 'Acme Corp', createdAt: '2026-01-01T00:00:00Z' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OrganizationApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('getMyEmployee fetches the current user employee record', () => {
    let result: EmployeeResponse | undefined;
    service.getMyEmployee().subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/employees/me`);
    expect(req.request.method).toBe('GET');
    req.flush(employee);

    expect(result).toEqual(employee);
  });

  it('getEmployee fetches by id', () => {
    service.getEmployee('e1').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/employees/e1`);
    expect(req.request.method).toBe('GET');
    req.flush(employee);
  });

  it('getDirectReports fetches reports for a manager', () => {
    let result: EmployeeResponse[] | undefined;
    service.getDirectReports('e1').subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/employees/e1/direct-reports`);
    expect(req.request.method).toBe('GET');
    req.flush([employee]);

    expect(result).toEqual([employee]);
  });

  it('listDepartments fetches departments for an organization', () => {
    const department: DepartmentResponse = { id: 'd1', organizationId: 'o1', name: 'Support', createdAt: '2026-01-01T00:00:00Z' };
    let result: DepartmentResponse[] | undefined;
    service.listDepartments('o1').subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/organizations/o1/departments`);
    expect(req.request.method).toBe('GET');
    req.flush([department]);

    expect(result).toEqual([department]);
  });

  it('listOrganizations fetches all organizations', () => {
    let result: OrganizationResponse[] | undefined;
    service.listOrganizations().subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/organizations`);
    expect(req.request.method).toBe('GET');
    req.flush([organization]);

    expect(result).toEqual([organization]);
  });

  it('createOrganization posts the request body', () => {
    let result: OrganizationResponse | undefined;
    service.createOrganization({ name: 'Acme Corp' }).subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/organizations`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Acme Corp' });
    req.flush(organization);

    expect(result).toEqual(organization);
  });

  it('getOrganization fetches by id', () => {
    service.getOrganization('o1').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/organizations/o1`);
    expect(req.request.method).toBe('GET');
    req.flush(organization);
  });

  it('listEmployeesInOrg fetches employees for an organization', () => {
    let result: EmployeeResponse[] | undefined;
    service.listEmployeesInOrg('o1').subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/organizations/o1/employees`);
    expect(req.request.method).toBe('GET');
    req.flush([employee]);

    expect(result).toEqual([employee]);
  });

  it('createEmployee posts the request body to the org employees endpoint', () => {
    const request = { userId: 'u1', email: 'a@b.com', jobTitle: null, departmentId: null, teamId: null, managerId: null };
    service.createEmployee('o1', request).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/organizations/o1/employees`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(employee);
  });

  it('createDepartment posts the request body to the org departments endpoint', () => {
    service.createDepartment('o1', { name: 'Support' }).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/organizations/o1/departments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Support' });
    req.flush({});
  });

  it('listTeams fetches teams for a department', () => {
    const team: TeamResponse = { id: 't1', departmentId: 'd1', name: 'Tier 1', createdAt: '2026-01-01T00:00:00Z' };
    let result: TeamResponse[] | undefined;
    service.listTeams('d1').subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/departments/d1/teams`);
    expect(req.request.method).toBe('GET');
    req.flush([team]);

    expect(result).toEqual([team]);
  });

  it('createTeam posts the request body to the department teams endpoint', () => {
    service.createTeam('d1', { name: 'Tier 1' }).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/departments/d1/teams`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Tier 1' });
    req.flush({});
  });
});
