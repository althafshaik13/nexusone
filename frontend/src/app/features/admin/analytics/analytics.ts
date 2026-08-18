import { Component, computed, inject, signal } from '@angular/core';

import { TicketApiService } from '../../../core/services/ticket-api.service';
import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { TicketStatsResponse } from '../../../core/models/ticket.model';
import { WorkflowStatsResponse } from '../../../core/models/workflow.model';

export interface StatBar {
  label: string;
  count: number;
  percent: number;
}

function toBars(counts: Record<string, number>): StatBar[] {
  const entries = Object.entries(counts);
  const max = Math.max(1, ...entries.map(([, count]) => count));
  return entries
    .sort((a, b) => b[1] - a[1])
    .map(([label, count]) => ({ label, count, percent: Math.round((count / max) * 100) }));
}

@Component({
  selector: 'app-analytics',
  imports: [],
  templateUrl: './analytics.html',
  styleUrl: './analytics.scss',
})
export class Analytics {
  private readonly ticketApi = inject(TicketApiService);
  private readonly workflowApi = inject(WorkflowApiService);

  protected readonly ticketStats = signal<TicketStatsResponse | null>(null);
  protected readonly workflowStats = signal<WorkflowStatsResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly ticketStatusBars = computed<StatBar[]>(() => {
    const stats = this.ticketStats();
    return stats ? toBars(stats.countByStatus) : [];
  });

  protected readonly workflowStatusBars = computed<StatBar[]>(() => {
    const stats = this.workflowStats();
    return stats ? toBars(stats.countByStatus) : [];
  });

  protected readonly workflowRequestTypeBars = computed<StatBar[]>(() => {
    const stats = this.workflowStats();
    return stats ? toBars(stats.countByRequestType) : [];
  });

  constructor() {
    this.loadStats();
  }

  protected formatHours(hours: number | null): string {
    if (hours === null) {
      return 'No data yet';
    }
    if (hours < 1) {
      return `${Math.round(hours * 60)} min`;
    }
    return `${hours.toFixed(1)} hours`;
  }

  private loadStats(): void {
    this.loading.set(true);
    this.error.set(null);
    this.ticketApi.getStats().subscribe({
      next: (stats) => {
        this.ticketStats.set(stats);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load ticket analytics.');
        this.loading.set(false);
      },
    });
    this.workflowApi.getStats().subscribe({
      next: (stats) => this.workflowStats.set(stats),
    });
  }
}
