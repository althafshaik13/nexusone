import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { KeycloakService } from '../../../core/auth/keycloak.service';
import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { Decision, WorkflowInstanceResponse, WorkflowStepInstanceResponse } from '../../../core/models/workflow.model';

@Component({
  selector: 'app-request-detail',
  imports: [DatePipe, RouterLink],
  templateUrl: './request-detail.html',
  styleUrl: './request-detail.scss',
})
export class RequestDetail {
  private readonly workflowApi = inject(WorkflowApiService);
  protected readonly auth = inject(KeycloakService);

  readonly id = input.required<string>();

  protected readonly instance = signal<WorkflowInstanceResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly acting = signal(false);
  protected readonly comment = signal('');

  constructor() {
    effect(() => {
      const instanceId = this.id();
      this.loadInstance(instanceId);
    });
  }

  protected canDecide(step: WorkflowStepInstanceResponse): boolean {
    const instance = this.instance();
    if (!instance) {
      return false;
    }
    return (
      step.resolvedApproverUserId === this.auth.subject() &&
      step.status === 'PENDING' &&
      instance.status === 'PENDING' &&
      step.stepOrder === instance.currentStepOrder
    );
  }

  protected onCommentInput(value: string): void {
    this.comment.set(value);
  }

  protected decide(decision: Decision): void {
    const instance = this.instance();
    if (!instance || this.acting()) {
      return;
    }
    this.acting.set(true);
    const commentValue = this.comment().trim();
    this.workflowApi
      .decide(instance.id, instance.currentStepOrder, { decision, comment: commentValue || null })
      .subscribe({
        next: (updated) => {
          this.instance.set(updated);
          this.comment.set('');
          this.acting.set(false);
        },
        error: () => this.acting.set(false),
      });
  }

  private loadInstance(instanceId: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.workflowApi.getInstance(instanceId).subscribe({
      next: (instance) => {
        this.instance.set(instance);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Request not found.');
        this.loading.set(false);
      },
    });
  }
}
