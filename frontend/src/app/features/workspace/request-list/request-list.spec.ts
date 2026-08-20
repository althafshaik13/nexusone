import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RequestList } from './request-list';
import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { WorkflowInstanceResponse } from '../../../core/models/workflow.model';

const request: WorkflowInstanceResponse = {
  id: 'i1',
  requestType: 'LEAVE_REQUEST',
  title: 'PTO next week',
  description: null,
  status: 'PENDING',
  currentStepOrder: 1,
  steps: [],
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

describe('RequestList', () => {
  let listMine: ReturnType<typeof vi.fn>;

  async function configure(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [RequestList],
      providers: [provideRouter([]), { provide: WorkflowApiService, useValue: { listMine } }],
    }).compileComponents();
  }

  it('loads and renders the current user requests', async () => {
    listMine = vi.fn().mockReturnValue(of([request]));
    await configure();
    const fixture = TestBed.createComponent(RequestList);
    fixture.detectChanges();

    expect(listMine).toHaveBeenCalled();
    expect(fixture.componentInstance['requests']()).toEqual([request]);
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('PTO next week');
  });

  it('sets an error when loading fails', async () => {
    listMine = vi.fn().mockReturnValue(throwError(() => new Error('boom')));
    await configure();
    const fixture = TestBed.createComponent(RequestList);
    fixture.detectChanges();

    expect(fixture.componentInstance['error']()).toBe('Failed to load your requests. Is workflow-service running?');
  });
});
