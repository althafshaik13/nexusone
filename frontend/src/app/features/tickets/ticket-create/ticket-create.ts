import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { TicketApiService } from '../../../core/services/ticket-api.service';
import { TicketPriority } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-create',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './ticket-create.html',
  styleUrl: './ticket-create.scss',
})
export class TicketCreate {
  private readonly fb = inject(FormBuilder);
  private readonly ticketApi = inject(TicketApiService);
  private readonly router = inject(Router);

  protected readonly priorities: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    subject: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
    priority: ['MEDIUM' as TicketPriority, Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const { subject, description, priority } = this.form.getRawValue();
    this.ticketApi.createTicket({ subject, description: description || null, priority }).subscribe({
      next: (ticket) => this.router.navigate(['/tickets', ticket.id]),
      error: () => {
        this.error.set('Failed to create ticket.');
        this.submitting.set(false);
      },
    });
  }
}
