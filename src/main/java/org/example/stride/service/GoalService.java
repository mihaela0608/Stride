package org.example.stride.service;

import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.model.dto.GoalResponseDto;

import java.util.List;

public interface GoalService {
    void createGoal(GoalCreationDto goalCreationDto);
    boolean replaceGoal(GoalCreationDto goalCreationDto);

    boolean completeActiveGoal();

    List<GoalResponseDto> getAll();
}
