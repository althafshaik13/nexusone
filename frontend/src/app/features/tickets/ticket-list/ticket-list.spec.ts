import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { TicketList } from './ticket-list';
import { TicketApiService } from '../../../core/services/ticket-api.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { Ticket } from '../../../core/models/ticket.model';

class KeycloakServiceStub {
  hasRole(): boolean {
    return false;
  }
}

const tickets: Ticket[] = [
  {
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
  },
];

describe('TicketList', () => {
  let listTickets: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    listTickets = vi.fn().mockReturnValue(of(tickets));
    await TestBed.configureTestingModule({
      imports: [TicketList],
      providers: [
        provideRouter([]),
        { provide: TicketApiService, useValue: { listTickets } },
        { provide: KeycloakService, useClass: KeycloakServiceStub },
      ],
    }).compileComponents();
  });

  it('loads tickets on creation', () => {
    const fixture = TestBed.createComponent(TicketList);
    fixture.detectChanges();

    expect(listTickets).toHaveBeenCalledWith('', undefined);
    expect(fixture.componentInstance['tickets']()).toEqual(tickets);
  });

  it('renders the fetched tickets', () => {
    const fixture = TestBed.createComponent(TicketList);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Cannot log in');
  });

  it('reloads with the selected status when changed', () => {
    const fixture = TestBed.createComponent(TicketList);
    fixture.detectChanges();
    listTickets.mockClear();

    fixture.componentInstance['onStatusChange']('ASSIGNED');

    expect(listTickets).toHaveBeenCalledWith('ASSIGNED', undefined);
  });
});
