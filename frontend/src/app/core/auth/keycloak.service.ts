import { Injectable, signal } from '@angular/core';
import Keycloak from 'keycloak-js';

import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private readonly keycloak = new Keycloak({
    url: environment.keycloak.url,
    realm: environment.keycloak.realm,
    clientId: environment.keycloak.clientId,
  });

  readonly authenticated = signal(false);
  readonly subject = signal<string | null>(null);
  readonly username = signal<string | null>(null);
  readonly roles = signal<string[]>([]);

  async init(): Promise<void> {
    const authenticated = await this.keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    });
    this.authenticated.set(authenticated);
    if (authenticated) {
      const tokenParsed = this.keycloak.tokenParsed as
        | { sub?: string; preferred_username?: string; realm_access?: { roles?: string[] } }
        | undefined;
      this.subject.set(tokenParsed?.sub ?? null);
      this.username.set(tokenParsed?.preferred_username ?? null);
      this.roles.set(tokenParsed?.realm_access?.roles ?? []);
    }
  }

  async getValidToken(): Promise<string | undefined> {
    try {
      await this.keycloak.updateToken(30);
    } catch {
      await this.keycloak.login();
    }
    return this.keycloak.token;
  }

  hasRole(role: string): boolean {
    return this.roles().includes(role);
  }

  logout(): void {
    this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
