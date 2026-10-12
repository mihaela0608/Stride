package org.example.stride.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import java.time.Duration;
import java.time.LocalDate;

public class GoalCreationDto {

        @NotNull
        @Positive
        private double targetDistance;

        @NotNull
        private Duration targetTime;

        private LocalDate targetDate;

}
