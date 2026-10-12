package org.example.stride.service;

import org.example.stride.model.dto.GoalCreationDto;

public interface GoalService {
    void createGoal(GoalCreationDto goalCreationDto);
}
