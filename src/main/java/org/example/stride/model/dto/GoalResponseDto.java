package org.example.stride.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Status;

import java.time.LocalDate;

@Getter
@Setter
public class GoalResponseDto {

     private double targetDistance;

     private int targetTimeSeconds;

     private LocalDate targetDate;
     private Status status;
     private LocalDate createdAt;

     private String event;




}
