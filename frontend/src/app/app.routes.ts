import { Routes } from '@angular/router';

import { roleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'tickets' },
  {
    path: 'tickets',
    loadComponent: () => import('./features/tickets/ticket-list/ticket-list').then((m) => m.TicketList),
  },
  {
    path: 'tickets/new',
    canActivate: [roleGuard('CUSTOMER')],
    loadComponent: () => import('./features/tickets/ticket-create/ticket-create').then((m) => m.TicketCreate),
  },
  {
    path: 'tickets/:id',
    loadComponent: () => import('./features/tickets/ticket-detail/ticket-detail').then((m) => m.TicketDetail),
  },
  {
    path: 'profile',
    loadComponent: () => import('./features/workspace/my-profile/my-profile').then((m) => m.MyProfile),
  },
  {
    path: 'requests',
    loadComponent: () => import('./features/workspace/request-list/request-list').then((m) => m.RequestList),
  },
  {
    path: 'requests/new',
    loadComponent: () =>
      import('./features/workspace/request-create/request-create').then((m) => m.RequestCreate),
  },
  {
    path: 'requests/:id',
    loadComponent: () =>
      import('./features/workspace/request-detail/request-detail').then((m) => m.RequestDetail),
  },
  {
    path: 'approvals',
    loadComponent: () => import('./features/workspace/approval-list/approval-list').then((m) => m.ApprovalList),
  },
  {
    path: 'admin/organizations',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/admin/organization-list/organization-list').then((m) => m.OrganizationList),
  },
  {
    path: 'admin/organizations/new',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/admin/organization-create/organization-create').then((m) => m.OrganizationCreate),
  },
  {
    path: 'admin/organizations/:id',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/admin/organization-detail/organization-detail').then((m) => m.OrganizationDetail),
  },
  {
    path: 'admin/workflow-definitions',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/admin/workflow-definition-list/workflow-definition-list').then(
        (m) => m.WorkflowDefinitionList,
      ),
  },
  {
    path: 'admin/workflow-definitions/new',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/admin/workflow-definition-create/workflow-definition-create').then(
        (m) => m.WorkflowDefinitionCreate,
      ),
  },
];
