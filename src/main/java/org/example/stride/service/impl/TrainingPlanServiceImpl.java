package org.example.stride.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.entity.Goal;
import org.example.stride.model.entity.GoalAssessment;
import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.entity.TrainingPlan;
import org.example.stride.model.enums.Status;
import org.example.stride.repository.GoalRepository;
import org.example.stride.repository.TrainingPlanRepository;
import org.example.stride.service.AdaptivePlanService;
import org.example.stride.service.TrainingPlanService;
import org.example.stride.service.session.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final AdaptivePlanService adaptivePlanService;
    private final CurrentUserService currentUserService;
    private final GoalRepository goalRepository;


    @Override
    @Transactional
    public void createTrainingPlan(Goal goal, GoalAssessment assessment) {
        if (goal.getTargetDate().isBefore(LocalDate.now().plusWeeks(assessment.getEstimatedWeeks()))){
            throw new IllegalStateException("Current goal is not compatible with your predictions.");
        }

        TrainingPlan trainingPlan = new TrainingPlan();
        trainingPlan.setStatus(Status.ACTIVE);
        trainingPlan.setGoal(goal);
        trainingPlan.setStartDate(LocalDate.now());
        trainingPlan.setEndDate(LocalDate.now().plusWeeks(assessment.getEstimatedWeeks()));
        trainingPlan.setWeeks(assessment.getEstimatedWeeks());

        trainingPlanRepository.save(trainingPlan);
        adaptivePlanService.generateOrUpdateNextWindow(trainingPlan);
    }

    @Override
    @Transactional
    public void generateNewWeeks() {
        RunnerProfile profile = currentUserService.getCurrentUser().getRunnerProfile();
        if (profile == null) {
            throw new IllegalStateException("Create a runner profile first.");
        }

        Goal goal = goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("You need an active goal."));

        TrainingPlan plan = trainingPlanRepository.findByGoal(goal)
                .orElseThrow(() -> new IllegalStateException("No training plan for the active goal."));

        adaptivePlanService.generateOrUpdateNextWindow(plan);
    }




}
