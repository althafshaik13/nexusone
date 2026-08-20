import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { CopilotApiService } from './copilot-api.service';
import { environment } from '../../../environments/environment';
import { SuggestReplyResponse, SummarizeResponse } from '../models/copilot.model';

describe('CopilotApiService', () => {
  let service: CopilotApiService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiBaseUrl}/copilot`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CopilotApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('suggestReply posts an empty body to the suggest-reply endpoint', () => {
    const response: SuggestReplyResponse = { suggestion: 'Have you tried resetting your password?' };
    let result: SuggestReplyResponse | undefined;
    service.suggestReply('t1').subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/tickets/t1/suggest-reply`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush(response);

    expect(result).toEqual(response);
  });

  it('summarize posts an empty body to the summarize endpoint', () => {
    const response: SummarizeResponse = { summary: 'Customer cannot log in after password reset.' };
    let result: SummarizeResponse | undefined;
    service.summarize('t1').subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${baseUrl}/tickets/t1/summarize`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush(response);

    expect(result).toEqual(response);
  });
});
