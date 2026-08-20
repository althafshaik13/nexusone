import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { TicketCreate } from './ticket-create';
import { TicketApiService } from '../../../core/services/ticket-api.service';
import { Ticket } from '../../../core/models/ticket.model';

const createdTicket: Ticket = {
  id: 't1',
  subject: 'New issue',
  description: null,
  status: 'CREATED',
  priority: 'MEDIUM',
  customerId: 'c1',
  assignedAgentId: null,
  slaDueAt: null,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

describe('TicketCreate', () => {
  let createTicket: ReturnType<typeof vi.fn>;
  let router: Router;

  beforeEach(async () => {
    createTicket = vi.fn().mockReturnValue(of(createdTicket));
    await TestBed.configureTestingModule({
      imports: [TicketCreate],
      providers: [provideRouter([]), { provide: TicketApiService, useValue: { createTicket } }],
    }).compileComponents();
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
  });

  it('does not submit when the form is invalid', () => {
    const fixture = TestBed.createComponent(TicketCreate);
    fixture.detectChanges();

    fixture.componentInstance['submit']();

    expect(createTicket).not.toHaveBeenCalled();
  });

  it('submits the ticket and navigates to its detail page on success', () => {
    const fixture = TestBed.createComponent(TicketCreate);
    fixture.detectChanges();
    fixture.componentInstance['form'].setValue({ subject: 'New issue', description: '', priority: 'MEDIUM' });

    fixture.componentInstance['submit']();

    expect(createTicket).toHaveBeenCalledWith({ subject: 'New issue', description: null, priority: 'MEDIUM' });
    expect(router.navigate).toHaveBeenCalledWith(['/tickets', 't1']);
  });

  it('surfaces an error and stops submitting when the API call fails', () => {
    createTicket.mockReturnValue(throwError(() => new Error('boom')));
    const fixture = TestBed.createComponent(TicketCreate);
    fixture.detectChanges();
    fixture.componentInstance['form'].setValue({ subject: 'New issue', description: '', priority: 'MEDIUM' });

    fixture.componentInstance['submit']();

    expect(fixture.componentInstance['error']()).toBe('Failed to create ticket.');
    expect(fixture.componentInstance['submitting']()).toBe(false);
  });
});
