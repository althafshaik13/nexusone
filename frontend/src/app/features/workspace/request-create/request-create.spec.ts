import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RequestCreate } from './request-create';
import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { WorkflowDefinitionResponse, WorkflowInstanceResponse } from '../../../core/models/workflow.model';

const definition: WorkflowDefinitionResponse = {
  id: 'd1',
  requestType: 'LEAVE_REQUEST',
  name: 'Leave Request',
  steps: [],
  createdAt: '2026-01-01T00:00:00Z',
};

const instance: WorkflowInstanceResponse = {
  id: 'i1',
  requestType: 'LEAVE_REQUEST',
  title: 'PTO',
  description: null,
  status: 'PENDING',
  currentStepOrder: 1,
  steps: [],
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

describe('RequestCreate', () => {
  let listDefinitions: ReturnType<typeof vi.fn>;
  let submitInstance: ReturnType<typeof vi.fn>;
  let router: Router;

  async function configure(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [RequestCreate],
      providers: [
        provideRouter([]),
        { provide: WorkflowApiService, useValue: { listDefinitions, submitInstance } },
      ],
    }).compileComponents();
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
  }

  beforeEach(() => {
    listDefinitions = vi.fn().mockReturnValue(of([definition]));
    submitInstance = vi.fn().mockReturnValue(of(instance));
  });

  it('loads definitions and pre-fills the request type with the first one', async () => {
    await configure();
    const fixture = TestBed.createComponent(RequestCreate);
    fixture.detectChanges();

    expect(listDefinitions).toHaveBeenCalled();
    expect(fixture.componentInstance['form'].controls.requestType.value).toBe('LEAVE_REQUEST');
  });

  it('submits the request and navigates to its detail page on success', async () => {
    await configure();
    const fixture = TestBed.createComponent(RequestCreate);
    fixture.detectChanges();
    fixture.componentInstance['form'].patchValue({ title: 'PTO next week' });

    fixture.componentInstance['submit']();

    expect(submitInstance).toHaveBeenCalledWith({ requestType: 'LEAVE_REQUEST', title: 'PTO next week', description: null });
    expect(router.navigate).toHaveBeenCalledWith(['/requests', 'i1']);
  });

  it('sets a load error when fetching definitions fails', async () => {
    listDefinitions = vi.fn().mockReturnValue(throwError(() => new Error('boom')));
    await configure();
    const fixture = TestBed.createComponent(RequestCreate);
    fixture.detectChanges();

    expect(fixture.componentInstance['loadError']()).toBe('Failed to load request types. Is workflow-service running?');
  });
});
