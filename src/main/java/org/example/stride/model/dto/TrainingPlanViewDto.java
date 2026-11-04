package org.example.stride.model.dto;


import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Status;

import java.time.LocalDate;

@Getter
@Setter
public class TrainingPlanViewDto {
    private Long goalId;

    private LocalDate startDate;

    private LocalDate endDate;

    private int weeks;

    private Status status;
}
