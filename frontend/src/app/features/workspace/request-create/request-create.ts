import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { WorkflowDefinitionResponse } from '../../../core/models/workflow.model';

@Component({
  selector: 'app-request-create',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './request-create.html',
  styleUrl: './request-create.scss',
})
export class RequestCreate {
  private readonly fb = inject(FormBuilder);
  private readonly workflowApi = inject(WorkflowApiService);
  private readonly router = inject(Router);

  protected readonly definitions = signal<WorkflowDefinitionResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    requestType: ['', Validators.required],
    title: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
  });

  constructor() {
    this.loadDefinitions();
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const { requestType, title, description } = this.form.getRawValue();
    this.workflowApi.submitInstance({ requestType, title, description: description || null }).subscribe({
      next: (instance) => this.router.navigate(['/requests', instance.id]),
      error: () => {
        this.error.set('Failed to submit request.');
        this.submitting.set(false);
      },
    });
  }

  private loadDefinitions(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.workflowApi.listDefinitions().subscribe({
      next: (definitions) => {
        this.definitions.set(definitions);
        this.loading.set(false);
        if (definitions.length > 0) {
          this.form.patchValue({ requestType: definitions[0].requestType });
        }
      },
      error: () => {
        this.loadError.set('Failed to load request types. Is workflow-service running?');
        this.loading.set(false);
      },
    });
  }
}
