import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { OrganizationApiService } from '../../../core/services/organization-api.service';

@Component({
  selector: 'app-organization-create',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './organization-create.html',
  styleUrl: './organization-create.scss',
})
export class OrganizationCreate {
  private readonly fb = inject(FormBuilder);
  private readonly organizationApi = inject(OrganizationApiService);
  private readonly router = inject(Router);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
  });

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const { name } = this.form.getRawValue();
    this.organizationApi.createOrganization({ name }).subscribe({
      next: (organization) => this.router.navigate(['/admin/organizations', organization.id]),
      error: () => {
        this.error.set('Failed to create organization.');
        this.submitting.set(false);
      },
    });
  }
}
