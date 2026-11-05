package org.example.stride.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Status;
import org.example.stride.model.enums.Type;

import java.time.LocalDate;

@Getter
@Setter
public class WorkoutsViewDto {
    private LocalDate date;

    private Type type;

    private double distance;

    private int durationSeconds;

    private String description;

    private int intensity;

    private Status status;
}
