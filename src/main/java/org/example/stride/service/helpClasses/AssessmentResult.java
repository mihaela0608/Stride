package org.example.stride.service.helpClasses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Difficulty;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class AssessmentResult {
           private int predictedTimeSeconds;
           private double requiredImprovementPercentage;
           private int estimatedWeeks;
           private LocalDate projectedDate;
           private Difficulty difficulty;
           private String explanation;
}
