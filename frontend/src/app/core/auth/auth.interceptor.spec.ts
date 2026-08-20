import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { authInterceptor } from './auth.interceptor';
import { KeycloakService } from './keycloak.service';
import { environment } from '../../../environments/environment';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let getValidToken: ReturnType<typeof vi.fn>;

  function setup(): void {
    getValidToken = vi.fn().mockResolvedValue('mock-token');
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: KeycloakService, useValue: { getValidToken } },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  }

  afterEach(() => {
    httpMock.verify();
  });

  it('attaches an Authorization header for requests to the API base url', async () => {
    setup();
    http.get(`${environment.apiBaseUrl}/tickets`).subscribe();

    const req = await vi.waitFor(() => httpMock.expectOne(`${environment.apiBaseUrl}/tickets`));
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush([]);
  });

  it('does not attach a header when no token is available', async () => {
    getValidToken = vi.fn().mockResolvedValue(undefined);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: KeycloakService, useValue: { getValidToken } },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);

    http.get(`${environment.apiBaseUrl}/tickets`).subscribe();

    const req = await vi.waitFor(() => httpMock.expectOne(`${environment.apiBaseUrl}/tickets`));
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });

  it('does not consult the token for requests outside the API base url', () => {
    setup();
    http.get('https://example.com/health').subscribe();

    const req = httpMock.expectOne('https://example.com/health');
    expect(req.request.headers.has('Authorization')).toBe(false);
    expect(getValidToken).not.toHaveBeenCalled();
    req.flush({});
  });
});
