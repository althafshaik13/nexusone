package dev.nexusone.ticket_service.service;

import dev.nexusone.ticket_service.domain.AppUser;
import dev.nexusone.ticket_service.dto.CreateUserRequest;
import dev.nexusone.ticket_service.exception.UserNotFoundException;
import dev.nexusone.ticket_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AppUser createUser(CreateUserRequest request) {
        AppUser user = new AppUser(request.email(), request.role());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser getUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}
