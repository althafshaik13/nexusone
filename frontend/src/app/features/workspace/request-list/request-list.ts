import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { WorkflowInstanceResponse } from '../../../core/models/workflow.model';

@Component({
  selector: 'app-request-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './request-list.html',
  styleUrl: './request-list.scss',
})
export class RequestList {
  private readonly workflowApi = inject(WorkflowApiService);

  protected readonly requests = signal<WorkflowInstanceResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.loadRequests();
  }

  private loadRequests(): void {
    this.loading.set(true);
    this.error.set(null);
    this.workflowApi.listMine().subscribe({
      next: (requests) => {
        this.requests.set(requests);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load your requests. Is workflow-service running?');
        this.loading.set(false);
      },
    });
  }
}
