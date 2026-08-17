import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApproverType, Decision, WorkflowDefinitionResponse, WorkflowInstanceResponse } from '../models/workflow.model';

export interface CreateWorkflowInstanceRequest {
  requestType: string;
  title: string;
  description: string | null;
}

export interface DecideStepRequest {
  decision: Decision;
  comment: string | null;
}

export interface CreateWorkflowDefinitionStepRequest {
  approverType: ApproverType;
  approverEmployeeId: string | null;
}

export interface CreateWorkflowDefinitionRequest {
  requestType: string;
  name: string;
  steps: CreateWorkflowDefinitionStepRequest[];
}

@Injectable({ providedIn: 'root' })
export class WorkflowApiService {
  private readonly http = inject(HttpClient);
  private readonly definitionsUrl = `${environment.apiBaseUrl}/workflow-definitions`;
  private readonly instancesUrl = `${environment.apiBaseUrl}/workflow-instances`;

  listDefinitions(): Observable<WorkflowDefinitionResponse[]> {
    return this.http.get<WorkflowDefinitionResponse[]>(this.definitionsUrl);
  }

  createDefinition(request: CreateWorkflowDefinitionRequest): Observable<WorkflowDefinitionResponse> {
    return this.http.post<WorkflowDefinitionResponse>(this.definitionsUrl, request);
  }

  submitInstance(request: CreateWorkflowInstanceRequest): Observable<WorkflowInstanceResponse> {
    return this.http.post<WorkflowInstanceResponse>(this.instancesUrl, request);
  }

  getInstance(id: string): Observable<WorkflowInstanceResponse> {
    return this.http.get<WorkflowInstanceResponse>(`${this.instancesUrl}/${id}`);
  }

  listMine(): Observable<WorkflowInstanceResponse[]> {
    return this.http.get<WorkflowInstanceResponse[]>(`${this.instancesUrl}/mine`);
  }

  listPendingMyApproval(): Observable<WorkflowInstanceResponse[]> {
    return this.http.get<WorkflowInstanceResponse[]>(`${this.instancesUrl}/pending-my-approval`);
  }

  decide(instanceId: string, stepOrder: number, request: DecideStepRequest): Observable<WorkflowInstanceResponse> {
    return this.http.post<WorkflowInstanceResponse>(
      `${this.instancesUrl}/${instanceId}/steps/${stepOrder}/decide`,
      request,
    );
  }
}
