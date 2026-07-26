import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { KeycloakService } from './keycloak.service';

export function roleGuard(role: string): CanActivateFn {
  return () => {
    const auth = inject(KeycloakService);
    const router = inject(Router);
    return auth.hasRole(role) ? true : router.createUrlTree(['/tickets']);
  };
}
