package org.example.stride.service;

import org.example.stride.model.dto.UserRegisterDto;
import org.example.stride.repository.UserRepository;

public interface UserService {
     boolean registerUser(UserRegisterDto dto);
}
