
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ModelMapper modelMapper;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();

        userService = new UserServiceImpl(
                userRepository,
                modelMapper,
                passwordEncoder
        );
    }

    private UserRegisterDto createDto() {
        return new UserRegisterDto(
                "Test",
                "Test",
                "test@gmail.com",
                "test123",
                LocalDate.of(1990, 1, 1),
                Gender.MALE
        );
    }

    @Test
    void registerUser_registersAndReturnsTrue_whenCorrectData() {
        UserRegisterDto dto = createDto();

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(dto.getPassword()))
                .thenReturn("encodedPassword");

        boolean result = userService.registerUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();

        assertTrue(result);
        assertEquals(dto.getFirstName(), saved.getFirstName());
        assertEquals(dto.getLastName(), saved.getLastName());
        assertEquals(dto.getEmail(), saved.getEmail());
        assertEquals("encodedPassword", saved.getPassword());
        assertEquals(dto.getDateOfBirth(), saved.getDateOfBirth());
        assertEquals(dto.getGender(), saved.getGender());
        assertEquals(Role.USER, saved.getRole());
        assertEquals(LocalDate.now(), saved.getCreatedAt());

        verify(passwordEncoder).encode(dto.getPassword());
    }

    @Test
    void registerUser_returnsFalse_whenUserIsPresent() {
        UserRegisterDto dto = createDto();

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(new User()));

        boolean result = userService.registerUser(dto);

        assertFalse(result);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
