package org.example.stride.service;

import org.example.stride.model.dto.TrainingPlanViewDto;
import org.example.stride.model.dto.TrainingWeekViewDto;
import org.example.stride.model.dto.WorkoutsViewDto;
import org.example.stride.model.entity.Goal;
import org.example.stride.model.entity.GoalAssessment;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TrainingPlanService {

    void createTrainingPlan(Goal goal, GoalAssessment assessment);
    void generateNewWeeks();

    TrainingPlanViewDto getCurrentTrainingPlan();


    List<WorkoutsViewDto> getAllWorkouts();

    List<TrainingWeekViewDto> getAllTrainingWeeks();
}
