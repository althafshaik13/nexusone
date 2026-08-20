import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { WorkflowDefinitionList } from './workflow-definition-list';
import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { WorkflowDefinitionResponse } from '../../../core/models/workflow.model';

const definition: WorkflowDefinitionResponse = {
  id: 'd1',
  requestType: 'LEAVE_REQUEST',
  name: 'Leave Request',
  steps: [],
  createdAt: '2026-01-01T00:00:00Z',
};

describe('WorkflowDefinitionList', () => {
  let listDefinitions: ReturnType<typeof vi.fn>;

  async function configure(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [WorkflowDefinitionList],
      providers: [provideRouter([]), { provide: WorkflowApiService, useValue: { listDefinitions } }],
    }).compileComponents();
  }

  it('loads and renders workflow definitions', async () => {
    listDefinitions = vi.fn().mockReturnValue(of([definition]));
    await configure();
    const fixture = TestBed.createComponent(WorkflowDefinitionList);
    fixture.detectChanges();

    expect(listDefinitions).toHaveBeenCalled();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Leave Request');
  });

  it('sets an error when loading fails', async () => {
    listDefinitions = vi.fn().mockReturnValue(throwError(() => new Error('boom')));
    await configure();
    const fixture = TestBed.createComponent(WorkflowDefinitionList);
    fixture.detectChanges();

    expect(fixture.componentInstance['error']()).toBe('Failed to load workflow definitions. Is workflow-service running?');
  });
});
