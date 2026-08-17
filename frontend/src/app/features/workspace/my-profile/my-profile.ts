import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';

import { OrganizationApiService } from '../../../core/services/organization-api.service';
import { EmployeeResponse } from '../../../core/models/organization.model';

@Component({
  selector: 'app-my-profile',
  imports: [],
  templateUrl: './my-profile.html',
  styleUrl: './my-profile.scss',
})
export class MyProfile {
  private readonly organizationApi = inject(OrganizationApiService);

  protected readonly employee = signal<EmployeeResponse | null>(null);
  protected readonly departmentName = signal<string | null>(null);
  protected readonly manager = signal<EmployeeResponse | null>(null);
  protected readonly directReports = signal<EmployeeResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly notFound = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.loadEmployee();
  }

  private loadEmployee(): void {
    this.loading.set(true);
    this.notFound.set(false);
    this.error.set(null);
    this.organizationApi.getMyEmployee().subscribe({
      next: (employee) => {
        this.employee.set(employee);
        this.loading.set(false);
        if (employee.departmentId) {
          this.loadDepartment(employee.organizationId, employee.departmentId);
        }
        if (employee.managerId) {
          this.loadManager(employee.managerId);
        }
        this.loadDirectReports(employee.id);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 404) {
          this.notFound.set(true);
        } else {
          this.error.set('Failed to load your profile. Is organization-service running?');
        }
        this.loading.set(false);
      },
    });
  }

  private loadDepartment(organizationId: string, departmentId: string): void {
    this.organizationApi.listDepartments(organizationId).subscribe({
      next: (departments) => {
        const department = departments.find((d) => d.id === departmentId);
        this.departmentName.set(department ? department.name : null);
      },
    });
  }

  private loadManager(managerId: string): void {
    this.organizationApi.getEmployee(managerId).subscribe({
      next: (manager) => this.manager.set(manager),
    });
  }

  private loadDirectReports(employeeId: string): void {
    this.organizationApi.getDirectReports(employeeId).subscribe({
      next: (reports) => this.directReports.set(reports),
    });
  }
}
