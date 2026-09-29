package org.example.stride.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stride.model.enums.Gender;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDto {

    @NotBlank(message = "Името е задължително.")
    @Size(
            min = 2,
            max = 30,
            message = "Името трябва да е между 2 и 30 символа."
    )
    private String firstName;

    @NotBlank(message = "Фамилията е задължителна.")
    @Size(
            min = 2,
            max = 30,
            message = "Фамилията трябва да е между 2 и 30 символа."
    )
    private String lastName;

    @NotBlank(message = "Email-ът е задължителен.")
    @Email(message = "Въведи валиден email.")
    private String email;

    @NotBlank(message = "Паролата е задължителна.")
    @Size(
            min = 5,
            max = 25,
            message = "Паролата трябва да е между 5 и 25 символа."
    )
    private String password;

    @NotNull(message = "Датата на раждане е задължителна.")
    @Past(message = "Датата на раждане трябва да е в миналото.")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate dateOfBirth;

    @NotNull(message = "Избери пол.")
    private Gender gender;
}