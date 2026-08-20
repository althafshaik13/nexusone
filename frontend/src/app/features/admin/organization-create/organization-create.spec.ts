import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { OrganizationCreate } from './organization-create';
import { OrganizationApiService } from '../../../core/services/organization-api.service';
import { OrganizationResponse } from '../../../core/models/organization.model';

const organization: OrganizationResponse = { id: 'o1', name: 'Acme Corp', createdAt: '2026-01-01T00:00:00Z' };

describe('OrganizationCreate', () => {
  let createOrganization: ReturnType<typeof vi.fn>;
  let router: Router;

  beforeEach(async () => {
    createOrganization = vi.fn().mockReturnValue(of(organization));
    await TestBed.configureTestingModule({
      imports: [OrganizationCreate],
      providers: [provideRouter([]), { provide: OrganizationApiService, useValue: { createOrganization } }],
    }).compileComponents();
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
  });

  it('does not submit when the form is invalid', () => {
    const fixture = TestBed.createComponent(OrganizationCreate);
    fixture.detectChanges();

    fixture.componentInstance['submit']();

    expect(createOrganization).not.toHaveBeenCalled();
  });

  it('submits and navigates to the new organization detail page', () => {
    const fixture = TestBed.createComponent(OrganizationCreate);
    fixture.detectChanges();
    fixture.componentInstance['form'].setValue({ name: 'Acme Corp' });

    fixture.componentInstance['submit']();

    expect(createOrganization).toHaveBeenCalledWith({ name: 'Acme Corp' });
    expect(router.navigate).toHaveBeenCalledWith(['/admin/organizations', 'o1']);
  });

  it('surfaces an error when creation fails', () => {
    createOrganization.mockReturnValue(throwError(() => new Error('boom')));
    const fixture = TestBed.createComponent(OrganizationCreate);
    fixture.detectChanges();
    fixture.componentInstance['form'].setValue({ name: 'Acme Corp' });

    fixture.componentInstance['submit']();

    expect(fixture.componentInstance['error']()).toBe('Failed to create organization.');
  });
});
