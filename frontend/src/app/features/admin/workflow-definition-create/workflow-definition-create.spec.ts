import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { WorkflowDefinitionCreate } from './workflow-definition-create';
import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { WorkflowDefinitionResponse } from '../../../core/models/workflow.model';

const created: WorkflowDefinitionResponse = {
  id: 'd1',
  requestType: 'LEAVE_REQUEST',
  name: 'Leave Request',
  steps: [],
  createdAt: '2026-01-01T00:00:00Z',
};

describe('WorkflowDefinitionCreate', () => {
  let createDefinition: ReturnType<typeof vi.fn>;
  let router: Router;

  beforeEach(async () => {
    createDefinition = vi.fn().mockReturnValue(of(created));
    await TestBed.configureTestingModule({
      imports: [WorkflowDefinitionCreate],
      providers: [provideRouter([]), { provide: WorkflowApiService, useValue: { createDefinition } }],
    }).compileComponents();
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
  });

  it('starts with a single step', () => {
    const fixture = TestBed.createComponent(WorkflowDefinitionCreate);
    fixture.detectChanges();

    expect(fixture.componentInstance['steps'].length).toBe(1);
  });

  it('adds and removes steps, never going below one', () => {
    const fixture = TestBed.createComponent(WorkflowDefinitionCreate);
    fixture.detectChanges();

    fixture.componentInstance['addStep']();
    expect(fixture.componentInstance['steps'].length).toBe(2);

    fixture.componentInstance['removeStep'](1);
    expect(fixture.componentInstance['steps'].length).toBe(1);

    fixture.componentInstance['removeStep'](0);
    expect(fixture.componentInstance['steps'].length).toBe(1);
  });

  it('submits with MANAGER steps carrying no approverEmployeeId', () => {
    const fixture = TestBed.createComponent(WorkflowDefinitionCreate);
    fixture.detectChanges();
    fixture.componentInstance['form'].patchValue({ requestType: 'LEAVE_REQUEST', name: 'Leave Request' });

    fixture.componentInstance['submit']();

    expect(createDefinition).toHaveBeenCalledWith({
      requestType: 'LEAVE_REQUEST',
      name: 'Leave Request',
      steps: [{ approverType: 'MANAGER', approverEmployeeId: null }],
    });
    expect(router.navigate).toHaveBeenCalledWith(['/admin/workflow-definitions']);
  });

  it('carries approverEmployeeId only for SPECIFIC_EMPLOYEE steps', () => {
    const fixture = TestBed.createComponent(WorkflowDefinitionCreate);
    fixture.detectChanges();
    fixture.componentInstance['form'].patchValue({ requestType: 'LEAVE_REQUEST', name: 'Leave Request' });
    fixture.componentInstance['steps'].at(0).patchValue({ approverType: 'SPECIFIC_EMPLOYEE', approverEmployeeId: 'e1' });

    fixture.componentInstance['submit']();

    expect(createDefinition).toHaveBeenCalledWith({
      requestType: 'LEAVE_REQUEST',
      name: 'Leave Request',
      steps: [{ approverType: 'SPECIFIC_EMPLOYEE', approverEmployeeId: 'e1' }],
    });
  });

  it('surfaces an error when creation fails', () => {
    createDefinition.mockReturnValue(throwError(() => new Error('boom')));
    const fixture = TestBed.createComponent(WorkflowDefinitionCreate);
    fixture.detectChanges();
    fixture.componentInstance['form'].patchValue({ requestType: 'LEAVE_REQUEST', name: 'Leave Request' });

    fixture.componentInstance['submit']();

    expect(fixture.componentInstance['error']()).toBe('Failed to create workflow definition.');
  });
});
