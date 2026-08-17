export interface EmployeeResponse {
  id: string;
  organizationId: string;
  userId: string;
  email: string;
  jobTitle: string | null;
  departmentId: string | null;
  teamId: string | null;
  managerId: string | null;
  createdAt: string;
}

export interface DepartmentResponse {
  id: string;
  organizationId: string;
  name: string;
  createdAt: string;
}

export interface OrganizationResponse {
  id: string;
  name: string;
  createdAt: string;
}

export interface TeamResponse {
  id: string;
  departmentId: string;
  name: string;
  createdAt: string;
}
