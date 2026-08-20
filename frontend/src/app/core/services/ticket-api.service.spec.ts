import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { TicketApiService } from './ticket-api.service';
import { environment } from '../../../environments/environment';
import { Ticket, TicketComment, TicketEvent, TicketStatsResponse } from '../models/ticket.model';

describe('TicketApiService', () => {
  let service: TicketApiService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiBaseUrl}/tickets`;

  const ticket: Ticket = {
    id: 't1',
    subject: 'Cannot log in',
    description: 'Password reset link is broken',
    status: 'CREATED',
    priority: 'HIGH',
    customerId: 'c1',
    assignedAgentId: null,
    slaDueAt: null,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TicketApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('listTickets with no filters hits base url with no params', () => {
    let result: Ticket[] | undefined;
    service.listTickets().subscribe((r) => (result = r));

    const req = httpMock.expectOne((r) => r.url === baseUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys().length).toBe(0);
    req.flush([ticket]);

    expect(result).toEqual([ticket]);
  });

  it('listTickets sets status and q params when provided', () => {
    service.listTickets('CREATED', 'login').subscribe();

    const req = httpMock.expectOne((r) => r.url === baseUrl);
    expect(req.request.params.get('status')).toBe('CREATED');
    expect(req.request.params.get('q')).toBe('login');
    req.flush([]);
  });

  it('listTickets omits status param when empty string', () => {
    service.listTickets('', 'login').subscribe();

    const req = httpMock.expectOne((r) => r.url === baseUrl);
    expect(req.request.params.has('status')).toBe(false);
    expect(req.request.params.get('q')).toBe('login');
    req.flush([]);
  });

  it('getTicket fetches by id', () => {
    let result: Ticket | undefined;
    service.getTicket('t1').subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/t1`);
    expect(req.request.method).toBe('GET');
    req.flush(ticket);

    expect(result).toEqual(ticket);
  });

  it('listComments fetches comments for a ticket', () => {
    const comment: TicketComment = {
      id: 'c1',
      ticketId: 't1',
      authorId: 'u1',
      body: 'Looking into it',
      internal: false,
      createdAt: '2026-01-01T00:00:00Z',
    };
    let result: TicketComment[] | undefined;
    service.listComments('t1').subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/t1/comments`);
    expect(req.request.method).toBe('GET');
    req.flush([comment]);

    expect(result).toEqual([comment]);
  });

  it('createTicket posts the request body', () => {
    const request = { subject: 'New issue', description: null, priority: 'LOW' as const };
    let result: Ticket | undefined;
    service.createTicket(request).subscribe((r) => (result = r));

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(ticket);

    expect(result).toEqual(ticket);
  });

  it('assignTicket posts agentId to the assign endpoint', () => {
    service.assignTicket('t1', 'agent1').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/t1/assign`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ agentId: 'agent1' });
    req.flush(ticket);
  });

  it('transitionTicket posts targetStatus to the transition endpoint', () => {
    service.transitionTicket('t1', 'ASSIGNED').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/t1/transition`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ targetStatus: 'ASSIGNED' });
    req.flush(ticket);
  });

  it('addComment posts to the comments endpoint', () => {
    const request = { body: 'Reply text', internal: false };
    service.addComment('t1', request).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/t1/comments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({});
  });

  it('listEvents fetches the audit trail for a ticket', () => {
    const event: TicketEvent = {
      id: 'e1',
      ticketId: 't1',
      eventType: 'TICKET_ASSIGNED',
      actorId: 'u1',
      payloadJson: '{}',
      createdAt: '2026-01-01T00:00:00Z',
    };
    let result: TicketEvent[] | undefined;
    service.listEvents('t1').subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/t1/events`);
    expect(req.request.method).toBe('GET');
    req.flush([event]);

    expect(result).toEqual([event]);
  });

  it('getStats fetches aggregate stats', () => {
    const stats: TicketStatsResponse = { countByStatus: { CREATED: 3 }, averageResolutionHours: 2.5 };
    let result: TicketStatsResponse | undefined;
    service.getStats().subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/stats`);
    expect(req.request.method).toBe('GET');
    req.flush(stats);

    expect(result).toEqual(stats);
  });
});
