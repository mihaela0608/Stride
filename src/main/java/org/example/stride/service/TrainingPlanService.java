package org.example.stride.service;

import org.example.stride.model.entity.Goal;
import org.example.stride.model.entity.GoalAssessment;

public interface TrainingPlanService {

    void createTrainingPlan(Goal goal, GoalAssessment assessment);
    void generateNewWeeks();
}
