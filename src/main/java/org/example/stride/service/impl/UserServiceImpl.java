package org.example.stride.service.impl;

import org.example.stride.model.dto.UserRegisterDto;
import org.example.stride.repository.UserRepository;
import org.example.stride.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean registerUser(UserRegisterDto dto) {
        return false;
    }
}
