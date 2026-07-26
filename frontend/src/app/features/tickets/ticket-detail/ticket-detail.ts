import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { KeycloakService } from '../../../core/auth/keycloak.service';
import { TicketApiService } from '../../../core/services/ticket-api.service';
import { Ticket, TicketComment, TicketStatus } from '../../../core/models/ticket.model';

const ALLOWED_TRANSITIONS: Record<TicketStatus, TicketStatus[]> = {
  CREATED: ['ASSIGNED'],
  ASSIGNED: ['IN_PROGRESS', 'ESCALATED'],
  IN_PROGRESS: ['RESOLVED', 'ESCALATED'],
  ESCALATED: ['IN_PROGRESS', 'RESOLVED'],
  RESOLVED: ['CLOSED', 'REOPENED'],
  REOPENED: ['ASSIGNED', 'IN_PROGRESS'],
  CLOSED: [],
};

@Component({
  selector: 'app-ticket-detail',
  imports: [DatePipe, RouterLink],
  templateUrl: './ticket-detail.html',
  styleUrl: './ticket-detail.scss',
})
export class TicketDetail {
  private readonly ticketApi = inject(TicketApiService);
  protected readonly auth = inject(KeycloakService);

  readonly id = input.required<string>();

  protected readonly ticket = signal<Ticket | null>(null);
  protected readonly comments = signal<TicketComment[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly acting = signal(false);

  constructor() {
    effect(() => {
      const ticketId = this.id();
      this.loadTicket(ticketId);
    });
  }

  protected isAgent(): boolean {
    return this.auth.hasRole('AGENT') || this.auth.hasRole('ADMIN');
  }

  protected allowedTargets(): TicketStatus[] {
    const ticket = this.ticket();
    return ticket ? ALLOWED_TRANSITIONS[ticket.status] : [];
  }

  protected canAssign(): boolean {
    return this.allowedTargets().includes('ASSIGNED');
  }

  protected transitionTargets(): TicketStatus[] {
    return this.allowedTargets().filter((status) => status !== 'ASSIGNED');
  }

  protected assignToMe(): void {
    const ticket = this.ticket();
    const subject = this.auth.subject();
    if (!ticket || !subject || this.acting()) {
      return;
    }
    this.acting.set(true);
    this.ticketApi.assignTicket(ticket.id, subject).subscribe({
      next: (updated) => {
        this.ticket.set(updated);
        this.acting.set(false);
      },
      error: () => this.acting.set(false),
    });
  }

  protected transition(target: TicketStatus): void {
    const ticket = this.ticket();
    if (!ticket || this.acting()) {
      return;
    }
    this.acting.set(true);
    this.ticketApi.transitionTicket(ticket.id, target).subscribe({
      next: (updated) => {
        this.ticket.set(updated);
        this.acting.set(false);
      },
      error: () => this.acting.set(false),
    });
  }

  private loadTicket(ticketId: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.ticketApi.getTicket(ticketId).subscribe({
      next: (ticket) => {
        this.ticket.set(ticket);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Ticket not found.');
        this.loading.set(false);
      },
    });
    this.ticketApi.listComments(ticketId).subscribe({
      next: (comments) => this.comments.set(comments),
    });
  }
}
