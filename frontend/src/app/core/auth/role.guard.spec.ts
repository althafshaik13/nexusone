import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { signal } from '@angular/core';

import { roleGuard } from './role.guard';
import { KeycloakService } from './keycloak.service';

class KeycloakServiceStub {
  roles = signal<string[]>([]);
  hasRole(role: string): boolean {
    return this.roles().includes(role);
  }
}

describe('roleGuard', () => {
  let auth: KeycloakServiceStub;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: KeycloakService, useClass: KeycloakServiceStub }],
    });
    auth = TestBed.inject(KeycloakService) as unknown as KeycloakServiceStub;
    router = TestBed.inject(Router);
  });

  it('allows navigation when the user has the required role', () => {
    auth.roles.set(['ADMIN']);
    const guard = roleGuard('ADMIN');

    const result = TestBed.runInInjectionContext(() => guard({} as never, {} as never));

    expect(result).toBe(true);
  });

  it('redirects to /tickets when the user lacks the required role', () => {
    auth.roles.set(['CUSTOMER']);
    const guard = roleGuard('ADMIN');

    const result = TestBed.runInInjectionContext(() => guard({} as never, {} as never));

    expect(result).not.toBe(true);
    expect(router.serializeUrl(result as ReturnType<Router['createUrlTree']>)).toBe('/tickets');
  });
});
