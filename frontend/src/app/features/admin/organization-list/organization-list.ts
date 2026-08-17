import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { OrganizationApiService } from '../../../core/services/organization-api.service';
import { OrganizationResponse } from '../../../core/models/organization.model';

@Component({
  selector: 'app-organization-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './organization-list.html',
  styleUrl: './organization-list.scss',
})
export class OrganizationList {
  private readonly organizationApi = inject(OrganizationApiService);

  protected readonly organizations = signal<OrganizationResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.loadOrganizations();
  }

  private loadOrganizations(): void {
    this.loading.set(true);
    this.error.set(null);
    this.organizationApi.listOrganizations().subscribe({
      next: (organizations) => {
        this.organizations.set(organizations);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load organizations. Is organization-service running?');
        this.loading.set(false);
      },
    });
  }
}
