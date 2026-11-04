package org.example.stride.model.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Type;

import java.time.LocalDate;

@Getter
@Setter
public class RunUpdateDto {

    @NotNull
    @PastOrPresent
    private LocalDate date;

    private Long workoutId;

    @Positive
    private double distance;

    @Positive
    private int duration;

    @NotNull
    private Type runType;

    @Min(1) @Max(10)
    private int perceivedEffort;

    @Positive
    private Integer averageHeartRate;

    @PositiveOrZero
    private Integer elevationGain;

    @Size(max = 500)
    private String notes;
}
