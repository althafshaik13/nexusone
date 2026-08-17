import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { SuggestReplyResponse, SummarizeResponse } from '../models/copilot.model';

@Injectable({ providedIn: 'root' })
export class CopilotApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/copilot`;

  suggestReply(ticketId: string): Observable<SuggestReplyResponse> {
    return this.http.post<SuggestReplyResponse>(`${this.baseUrl}/tickets/${ticketId}/suggest-reply`, {});
  }

  summarize(ticketId: string): Observable<SummarizeResponse> {
    return this.http.post<SummarizeResponse>(`${this.baseUrl}/tickets/${ticketId}/summarize`, {});
  }
}
