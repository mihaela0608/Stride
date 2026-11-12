package org.example.stride.service.impl;

import org.example.stride.model.dto.UserRegisterDto;
import org.example.stride.model.entity.User;
import org.example.stride.model.enums.Gender;
import org.example.stride.model.enums.Role;
import org.example.stride.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    private ModelMapper modelMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp(){
        modelMapper = new ModelMapper();

        userService = new UserServiceImpl(userRepository, modelMapper, passwordEncoder);
    }

    @Test
    void registerUser_registersAndReturnsTrue_whenCorrectData(){
        UserRegisterDto dto = new UserRegisterDto("Test", "Test", "test@gmail.com", "test123", LocalDate.of(1990, 01, 01), Gender.MALE);
        Mockito.when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(dto.getPassword())).thenReturn(dto.getPassword());

        boolean result = userService.registerUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals(dto.getFirstName(), saved.getFirstName());
        assertEquals(dto.getLastName(), saved.getLastName());
        assertEquals(dto.getEmail(), saved.getEmail());
        assertEquals(dto.getPassword(), saved.getPassword());
        assertEquals(dto.getDateOfBirth(), saved.getDateOfBirth());
        assertEquals(dto.getGender(), saved.getGender());
        assertEquals(Role.USER, saved.getRole());
        assertTrue(result);
    }

    @Test
    void registerUser_returnsFalse_whenUserIsPresent(){
        UserRegisterDto dto = new UserRegisterDto("Test", "Test", "test@gmail.com", "test123", LocalDate.of(1990, 01, 01), Gender.MALE);
        Mockito.when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertFalse(userService.registerUser(dto));
    }



}
