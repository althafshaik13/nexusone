import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DepartmentResponse, EmployeeResponse } from '../models/organization.model';

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
}
