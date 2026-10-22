package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.CurrentUserResponseDto;
import org.example.stride.model.dto.UserRegisterDto;
import org.example.stride.model.entity.User;
import org.example.stride.service.UserService;
import org.example.stride.service.session.CurrentUserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody UserRegisterDto dto) {

        boolean registered = userService.registerUser(dto);

        if (!registered) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "User with this email already exists."
                    ));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "Registration is done."
                ));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponseDto> getCurrentUser(){
        User user = currentUserService.getCurrentUser();

        CurrentUserResponseDto dto = new CurrentUserResponseDto();
        modelMapper.map(user, dto);
        dto.setHasRunnerProfile(user.getRunnerProfile() != null);

        return ResponseEntity.ok(dto);
    }
}