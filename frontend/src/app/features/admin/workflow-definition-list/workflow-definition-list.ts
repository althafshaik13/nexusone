import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { WorkflowDefinitionResponse } from '../../../core/models/workflow.model';

@Component({
  selector: 'app-workflow-definition-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './workflow-definition-list.html',
  styleUrl: './workflow-definition-list.scss',
})
export class WorkflowDefinitionList {
  private readonly workflowApi = inject(WorkflowApiService);

  protected readonly definitions = signal<WorkflowDefinitionResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.loadDefinitions();
  }

  private loadDefinitions(): void {
    this.loading.set(true);
    this.error.set(null);
    this.workflowApi.listDefinitions().subscribe({
      next: (definitions) => {
        this.definitions.set(definitions);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load workflow definitions. Is workflow-service running?');
        this.loading.set(false);
      },
    });
  }
}
