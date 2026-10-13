package org.example.stride.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stride.model.enums.Day;
import org.example.stride.model.enums.ExperienceLevel;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RunnerProfileDto {

    @NotNull(message = "Избери нивото си.")
    private ExperienceLevel experienceLevel;

    @Min(value = 0, message = "Броят тренировки не може да е отрицателен.")
    @Max(value = 14, message = "Въведи реалистичен брой тренировки.")
    private int runsPerWeek;

    @Positive(message = "Пробегът не може да е отрицателен.")
    private double averageWeeklyDistance;

    @Positive(message = "Дистанцията не може да е отрицателна.")
    private double longestRecentRun;

    @NotNull
    private Set<Day> preferredRunningDays;
}
