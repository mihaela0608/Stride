package org.example.stride.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stride.model.enums.ExperienceLevel;

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

    @PositiveOrZero(message = "Пробегът не може да е отрицателен.")
    private double averageWeeklyDistance;

    @PositiveOrZero(message = "Дистанцията не може да е отрицателна.")
    private double longestRecentRun;
}
