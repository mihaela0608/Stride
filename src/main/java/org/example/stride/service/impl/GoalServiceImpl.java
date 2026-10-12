package org.example.stride.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.model.entity.Goal;
import org.example.stride.repository.GoalRepository;
import org.example.stride.service.GoalService;
import org.example.stride.service.session.CurrentUserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;

    @Override
    public void createGoal(GoalCreationDto goalCreationDto) {
        Goal goal = null;
        modelMapper.map(goalCreationDto, goal);
        goal.setRunnerProfile(currentUserService.getCurrentUser().getRunnerProfile());
        goal.setCreatedAt(LocalDate.now());
        //TODO: Change status

    }
}
