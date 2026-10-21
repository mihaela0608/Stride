
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
import java.util.Optional;

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
                    "Вече имаш активна цел. Замени я, преди да създадеш нова."
            );
        }

        createGoalWithAssessment(dto, profile);
    }

    @Override
    @Transactional
    public boolean replaceGoal(GoalCreationDto dto) {
        RunnerProfile profile = getCurrentRunnerProfile();

        Goal activeGoal = (Goal) goalRepository
                .findAllByRunnerProfileAndStatus(profile, Status.ACTIVE)
                .orElseThrow(() -> new IllegalStateException(
                        "Нямаш активна цел, която да замениш."
                ));

        activeGoal.setStatus(Status.REPLACED);

        createGoalWithAssessment(dto, profile);

        return true;
    }

    @Override
    public boolean completeActiveGoal() {
        RunnerProfile profile =
                currentUserService.getCurrentUser().getRunnerProfile();
        Optional<Goal> goal = goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE);

        if (goal.isEmpty()){
            return false;
        }
        goal.get().setStatus(Status.COMPLETED);
        goalRepository.save(goal.get());
        return true;
    }

    @Override
    public List<GoalResponseDto> getAll() {
        RunnerProfile profile =
                currentUserService.getCurrentUser().getRunnerProfile();
        return goalRepository.findAllByRunnerProfile(profile).stream().map(this::toGoalResponseDto).toList();
    }



    private RunnerProfile getCurrentRunnerProfile() {
        RunnerProfile profile =
                currentUserService.getCurrentUser().getRunnerProfile();

        if (profile == null) {
            throw new IllegalStateException(
                    "Преди да създадеш цел, трябва да имаш бегачески профил."
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
                    "Целевото време трябва да бъде положително."
            );
        }

        if (profile.getLongestRecentRun() <= 0
                || profile.getAveragePaceSeconds() <= 0) {
            throw new IllegalStateException(
                    "Необходими са валидни данни за бягане, за да се направи прогноза."
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
        goal.setTargetDistance(dto.getTargetDistance());
        goal.setTargetTimeSeconds(targetSeconds);
        goal.setTargetDate(dto.getTargetDate());
        goal.setEvent(dto.getEvent());
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

    private int parseTargetTime(GoalCreationDto dto) {
        int hours = dto.getTargetHours() != null
                ? dto.getTargetHours() : 0;
        int minutes = dto.getTargetMinutes() != null
                ? dto.getTargetMinutes() : 0;
        int seconds = dto.getTargetSeconds() != null
                ? dto.getTargetSeconds() : 0;

        return hours * 3600 + minutes * 60 + seconds;
    }

    private GoalResponseDto toGoalResponseDto(Goal goal) {
        GoalResponseDto dto = new GoalResponseDto();
        modelMapper.map(goal, dto);
        return dto;
    }
}
