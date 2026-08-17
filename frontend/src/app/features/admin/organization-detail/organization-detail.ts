import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CreateEmployeeRequest, OrganizationApiService } from '../../../core/services/organization-api.service';
import {
  DepartmentResponse,
  EmployeeResponse,
  OrganizationResponse,
  TeamResponse,
} from '../../../core/models/organization.model';

@Component({
  selector: 'app-organization-detail',
  imports: [DatePipe, RouterLink, ReactiveFormsModule],
  templateUrl: './organization-detail.html',
  styleUrl: './organization-detail.scss',
})
export class OrganizationDetail {
  private readonly organizationApi = inject(OrganizationApiService);
  private readonly fb = inject(FormBuilder);

  readonly id = input.required<string>();

  protected readonly organization = signal<OrganizationResponse | null>(null);
  protected readonly departments = signal<DepartmentResponse[]>([]);
  protected readonly employees = signal<EmployeeResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly expandedDepartmentId = signal<string | null>(null);
  protected readonly teamsByDepartment = signal<Record<string, TeamResponse[]>>({});
  protected readonly teamsLoading = signal(false);

  protected readonly newDepartmentName = signal('');
  protected readonly departmentAdding = signal(false);
  protected readonly departmentError = signal<string | null>(null);

  protected readonly newTeamName = signal('');
  protected readonly teamAdding = signal(false);
  protected readonly teamError = signal<string | null>(null);

  protected readonly employeeSubmitting = signal(false);
  protected readonly employeeError = signal<string | null>(null);

  protected readonly employeeForm = this.fb.nonNullable.group({
    userId: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    jobTitle: [''],
    departmentId: [''],
    managerId: [''],
  });

  constructor() {
    effect(() => {
      const organizationId = this.id();
      this.loadOrganization(organizationId);
    });
  }

  protected departmentName(departmentId: string | null): string {
    if (!departmentId) {
      return '—';
    }
    const department = this.departments().find((d) => d.id === departmentId);
    return department ? department.name : '—';
  }

  protected toggleDepartment(departmentId: string): void {
    if (this.expandedDepartmentId() === departmentId) {
      this.expandedDepartmentId.set(null);
      return;
    }
    this.expandedDepartmentId.set(departmentId);
    this.newTeamName.set('');
    this.teamError.set(null);
    if (!this.teamsByDepartment()[departmentId]) {
      this.loadTeams(departmentId);
    }
  }

  protected onNewDepartmentNameInput(value: string): void {
    this.newDepartmentName.set(value);
  }

  protected addDepartment(): void {
    const organization = this.organization();
    const name = this.newDepartmentName().trim();
    if (!organization || !name || this.departmentAdding()) {
      return;
    }
    this.departmentAdding.set(true);
    this.departmentError.set(null);
    this.organizationApi.createDepartment(organization.id, { name }).subscribe({
      next: (department) => {
        this.departments.update((departments) => [...departments, department]);
        this.newDepartmentName.set('');
        this.departmentAdding.set(false);
      },
      error: () => {
        this.departmentError.set('Failed to create department.');
        this.departmentAdding.set(false);
      },
    });
  }

  protected onNewTeamNameInput(value: string): void {
    this.newTeamName.set(value);
  }

  protected addTeam(departmentId: string): void {
    const name = this.newTeamName().trim();
    if (!name || this.teamAdding()) {
      return;
    }
    this.teamAdding.set(true);
    this.teamError.set(null);
    this.organizationApi.createTeam(departmentId, { name }).subscribe({
      next: (team) => {
        this.teamsByDepartment.update((map) => ({
          ...map,
          [departmentId]: [...(map[departmentId] ?? []), team],
        }));
        this.newTeamName.set('');
        this.teamAdding.set(false);
      },
      error: () => {
        this.teamError.set('Failed to create team.');
        this.teamAdding.set(false);
      },
    });
  }

  protected submitEmployee(): void {
    const organization = this.organization();
    if (!organization || this.employeeForm.invalid || this.employeeSubmitting()) {
      return;
    }
    this.employeeSubmitting.set(true);
    this.employeeError.set(null);
    const { userId, email, jobTitle, departmentId, managerId } = this.employeeForm.getRawValue();
    const request: CreateEmployeeRequest = {
      userId,
      email,
      jobTitle: jobTitle || null,
      departmentId: departmentId || null,
      teamId: null,
      managerId: managerId || null,
    };
    this.organizationApi.createEmployee(organization.id, request).subscribe({
      next: (employee) => {
        this.employees.update((employees) => [...employees, employee]);
        this.employeeForm.reset({ userId: '', email: '', jobTitle: '', departmentId: '', managerId: '' });
        this.employeeSubmitting.set(false);
      },
      error: () => {
        this.employeeError.set('Failed to create employee.');
        this.employeeSubmitting.set(false);
      },
    });
  }

  private loadOrganization(organizationId: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.expandedDepartmentId.set(null);
    this.teamsByDepartment.set({});
    this.organizationApi.getOrganization(organizationId).subscribe({
      next: (organization) => {
        this.organization.set(organization);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Organization not found.');
        this.loading.set(false);
      },
    });
    this.organizationApi.listDepartments(organizationId).subscribe({
      next: (departments) => this.departments.set(departments),
    });
    this.organizationApi.listEmployeesInOrg(organizationId).subscribe({
      next: (employees) => this.employees.set(employees),
    });
  }

  private loadTeams(departmentId: string): void {
    this.teamsLoading.set(true);
    this.teamError.set(null);
    this.organizationApi.listTeams(departmentId).subscribe({
      next: (teams) => {
        this.teamsByDepartment.update((map) => ({ ...map, [departmentId]: teams }));
        this.teamsLoading.set(false);
      },
      error: () => {
        this.teamError.set('Failed to load teams.');
        this.teamsLoading.set(false);
      },
    });
  }
}
