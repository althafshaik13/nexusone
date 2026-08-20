import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';

import { TicketDetail } from './ticket-detail';
import { TicketApiService } from '../../../core/services/ticket-api.service';
import { CopilotApiService } from '../../../core/services/copilot-api.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { Ticket } from '../../../core/models/ticket.model';

class KeycloakServiceStub {
  subject = signal<string | null>('agent-1');
  roles = signal<string[]>(['AGENT']);
  hasRole(role: string): boolean {
    return this.roles().includes(role);
  }
}

const ticket: Ticket = {
  id: 't1',
  subject: 'Cannot log in',
  description: null,
  status: 'CREATED',
  priority: 'HIGH',
  customerId: 'c1',
  assignedAgentId: null,
  slaDueAt: null,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

describe('TicketDetail', () => {
  let getTicket: ReturnType<typeof vi.fn>;
  let listComments: ReturnType<typeof vi.fn>;
  let listEvents: ReturnType<typeof vi.fn>;
  let assignTicket: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    getTicket = vi.fn().mockReturnValue(of(ticket));
    listComments = vi.fn().mockReturnValue(of([]));
    listEvents = vi.fn().mockReturnValue(of([]));
    assignTicket = vi.fn().mockReturnValue(of({ ...ticket, status: 'ASSIGNED' }));

    await TestBed.configureTestingModule({
      imports: [TicketDetail],
      providers: [
        provideRouter([]),
        { provide: TicketApiService, useValue: { getTicket, listComments, listEvents, assignTicket } },
        { provide: CopilotApiService, useValue: { suggestReply: vi.fn(), summarize: vi.fn() } },
        { provide: KeycloakService, useClass: KeycloakServiceStub },
      ],
    }).compileComponents();
  });

  it('loads the ticket, its comments, and events for an agent when the id input is set', () => {
    const fixture = TestBed.createComponent(TicketDetail);
    fixture.componentRef.setInput('id', 't1');
    fixture.detectChanges();

    expect(getTicket).toHaveBeenCalledWith('t1');
    expect(listComments).toHaveBeenCalledWith('t1');
    expect(listEvents).toHaveBeenCalledWith('t1');
    expect(fixture.componentInstance['ticket']()).toEqual(ticket);
  });

  it('does not load the audit trail for a non-agent user', async () => {
    await TestBed.resetTestingModule().configureTestingModule({
      imports: [TicketDetail],
      providers: [
        provideRouter([]),
        { provide: TicketApiService, useValue: { getTicket, listComments, listEvents, assignTicket } },
        { provide: CopilotApiService, useValue: { suggestReply: vi.fn(), summarize: vi.fn() } },
        {
          provide: KeycloakService,
          useValue: { subject: signal('customer-1'), roles: signal([]), hasRole: () => false },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(TicketDetail);
    fixture.componentRef.setInput('id', 't1');
    fixture.detectChanges();

    expect(listEvents).not.toHaveBeenCalled();
  });

  it('assigns the ticket to the current user', () => {
    const fixture = TestBed.createComponent(TicketDetail);
    fixture.componentRef.setInput('id', 't1');
    fixture.detectChanges();

    fixture.componentInstance['assignToMe']();

    expect(assignTicket).toHaveBeenCalledWith('t1', 'agent-1');
    expect(fixture.componentInstance['ticket']()?.status).toBe('ASSIGNED');
  });
});
