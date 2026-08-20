import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { Analytics } from './analytics';
import { TicketApiService } from '../../../core/services/ticket-api.service';
import { WorkflowApiService } from '../../../core/services/workflow-api.service';
import { TicketStatsResponse } from '../../../core/models/ticket.model';
import { WorkflowStatsResponse } from '../../../core/models/workflow.model';

const ticketStats: TicketStatsResponse = {
  countByStatus: { CREATED: 4, ASSIGNED: 2 },
  averageResolutionHours: 2.5,
};

const workflowStats: WorkflowStatsResponse = {
  countByStatus: { PENDING: 1, APPROVED: 3 },
  countByRequestType: { LEAVE_REQUEST: 4 },
  averageDecisionHours: 0.5,
};

describe('Analytics', () => {
  let getTicketStats: ReturnType<typeof vi.fn>;
  let getWorkflowStats: ReturnType<typeof vi.fn>;

  async function configure(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [Analytics],
      providers: [
        { provide: TicketApiService, useValue: { getStats: getTicketStats } },
        { provide: WorkflowApiService, useValue: { getStats: getWorkflowStats } },
      ],
    }).compileComponents();
  }

  beforeEach(() => {
    getTicketStats = vi.fn().mockReturnValue(of(ticketStats));
    getWorkflowStats = vi.fn().mockReturnValue(of(workflowStats));
  });

  it('loads ticket and workflow stats on creation', async () => {
    await configure();
    const fixture = TestBed.createComponent(Analytics);
    fixture.detectChanges();

    expect(getTicketStats).toHaveBeenCalled();
    expect(getWorkflowStats).toHaveBeenCalled();
    expect(fixture.componentInstance['ticketStats']()).toEqual(ticketStats);
    expect(fixture.componentInstance['workflowStats']()).toEqual(workflowStats);
  });

  it('converts counts into sorted percentage bars', async () => {
    await configure();
    const fixture = TestBed.createComponent(Analytics);
    fixture.detectChanges();

    const bars = fixture.componentInstance['ticketStatusBars']();
    expect(bars).toEqual([
      { label: 'CREATED', count: 4, percent: 100 },
      { label: 'ASSIGNED', count: 2, percent: 50 },
    ]);
  });

  it('formats hours below one as minutes, and null as no data', async () => {
    await configure();
    const fixture = TestBed.createComponent(Analytics);
    fixture.detectChanges();

    expect(fixture.componentInstance['formatHours'](null)).toBe('No data yet');
    expect(fixture.componentInstance['formatHours'](0.5)).toBe('30 min');
    expect(fixture.componentInstance['formatHours'](2.5)).toBe('2.5 hours');
  });

  it('sets an error when ticket stats fail to load', async () => {
    getTicketStats = vi.fn().mockReturnValue(throwError(() => new Error('boom')));
    await configure();
    const fixture = TestBed.createComponent(Analytics);
    fixture.detectChanges();

    expect(fixture.componentInstance['error']()).toBe('Failed to load ticket analytics.');
  });
});
