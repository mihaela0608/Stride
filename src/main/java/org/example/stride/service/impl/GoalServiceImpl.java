package org.example.stride.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.model.entity.Goal;
import org.example.stride.model.entity.GoalAssessment;
import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.enums.Difficulty;
import org.example.stride.model.enums.Status;
import org.example.stride.repository.GoalAssessmentRepository;
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
    private final GoalAssessmentRepository goalAssessmentRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public void createGoal(GoalCreationDto dto) {
        RunnerProfile runnerProfile =
                currentUserService.getCurrentUser().getRunnerProfile();

        if (runnerProfile == null) {
            throw new IllegalStateException(
                    "Преди да създадеш цел, трябва да имаш бегачески профил."
            );
        }

        Goal goal = new Goal();
        modelMapper.map(dto, goal);

        goal.setRunnerProfile(runnerProfile);
        goal.setCreatedAt(LocalDate.now());
        goal.setStatus(Status.ACTIVE);

        Goal savedGoal = goalRepository.save(goal);

        GoalAssessment assessment = createGoalAssessment(savedGoal);

        goalAssessmentRepository.save(assessment);
    }

    private GoalAssessment createGoalAssessment(Goal goal) {
        RunnerProfile profile = goal.getRunnerProfile();

        double distance = profile.getLongestRecentRun();
        double pace = profile.getAveragePaceSeconds();

        if (distance <= 0 || pace <= 0) {
            throw new IllegalStateException(
                    "Необходими са валидни данни за бягане, за да се направи прогноза."
            );
        }

        double baseTimeSeconds = pace * distance;

        double predictedTime =
                baseTimeSeconds
                        * Math.pow(goal.getTargetDistance() / distance, 1.06);

        GoalAssessment assessment = new GoalAssessment();
        assessment.setGoal(goal);
        assessment.setPredictedTime((int) Math.round(predictedTime));

        if (goal.getTargetTimeSeconds() <= 0) {
            throw new IllegalArgumentException(
                    "Целевото време трябва да бъде положително."
            );
        }

        double percentage =
                ((predictedTime - goal.getTargetTimeSeconds())
                        / predictedTime) * 100;

        assessment.setRequiredImprovementPercentage(
                Math.max(0, percentage)
        );

        assessment.setCreatedAt(LocalDate.now());
        assessment.setDifficulty(determineDifficulty(percentage));

        //TODO: Goal-а не е определен спрямо таргет датата. Да се изчисли спрямо това и трудноста

        return assessment;
    }

    private Difficulty determineDifficulty(double percentage) {
        if (percentage <= 0) {
            return Difficulty.REALISTIC;
        }

        if (percentage <= 3) {
            return Difficulty.CHALLENGING;
        }

        if (percentage <= 8) {
            return Difficulty.VERY_AMBITIOUS;
        }

        return Difficulty.UNREALISTIC;
    }
}
