package org.example.stride.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.TrainingPlanViewDto;
import org.example.stride.model.dto.TrainingWeekViewDto;
import org.example.stride.model.dto.WorkoutsViewDto;
import org.example.stride.model.entity.*;
import org.example.stride.model.enums.Status;
import org.example.stride.repository.GoalRepository;
import org.example.stride.repository.TrainingPlanRepository;
import org.example.stride.repository.TrainingWeekRepository;
import org.example.stride.repository.WorkoutRepository;
import org.example.stride.service.AdaptivePlanService;
import org.example.stride.service.TrainingPlanService;
import org.example.stride.service.session.CurrentUserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final AdaptivePlanService adaptivePlanService;
    private final CurrentUserService currentUserService;
    private final GoalRepository goalRepository;
    private final ModelMapper modelMapper;
    private final TrainingWeekRepository trainingWeekRepository;
    private final WorkoutRepository workoutRepository;


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
        RunnerProfile profile = getRunnerProfile();
        if (profile == null) {
            throw new IllegalStateException("Create a runner profile first.");
        }

        Goal goal = goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("You need an active goal."));

        TrainingPlan plan = trainingPlanRepository.findByGoal(goal)
                .orElseThrow(() -> new IllegalStateException("No training plan for the active goal."));

        adaptivePlanService.generateOrUpdateNextWindow(plan);
    }

    @Override
    public TrainingPlanViewDto getCurrentTrainingPlan() {
        RunnerProfile runnerProfile = getRunnerProfile();
        Optional<Goal> optionalGoal = goalRepository.findByRunnerProfileAndStatus(runnerProfile, Status.ACTIVE);

        if (optionalGoal.isEmpty()){
            throw new IllegalStateException("You need to have goal to have a training plan!");
        }

        TrainingPlan trainingPlan = trainingPlanRepository.findByGoal(optionalGoal.get()).get();
        TrainingPlanViewDto dto = new TrainingPlanViewDto();
        modelMapper.map(trainingPlan, dto);
        return dto;
    }

    @Override
    public List<WorkoutsViewDto> getAllWorkouts() {
        RunnerProfile runnerProfile = getRunnerProfile();
        Optional<Goal> optionalGoal = goalRepository.findByRunnerProfileAndStatus(runnerProfile, Status.ACTIVE);
        if (optionalGoal.isEmpty()){
            throw new IllegalStateException("You need to have goal to have a workout!");
        }

        TrainingPlan trainingPlan = trainingPlanRepository.findByGoal(optionalGoal.get()).get();
        List<TrainingWeek> allWeeks = trainingWeekRepository.findAllByTrainingPlanOrderByWeekNumberAsc(trainingPlan);
        List<WorkoutsViewDto> dtos = new ArrayList<>();
        allWeeks.forEach(w -> {
                    List<Workout> workoutsForWeek = workoutRepository.findAllByTrainingWeek(w);
                    workoutsForWeek.forEach(c -> {
                        WorkoutsViewDto dto = new WorkoutsViewDto();
                        modelMapper.map(c, dto);
                        dtos.add(dto);
                    });
                });
        return dtos;
    }

    @Override
    public List<TrainingWeekViewDto> getAllTrainingWeeks() {
        RunnerProfile runnerProfile = getRunnerProfile();
        Optional<Goal> optionalGoal = goalRepository.findByRunnerProfileAndStatus(runnerProfile, Status.ACTIVE);
        if (optionalGoal.isEmpty()){
            throw new IllegalStateException("You need to have goal to have training weeks!");
        }

        return trainingWeekRepository.findAllByTrainingPlanOrderByWeekNumberAsc(trainingPlanRepository.findByGoal(optionalGoal.get()).get())
                .stream().map(w -> {
                    TrainingWeekViewDto dto = new TrainingWeekViewDto();
                    modelMapper.map(w, dto);
                    return dto;
                }).toList();
    }

    @Override
    public List<WorkoutsViewDto> getWeeklyWorkouts(int number) {
        RunnerProfile runnerProfile = getRunnerProfile();
        Optional<Goal> optionalGoal = goalRepository.findByRunnerProfileAndStatus(runnerProfile, Status.ACTIVE);
        if (optionalGoal.isEmpty()){
            throw new IllegalStateException("You need to have goal to have training weeks!");
        }

        Optional<TrainingWeek> week = trainingWeekRepository.findByTrainingPlanAndWeekNumber(trainingPlanRepository.findByGoal(optionalGoal.get()).get(), number);

        if (week.isEmpty()){
            throw new IllegalStateException("Invalid week!");
        }

        return workoutRepository.findAllByTrainingWeek(week.get()).stream().map(w -> {
            WorkoutsViewDto dto = new WorkoutsViewDto();
            modelMapper.map(w, dto);
            return dto;
        }).toList();
    }

    private RunnerProfile getRunnerProfile() {
        RunnerProfile runnerProfile = currentUserService.getCurrentUser().getRunnerProfile();
        if (runnerProfile == null){
            throw new IllegalStateException("You need runner profile to work with training plans!");
        }
        return runnerProfile;
    }


}
