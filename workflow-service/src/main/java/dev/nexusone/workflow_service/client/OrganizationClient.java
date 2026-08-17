package dev.nexusone.workflow_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class OrganizationClient {

    private final RestClient restClient;

    public OrganizationClient(RestClient.Builder builder,
                               @Value("${nexusone.organization-service.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public EmployeeDto getMyEmployee(String bearerToken) {
        return restClient.get()
                .uri("/api/employees/me")
                .header("Authorization", bearerToken)
                .retrieve()
                .body(EmployeeDto.class);
    }

    public EmployeeDto getEmployee(UUID employeeId, String bearerToken) {
        return restClient.get()
                .uri("/api/employees/{id}", employeeId)
                .header("Authorization", bearerToken)
                .retrieve()
                .body(EmployeeDto.class);
    }

    public record EmployeeDto(
            UUID id,
            UUID organizationId,
            UUID userId,
            String email,
            String jobTitle,
            UUID departmentId,
            UUID teamId,
            UUID managerId,
            String createdAt
    ) {
    }
}
