package org.example.stride.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;


import java.time.Duration;
import java.time.LocalDate;

@Getter
@Setter
public class GoalCreationDto {

        @NotNull
        @Positive
        private double targetDistance;

        @NotNull
        @Min(0)
        private Integer targetHours;

        @NotNull
        @Min(0)
        @Max(59)
        private Integer targetMinutes;

        @NotNull
        @Min(0)
        @Max(59)
        private Integer targetSeconds;

        private LocalDate targetDate;

        private String event;


}
