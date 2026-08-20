import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { OrganizationList } from './organization-list';
import { OrganizationApiService } from '../../../core/services/organization-api.service';
import { OrganizationResponse } from '../../../core/models/organization.model';

const organization: OrganizationResponse = { id: 'o1', name: 'Acme Corp', createdAt: '2026-01-01T00:00:00Z' };

describe('OrganizationList', () => {
  let listOrganizations: ReturnType<typeof vi.fn>;

  async function configure(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [OrganizationList],
      providers: [provideRouter([]), { provide: OrganizationApiService, useValue: { listOrganizations } }],
    }).compileComponents();
  }

  it('loads and renders organizations', async () => {
    listOrganizations = vi.fn().mockReturnValue(of([organization]));
    await configure();
    const fixture = TestBed.createComponent(OrganizationList);
    fixture.detectChanges();

    expect(listOrganizations).toHaveBeenCalled();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Acme Corp');
  });

  it('sets an error when loading fails', async () => {
    listOrganizations = vi.fn().mockReturnValue(throwError(() => new Error('boom')));
    await configure();
    const fixture = TestBed.createComponent(OrganizationList);
    fixture.detectChanges();

    expect(fixture.componentInstance['error']()).toBe('Failed to load organizations. Is organization-service running?');
  });
});
