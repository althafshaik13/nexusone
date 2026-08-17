import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DepartmentResponse, EmployeeResponse, OrganizationResponse, TeamResponse } from '../models/organization.model';

export interface CreateEmployeeRequest {
  userId: string;
  email: string;
  jobTitle: string | null;
  departmentId: string | null;
  teamId: string | null;
  managerId: string | null;
}

@Injectable({ providedIn: 'root' })
export class OrganizationApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  getMyEmployee(): Observable<EmployeeResponse> {
    return this.http.get<EmployeeResponse>(`${this.baseUrl}/employees/me`);
  }

  getEmployee(id: string): Observable<EmployeeResponse> {
    return this.http.get<EmployeeResponse>(`${this.baseUrl}/employees/${id}`);
  }

  getDirectReports(employeeId: string): Observable<EmployeeResponse[]> {
    return this.http.get<EmployeeResponse[]>(`${this.baseUrl}/employees/${employeeId}/direct-reports`);
  }

  listDepartments(organizationId: string): Observable<DepartmentResponse[]> {
    return this.http.get<DepartmentResponse[]>(`${this.baseUrl}/organizations/${organizationId}/departments`);
  }

  listOrganizations(): Observable<OrganizationResponse[]> {
    return this.http.get<OrganizationResponse[]>(`${this.baseUrl}/organizations`);
  }

  createOrganization(request: { name: string }): Observable<OrganizationResponse> {
    return this.http.post<OrganizationResponse>(`${this.baseUrl}/organizations`, request);
  }

  getOrganization(id: string): Observable<OrganizationResponse> {
    return this.http.get<OrganizationResponse>(`${this.baseUrl}/organizations/${id}`);
  }

  listEmployeesInOrg(organizationId: string): Observable<EmployeeResponse[]> {
    return this.http.get<EmployeeResponse[]>(`${this.baseUrl}/organizations/${organizationId}/employees`);
  }

  createEmployee(organizationId: string, request: CreateEmployeeRequest): Observable<EmployeeResponse> {
    return this.http.post<EmployeeResponse>(`${this.baseUrl}/organizations/${organizationId}/employees`, request);
  }

  createDepartment(organizationId: string, request: { name: string }): Observable<DepartmentResponse> {
    return this.http.post<DepartmentResponse>(`${this.baseUrl}/organizations/${organizationId}/departments`, request);
  }

  listTeams(departmentId: string): Observable<TeamResponse[]> {
    return this.http.get<TeamResponse[]>(`${this.baseUrl}/departments/${departmentId}/teams`);
  }

  createTeam(departmentId: string, request: { name: string }): Observable<TeamResponse> {
    return this.http.post<TeamResponse>(`${this.baseUrl}/departments/${departmentId}/teams`, request);
  }
}
