package org.example.stride.model.dto;


import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Type;

import java.time.LocalDate;

@Getter
@Setter
public class RunResponseDto {
    private LocalDate date;

    private double distance;

    private int duration;

    private double averagePace;

    private Integer averageHeartRate;

    private Integer elevationGain;

    private Type runType;

    private int perceivedEffort;

    private String notes;
}
