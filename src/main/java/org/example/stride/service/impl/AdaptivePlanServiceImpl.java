package org.example.stride.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.stride.model.entity.*;
import org.example.stride.model.enums.Day;
import org.example.stride.model.enums.Type;
import org.example.stride.repository.RunRepository;
import org.example.stride.repository.TrainingWeekRepository;
import org.example.stride.service.AdaptivePlanService;
import org.example.stride.service.session.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdaptivePlanServiceImpl implements AdaptivePlanService {
    private final TrainingWeekRepository trainingWeekRepository;
    private final RunRepository runRepository;
    private final CurrentUserService currentUserService;


    @Override
    public void generateOrUpdateNextWindow(TrainingPlan plan) {
        RunnerProfile profile = currentUserService.getCurrentUser().getRunnerProfile();
        LocalDate today = LocalDate.now();

        double fatigueAdjustmentFactor = calculateFatigueFactor(profile);
        int currentWeekNumber = determineCurrentWeek(plan, today);

        for (int i = 0; i < 2; i++) {
            int targetWeekNum = currentWeekNumber + i;
            if (targetWeekNum > plan.getWeeks()) break;

            if (trainingWeekRepository.findByTrainingPlanAndWeekNumber(plan, targetWeekNum).isPresent()) {
                continue;
            }

            TrainingWeek week = createAdaptiveWeek(plan, profile, targetWeekNum, fatigueAdjustmentFactor);
            trainingWeekRepository.save(week);

        }
    }

    private TrainingWeek createAdaptiveWeek(TrainingPlan plan, RunnerProfile profile, int targetWeekNum, double fatigueAdjustmentFactor) {
        TrainingWeek trainingWeek = new TrainingWeek();
        trainingWeek.setTrainingPlan(plan);
        trainingWeek.setWeekNumber(targetWeekNum);

        LocalDate weekStartDate = plan.getStartDate().plusWeeks(targetWeekNum - 1);
        trainingWeek.setStartDate(weekStartDate);

        boolean isRecovery = (targetWeekNum % 4 == 0);
        double baseVolume = profile.getAverageWeeklyDistance() * Math.pow(1.05, targetWeekNum - 1);
        double targetVolume = isRecovery ? baseVolume * 0.75 : baseVolume;

        targetVolume = targetVolume * fatigueAdjustmentFactor;

        trainingWeek.setTargetWeeklyDistance(Math.round(targetVolume * 10.0) / 10.0);

        List<Workout> workouts = buildWorkoutsForDays(trainingWeek, profile, targetVolume);
        trainingWeek.setWorkouts(workouts);

        return trainingWeek;
    }

    private List<Workout> buildWorkoutsForDays(TrainingWeek trainingWeek, RunnerProfile profile, double targetVolume) {
        List<Workout> workouts = new ArrayList<>();
        Set<Day> preferredDays = profile.getPreferredRunningDays();

        if (preferredDays.isEmpty()) return workouts;

        List<DayOfWeek> sortedDays = preferredDays.stream()
                .map(d -> DayOfWeek.valueOf(d.name()))
                .sorted()
                .toList();

        double longRunDist = Math.round((targetVolume * 0.40) * 10.0) / 10.0;
        double remainingVolume = targetVolume - longRunDist;
        double easyRunDist = Math.round((remainingVolume / Math.max(1, sortedDays.size() - 1)) * 10.0) / 10.0;

        for (int i = 0; i < sortedDays.size(); i++) {
            DayOfWeek dayOfWeek = sortedDays.get(i);
            Workout workout = new Workout();
            workout.setTrainingWeek(trainingWeek);

            LocalDate scheduledDate = trainingWeek.getStartDate().with(dayOfWeek);

            if (scheduledDate.isBefore(trainingWeek.getStartDate())){
                scheduledDate = scheduledDate.plusWeeks(1);
            }
            workout.setDate(scheduledDate);

            if (i == sortedDays.size() - 1) {
                workout.setType(Type.LONG_RUN);
                workout.setDistance(longRunDist);
                workout.setDescription(String.format("Дълго бягане за издръжливост: %.1f км", longRunDist));
            } else {
                workout.setType(Type.EASY);
                workout.setDistance(easyRunDist);
                workout.setDescription(String.format("Леко бягане с аеробно темпо: %.1f км", easyRunDist));
            }

            workouts.add(workout);
        }

        return workouts;
        }



    private int determineCurrentWeek(TrainingPlan plan, LocalDate today) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(plan.getStartDate(), today);
        if (daysBetween < 0) return 1;
        return (int) (daysBetween / 7) + 1;
    }

    private double calculateFatigueFactor(RunnerProfile profile) {
        LocalDate startDate = LocalDate.now().minusDays(14);
        List<Run> recentRuns = runRepository.findAllByRunnerProfileAndDateAfter(profile, startDate);

        if (recentRuns.isEmpty()){
            return 1.0;
        }

        double averageRpe = recentRuns.stream()
                .mapToInt(Run::getPerceivedEffort)
                .average().orElse(5.0);

        if (averageRpe >= 8.0){
            return 0.85;
        } else if(averageRpe <= 4.0){
            return 1.05;
        }

        return 1.0;
    }
}
