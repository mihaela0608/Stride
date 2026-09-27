package org.example.stride.service.impl;

import org.example.stride.model.dto.UserRegisterDto;
import org.example.stride.model.entity.User;
import org.example.stride.model.enums.Role;
import org.example.stride.repository.UserRepository;
import org.example.stride.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean registerUser(UserRegisterDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return false;
        }
        userRepository.save(createUser(dto));
        return true;
    }

    private User createUser(UserRegisterDto dto) {
        User user = modelMapper.map(dto, User.class);

        user.setCreatedAt(LocalDate.now());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        return user;
    }
}
