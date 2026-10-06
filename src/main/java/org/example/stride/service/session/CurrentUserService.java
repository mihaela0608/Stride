package org.example.stride.service.session;

import lombok.AllArgsConstructor;
import org.example.stride.model.entity.User;
import org.example.stride.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException("Current user not found")
                );
    }

}
