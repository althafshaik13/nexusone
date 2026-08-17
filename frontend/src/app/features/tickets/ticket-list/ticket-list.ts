import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { KeycloakService } from '../../../core/auth/keycloak.service';
import { TicketApiService } from '../../../core/services/ticket-api.service';
import { Ticket, TicketStatus } from '../../../core/models/ticket.model';

const STATUS_FILTERS: Array<TicketStatus | ''> = [
  '',
  'CREATED',
  'ASSIGNED',
  'IN_PROGRESS',
  'ESCALATED',
  'RESOLVED',
  'REOPENED',
  'CLOSED',
];

@Component({
  selector: 'app-ticket-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.scss',
})
export class TicketList {
  private readonly ticketApi = inject(TicketApiService);
  protected readonly auth = inject(KeycloakService);

  protected readonly statusFilters = STATUS_FILTERS;
  protected readonly selectedStatus = signal<TicketStatus | ''>('');
  protected readonly searchTerm = signal('');
  protected readonly tickets = signal<Ticket[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  private searchDebounce?: ReturnType<typeof setTimeout>;

  constructor() {
    this.loadTickets();
  }

  protected onStatusChange(status: TicketStatus | ''): void {
    this.selectedStatus.set(status);
    this.loadTickets();
  }

  protected onSearchInput(value: string): void {
    this.searchTerm.set(value);
    if (this.searchDebounce) {
      clearTimeout(this.searchDebounce);
    }
    this.searchDebounce = setTimeout(() => this.loadTickets(), 300);
  }

  private loadTickets(): void {
    this.loading.set(true);
    this.error.set(null);
    this.ticketApi.listTickets(this.selectedStatus(), this.searchTerm().trim() || undefined).subscribe({
      next: (tickets) => {
        this.tickets.set(tickets);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load tickets. Is ticket-service running?');
        this.loading.set(false);
      },
    });
  }
}
