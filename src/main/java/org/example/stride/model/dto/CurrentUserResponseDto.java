package org.example.stride.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Gender;

import java.time.LocalDate;

@Getter
@Setter
public class CurrentUserResponseDto {

    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    private LocalDate createdAt;
    private String role;
    private boolean hasRunnerProfile;
}