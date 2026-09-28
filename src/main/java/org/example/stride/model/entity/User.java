package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Gender;
import org.example.stride.model.enums.Role;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity{

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @OneToOne(mappedBy = "user")
    private RunnerProfile runnerProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
