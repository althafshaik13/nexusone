package dev.nexusone.ticket_service.repository;

import dev.nexusone.ticket_service.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, UUID> {
}
