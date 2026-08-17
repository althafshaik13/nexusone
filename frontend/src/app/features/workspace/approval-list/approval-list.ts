import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { WorkflowInstanceResponse } from '../../../core/models/workflow.model';

@Component({
  selector: 'app-approval-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './approval-list.html',
  styleUrl: './approval-list.scss',
})
export class ApprovalList {
  private readonly workflowApi = inject(WorkflowApiService);

  protected readonly requests = signal<WorkflowInstanceResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.loadPending();
  }

  private loadPending(): void {
    this.loading.set(true);
    this.error.set(null);
    this.workflowApi.listPendingMyApproval().subscribe({
      next: (requests) => {
        this.requests.set(requests);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load approvals. Is workflow-service running?');
        this.loading.set(false);
      },
    });
  }
}
