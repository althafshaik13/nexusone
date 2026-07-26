import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';

import { App } from './app';
import { KeycloakService } from './core/auth/keycloak.service';

class KeycloakServiceStub {
  authenticated = signal(true);
  username = signal('testuser');
  roles = signal<string[]>(['CUSTOMER']);
  hasRole(role: string): boolean {
    return this.roles().includes(role);
  }
  logout(): void {}
}

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        { provide: KeycloakService, useClass: KeycloakServiceStub },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the NexusOne brand', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.brand')?.textContent).toContain('NexusOne');
  });

  it('should show the logged-in username', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.user')?.textContent).toContain('testuser');
  });
});
