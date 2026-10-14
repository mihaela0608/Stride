package org.example.stride.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.model.entity.Goal;
import org.example.stride.model.entity.GoalAssessment;
import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.enums.Status;
import org.example.stride.repository.GoalAssessmentRepository;
import org.example.stride.repository.GoalRepository;
import org.example.stride.service.GoalService;
import org.example.stride.service.helpClasses.AssessmentResult;
import org.example.stride.service.helpClasses.GoalAssessmentCalculator;
import org.example.stride.service.session.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GoalAssessmentRepository goalAssessmentRepository;
    private final CurrentUserService currentUserService;
    private final GoalAssessmentCalculator calculator;

    @Override
    @Transactional
    public void createGoal(GoalCreationDto dto) {
        RunnerProfile runnerProfile = currentUserService.getCurrentUser().getRunnerProfile();

        if (runnerProfile == null) {
            throw new IllegalStateException("Преди да създадеш цел, трябва да имаш бегачески профил.");
        }

        int targetSeconds = parseTargetTime(dto);

        if (targetSeconds <= 0) {
            throw new IllegalArgumentException("Целевото време трябва да бъде положително.");
        }

        if (runnerProfile.getLongestRecentRun() <= 0 || runnerProfile.getAveragePaceSeconds() <= 0) {
            throw new IllegalStateException("Необходими са валидни данни за бягане, за да се направи прогноза.");
        }

        LocalDate today = LocalDate.now();

        AssessmentResult result = calculator.predict(
                runnerProfile.getLongestRecentRun(),
                runnerProfile.getAveragePaceSeconds(),
                runnerProfile.getAverageWeeklyDistance(),
                dto.getTargetDistance(),
                targetSeconds,
                dto.getTargetDate()
        );

        Goal goal = new Goal();
        goal.setRunnerProfile(runnerProfile);
        goal.setTargetDistance(dto.getTargetDistance());
        goal.setTargetTimeSeconds(targetSeconds);
        goal.setTargetDate(dto.getTargetDate());
        goal.setEvent(dto.getEvent());
        goal.setStatus(Status.ACTIVE);
        goal.setCreatedAt(today);
        goalRepository.save(goal);

        GoalAssessment assessment = new GoalAssessment();
        assessment.setGoal(goal);
        assessment.setRequiredImprovementPercentage(result.getRequiredImprovementPercentage());
        assessment.setEstimatedWeeks(result.getEstimatedWeeks());
        assessment.setProjectedDate(result.getProjectedDate());
        assessment.setDifficulty(result.getDifficulty());
        assessment.setExplanation(result.getExplanation());
        assessment.setCreatedAt(today);



        goalAssessmentRepository.save(assessment);
    }

    private int parseTargetTime(GoalCreationDto dto) {
        int hours = dto.getTargetHours() != null ? dto.getTargetHours() : 0;
        int minutes = dto.getTargetMinutes() != null ? dto.getTargetMinutes() : 0;
        int seconds = dto.getTargetSeconds() != null ? dto.getTargetSeconds() : 0;

        return (hours * 3600) + (minutes * 60) + seconds;
    }
}