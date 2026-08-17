import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { CreateWorkflowDefinitionRequest, WorkflowApiService } from '../../../core/services/workflow-api.service';
import { ApproverType } from '../../../core/models/workflow.model';

@Component({
  selector: 'app-workflow-definition-create',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './workflow-definition-create.html',
  styleUrl: './workflow-definition-create.scss',
})
export class WorkflowDefinitionCreate {
  private readonly fb = inject(FormBuilder);
  private readonly workflowApi = inject(WorkflowApiService);
  private readonly router = inject(Router);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    requestType: ['', Validators.required],
    name: ['', [Validators.required, Validators.maxLength(255)]],
    steps: this.fb.array([this.createStep()]),
  });

  protected get steps() {
    return this.form.controls.steps;
  }

  protected addStep(): void {
    this.steps.push(this.createStep());
  }

  protected removeStep(index: number): void {
    if (this.steps.length > 1) {
      this.steps.removeAt(index);
    }
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const { requestType, name, steps } = this.form.getRawValue();
    const request: CreateWorkflowDefinitionRequest = {
      requestType,
      name,
      steps: steps.map((step) => ({
        approverType: step.approverType,
        approverEmployeeId: step.approverType === 'SPECIFIC_EMPLOYEE' ? step.approverEmployeeId || null : null,
      })),
    };
    this.workflowApi.createDefinition(request).subscribe({
      next: () => this.router.navigate(['/admin/workflow-definitions']),
      error: () => {
        this.error.set('Failed to create workflow definition.');
        this.submitting.set(false);
      },
    });
  }

  private createStep() {
    return this.fb.nonNullable.group({
      approverType: ['MANAGER' as ApproverType, Validators.required],
      approverEmployeeId: [''],
    });
  }
}
