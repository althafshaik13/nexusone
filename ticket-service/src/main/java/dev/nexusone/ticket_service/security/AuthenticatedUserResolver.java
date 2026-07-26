package dev.nexusone.ticket_service.security;

import dev.nexusone.ticket_service.domain.AppUser;
import dev.nexusone.ticket_service.domain.UserRole;
import dev.nexusone.ticket_service.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AuthenticatedUserResolver {

    private final UserRepository userRepository;

    public AuthenticatedUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AppUser resolve(Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        return userRepository.findById(id).orElseGet(() -> provision(id, jwt));
    }

    private AppUser provision(UUID id, Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        UserRole role = resolveRole(jwt);
        AppUser user = new AppUser(id, email != null ? email : jwt.getSubject(), role);
        return userRepository.save(user);
    }

    @SuppressWarnings("unchecked")
    private UserRole resolveRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = realmAccess == null ? List.of() : (List<String>) realmAccess.getOrDefault("roles", List.of());
        if (roles.contains("ADMIN")) {
            return UserRole.ADMIN;
        }
        if (roles.contains("AGENT")) {
            return UserRole.AGENT;
        }
        return UserRole.CUSTOMER;
    }
}
