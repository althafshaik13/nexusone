import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { WorkflowApiService } from './workflow-api.service';
import { environment } from '../../../environments/environment';
import { WorkflowDefinitionResponse, WorkflowInstanceResponse, WorkflowStatsResponse } from '../models/workflow.model';

describe('WorkflowApiService', () => {
  let service: WorkflowApiService;
  let httpMock: HttpTestingController;
  const definitionsUrl = `${environment.apiBaseUrl}/workflow-definitions`;
  const instancesUrl = `${environment.apiBaseUrl}/workflow-instances`;

  const instance: WorkflowInstanceResponse = {
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

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(WorkflowApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('listDefinitions fetches all definitions', () => {
    const definition: WorkflowDefinitionResponse = {
      id: 'd1',
      requestType: 'LEAVE_REQUEST',
      name: 'Leave Request',
      steps: [],
      createdAt: '2026-01-01T00:00:00Z',
    };
    let result: WorkflowDefinitionResponse[] | undefined;
    service.listDefinitions().subscribe((r) => (result = r));

    const req = httpMock.expectOne(definitionsUrl);
    expect(req.request.method).toBe('GET');
    req.flush([definition]);

    expect(result).toEqual([definition]);
  });

  it('createDefinition posts the request body', () => {
    const request = {
      requestType: 'LEAVE_REQUEST',
      name: 'Leave Request',
      steps: [{ approverType: 'MANAGER' as const, approverEmployeeId: null }],
    };
    service.createDefinition(request).subscribe();

    const req = httpMock.expectOne(definitionsUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({});
  });

  it('submitInstance posts to the instances url', () => {
    const request = { requestType: 'LEAVE_REQUEST', title: 'PTO', description: null };
    let result: WorkflowInstanceResponse | undefined;
    service.submitInstance(request).subscribe((r) => (result = r));

    const req = httpMock.expectOne(instancesUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(instance);

    expect(result).toEqual(instance);
  });

  it('getInstance fetches by id', () => {
    service.getInstance('i1').subscribe();

    const req = httpMock.expectOne(`${instancesUrl}/i1`);
    expect(req.request.method).toBe('GET');
    req.flush(instance);
  });

  it('listMine fetches the mine endpoint', () => {
    let result: WorkflowInstanceResponse[] | undefined;
    service.listMine().subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${instancesUrl}/mine`);
    expect(req.request.method).toBe('GET');
    req.flush([instance]);

    expect(result).toEqual([instance]);
  });

  it('listPendingMyApproval fetches the pending-my-approval endpoint', () => {
    service.listPendingMyApproval().subscribe();

    const req = httpMock.expectOne(`${instancesUrl}/pending-my-approval`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('decide posts decision and comment to the step decide endpoint', () => {
    const request = { decision: 'APPROVE' as const, comment: 'Looks good' };
    service.decide('i1', 2, request).subscribe();

    const req = httpMock.expectOne(`${instancesUrl}/i1/steps/2/decide`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(instance);
  });

  it('getStats fetches aggregate stats', () => {
    const stats: WorkflowStatsResponse = {
      countByStatus: { PENDING: 1 },
      countByRequestType: { LEAVE_REQUEST: 1 },
      averageDecisionHours: null,
    };
    let result: WorkflowStatsResponse | undefined;
    service.getStats().subscribe((r) => (result = r));

    const req = httpMock.expectOne(`${instancesUrl}/stats`);
    expect(req.request.method).toBe('GET');
    req.flush(stats);

    expect(result).toEqual(stats);
  });
});
