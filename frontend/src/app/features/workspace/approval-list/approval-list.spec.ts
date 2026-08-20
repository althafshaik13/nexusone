import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ApprovalList } from './approval-list';
import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { WorkflowInstanceResponse } from '../../../core/models/workflow.model';

const pending: WorkflowInstanceResponse = {
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

describe('ApprovalList', () => {
  let listPendingMyApproval: ReturnType<typeof vi.fn>;

  async function configure(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ApprovalList],
      providers: [provideRouter([]), { provide: WorkflowApiService, useValue: { listPendingMyApproval } }],
    }).compileComponents();
  }

  it('loads requests pending the current user approval', async () => {
    listPendingMyApproval = vi.fn().mockReturnValue(of([pending]));
    await configure();
    const fixture = TestBed.createComponent(ApprovalList);
    fixture.detectChanges();

    expect(listPendingMyApproval).toHaveBeenCalled();
    expect(fixture.componentInstance['requests']()).toEqual([pending]);
  });

  it('sets an error when loading fails', async () => {
    listPendingMyApproval = vi.fn().mockReturnValue(throwError(() => new Error('boom')));
    await configure();
    const fixture = TestBed.createComponent(ApprovalList);
    fixture.detectChanges();

    expect(fixture.componentInstance['error']()).toBe('Failed to load approvals. Is workflow-service running?');
  });
});
