package org.example.stride.model.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


import java.time.Duration;
import java.time.LocalDate;

@Getter
@Setter
public class GoalCreationDto {

        @NotNull
        @Positive
        private Double targetDistance;

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

        @Future
        private LocalDate targetDate;

        private String event;


}
