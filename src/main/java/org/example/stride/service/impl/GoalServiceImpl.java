
package org.example.stride.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.model.dto.GoalResponseDto;
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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GoalAssessmentRepository goalAssessmentRepository;
    private final CurrentUserService currentUserService;
    private final GoalAssessmentCalculator calculator;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public void createGoal(GoalCreationDto dto) {
        RunnerProfile profile = getCurrentRunnerProfile();

        if (goalRepository.existsByRunnerProfileAndStatus(
                profile, Status.ACTIVE)) {
            throw new IllegalStateException(
                    "You already have active goal. Replace it with a new one."
            );
        }

        createGoalWithAssessment(dto, profile);
    }

    @Override
    @Transactional
    public boolean replaceGoal(GoalCreationDto dto) {
        RunnerProfile profile = getCurrentRunnerProfile();

        Goal activeGoal = goalRepository
                .findByRunnerProfileAndStatus(profile, Status.ACTIVE)
                .orElseThrow(() -> new IllegalStateException(
                        "You don't have active goal to replace."
                ));

        activeGoal.setStatus(Status.REPLACED);

        createGoalWithAssessment(dto, profile);

        return true;
    }

    @Override
    @Transactional
    public boolean completeActiveGoal() {
        RunnerProfile profile = getCurrentRunnerProfile();

        return goalRepository
                .findByRunnerProfileAndStatus(profile, Status.ACTIVE)
                .map(goal -> {
                    goal.setStatus(Status.COMPLETED);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponseDto> getAll() {
        RunnerProfile profile = getCurrentRunnerProfile();

        return goalRepository.findAllByRunnerProfile(profile)
                .stream()
                .map(this::toGoalResponseDto)
                .toList();
    }

    private RunnerProfile getCurrentRunnerProfile() {
        RunnerProfile profile =
                currentUserService.getCurrentUser().getRunnerProfile();

        if (profile == null) {
            throw new IllegalStateException(
                    "You need to have runner profile to work with goals."
            );
        }

        return profile;
    }

    private void createGoalWithAssessment(
            GoalCreationDto dto,
            RunnerProfile profile) {

        int targetSeconds = parseTargetTime(dto);

        if (targetSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Target time should be positive."
            );
        }

        if (profile.getLongestRecentRun() <= 0
                || profile.getAveragePaceSeconds() <= 0) {
            throw new IllegalStateException(
                    "Run data should be valid to make a assessment."
            );
        }

        LocalDate today = LocalDate.now();

        AssessmentResult result = calculator.predict(
                profile.getLongestRecentRun(),
                profile.getAveragePaceSeconds(),
                profile.getAverageWeeklyDistance(),
                dto.getTargetDistance(),
                targetSeconds,
                dto.getTargetDate()
        );

        Goal goal = new Goal();
        goal.setRunnerProfile(profile);
        modelMapper.map(dto, goal);
        goal.setCreatedAt(today);
        goal.setStatus(Status.ACTIVE);

        Goal savedGoal = goalRepository.save(goal);

        GoalAssessment assessment = new GoalAssessment();
        assessment.setGoal(savedGoal);
        assessment.setRequiredImprovementPercentage(
                result.getRequiredImprovementPercentage());
        assessment.setEstimatedWeeks(result.getEstimatedWeeks());
        assessment.setProjectedDate(result.getProjectedDate());
        assessment.setDifficulty(result.getDifficulty());
        assessment.setExplanation(result.getExplanation());
        assessment.setCreatedAt(today);

        goalAssessmentRepository.save(assessment);
    }

    private GoalResponseDto toGoalResponseDto(Goal goal) {
        GoalResponseDto dto = new GoalResponseDto();

        modelMapper.map(goal, dto);
        dto.setStatus(
                goal.getStatus() != null
                        ? goal.getStatus()
                        : null
        );

        return dto;
    }

    private int parseTargetTime(GoalCreationDto dto) {
        int hours = dto.getTargetHours() != null
                ? dto.getTargetHours() : 0;
        int minutes = dto.getTargetMinutes() != null
                ? dto.getTargetMinutes() : 0;
        int seconds = dto.getTargetSeconds() != null
                ? dto.getTargetSeconds() : 0;

        return hours * 3600 + minutes * 60 + seconds;
    }
}
