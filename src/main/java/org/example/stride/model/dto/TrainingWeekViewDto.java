package org.example.stride.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Focus;

import java.time.LocalDate;

@Getter
@Setter
public class TrainingWeekViewDto {
    private int weekNumber;

    private double targetWeeklyDistance;

    private LocalDate startDate;

    private LocalDate endDate;

    private Focus focus;
}
