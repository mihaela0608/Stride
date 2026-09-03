package org.example.stride.service;

import org.example.stride.model.dto.UserRegisterDto;
import org.example.stride.repository.UserRepository;

public interface UserService {
    public boolean registerUser(UserRegisterDto dto);
}
