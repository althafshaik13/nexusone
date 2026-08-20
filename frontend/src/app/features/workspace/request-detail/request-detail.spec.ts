import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';

import { RequestDetail } from './request-detail';
import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { WorkflowInstanceResponse } from '../../../core/models/workflow.model';

class KeycloakServiceStub {
  subject = signal<string | null>('approver-1');
}

const instance: WorkflowInstanceResponse = {
  id: 'i1',
  requestType: 'LEAVE_REQUEST',
  title: 'PTO next week',
  description: null,
  status: 'PENDING',
  currentStepOrder: 1,
  steps: [
    {
      stepOrder: 1,
      approverType: 'MANAGER',
      resolvedApproverEmployeeId: 'e2',
      resolvedApproverUserId: 'approver-1',
      status: 'PENDING',
      decidedByUserId: null,
      decidedAt: null,
      comment: null,
    },
  ],
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

describe('RequestDetail', () => {
  let getInstance: ReturnType<typeof vi.fn>;
  let decide: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    getInstance = vi.fn().mockReturnValue(of(instance));
    decide = vi.fn().mockReturnValue(of({ ...instance, status: 'APPROVED' }));

    await TestBed.configureTestingModule({
      imports: [RequestDetail],
      providers: [
        provideRouter([]),
        { provide: WorkflowApiService, useValue: { getInstance, decide } },
        { provide: KeycloakService, useClass: KeycloakServiceStub },
      ],
    }).compileComponents();
  });

  it('loads the instance when the id input is set', () => {
    const fixture = TestBed.createComponent(RequestDetail);
    fixture.componentRef.setInput('id', 'i1');
    fixture.detectChanges();

    expect(getInstance).toHaveBeenCalledWith('i1');
    expect(fixture.componentInstance['instance']()).toEqual(instance);
  });

  it('allows the resolved approver to decide on the current pending step', () => {
    const fixture = TestBed.createComponent(RequestDetail);
    fixture.componentRef.setInput('id', 'i1');
    fixture.detectChanges();

    expect(fixture.componentInstance['canDecide'](instance.steps[0])).toBe(true);
  });

  it('submits a decision with the current comment', () => {
    const fixture = TestBed.createComponent(RequestDetail);
    fixture.componentRef.setInput('id', 'i1');
    fixture.detectChanges();
    fixture.componentInstance['onCommentInput']('Approved, enjoy!');

    fixture.componentInstance['decide']('APPROVE');

    expect(decide).toHaveBeenCalledWith('i1', 1, { decision: 'APPROVE', comment: 'Approved, enjoy!' });
    expect(fixture.componentInstance['instance']()?.status).toBe('APPROVED');
  });
});
