import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Ticket, TicketComment, TicketPriority, TicketStatus } from '../models/ticket.model';

export interface CreateTicketRequest {
  subject: string;
  description: string | null;
  priority: TicketPriority;
}

export interface CreateCommentRequest {
  body: string;
  internal: boolean;
}

@Injectable({ providedIn: 'root' })
export class TicketApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/tickets`;

  listTickets(status?: TicketStatus | ''): Observable<Ticket[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<Ticket[]>(this.baseUrl, { params });
  }

  getTicket(id: string): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.baseUrl}/${id}`);
  }

  listComments(ticketId: string): Observable<TicketComment[]> {
    return this.http.get<TicketComment[]>(`${this.baseUrl}/${ticketId}/comments`);
  }

  createTicket(request: CreateTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(this.baseUrl, request);
  }

  assignTicket(ticketId: string, agentId: string): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.baseUrl}/${ticketId}/assign`, { agentId });
  }

  transitionTicket(ticketId: string, targetStatus: TicketStatus): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.baseUrl}/${ticketId}/transition`, { targetStatus });
  }

  addComment(ticketId: string, request: CreateCommentRequest): Observable<TicketComment> {
    return this.http.post<TicketComment>(`${this.baseUrl}/${ticketId}/comments`, request);
  }
}
