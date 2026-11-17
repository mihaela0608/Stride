
package org.example.stride.service.impl;

import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.model.dto.GoalResponseDto;
import org.example.stride.model.entity.*;
import org.example.stride.model.enums.Status;
import org.example.stride.repository.GoalAssessmentRepository;
import org.example.stride.repository.GoalRepository;
import org.example.stride.service.TrainingPlanService;
import org.example.stride.service.helpClasses.AssessmentResult;
import org.example.stride.service.helpClasses.GoalAssessmentCalculator;
import org.example.stride.service.session.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalAssessmentRepository goalAssessmentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TrainingPlanService trainingPlanService;

    @Mock
    private GoalAssessmentCalculator calculator;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private GoalServiceImpl goalService;

    private User user;
    private RunnerProfile profile;
    private GoalCreationDto dto;
    private AssessmentResult assessmentResult;

    @BeforeEach
    void setUp() {
        profile = new RunnerProfile();
        profile.setLongestRecentRun(10.0);
        profile.setAveragePaceSeconds(360.0);
        profile.setAverageWeeklyDistance(20.0);

        user = new User();
        user.setRunnerProfile(profile);

        dto = new GoalCreationDto();
        dto.setTargetHours(0);
        dto.setTargetMinutes(30);
        dto.setTargetSeconds(0);
        dto.setTargetDistance(5.0);
        dto.setTargetDate(LocalDate.now().plusMonths(2));

        assessmentResult = mock(AssessmentResult.class);

        when(currentUserService.getCurrentUser()).thenReturn(user);
    }


    @Test
    void createGoal_shouldCreateGoalAssessmentAndTrainingPlan() {
        when(goalRepository.existsByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(false);

        when(calculator.predict(
                anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyInt(), any(LocalDate.class)
        )).thenReturn(assessmentResult);

        when(goalRepository.save(any(Goal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        goalService.createGoal(dto);

        ArgumentCaptor<Goal> goalCaptor =
                ArgumentCaptor.forClass(Goal.class);

        verify(goalRepository).save(goalCaptor.capture());

        Goal savedGoal = goalCaptor.getValue();

        assertEquals(profile, savedGoal.getRunnerProfile());
        assertEquals(Status.ACTIVE, savedGoal.getStatus());
        assertEquals(LocalDate.now(), savedGoal.getCreatedAt());
        assertEquals(1800, savedGoal.getTargetTimeSeconds());

        verify(calculator).predict(
                10.0, 360.0, 20.0,
                5.0, 1800, dto.getTargetDate()
        );

        ArgumentCaptor<GoalAssessment> assessmentCaptor =
                ArgumentCaptor.forClass(GoalAssessment.class);

        verify(goalAssessmentRepository).save(assessmentCaptor.capture());

        GoalAssessment savedAssessment = assessmentCaptor.getValue();

        assertEquals(savedGoal, savedAssessment.getGoal());
        assertEquals(LocalDate.now(), savedAssessment.getCreatedAt());

        verify(trainingPlanService).createTrainingPlan(
                eq(savedGoal), same(savedAssessment));
    }

    @Test
    void createGoal_shouldThrowExceptionWhenActiveGoalExists() {
        when(goalRepository.existsByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> goalService.createGoal(dto));

        verify(goalRepository, never()).save(any());
        verify(goalAssessmentRepository, never()).save(any());
        verifyNoInteractions(calculator, trainingPlanService);
    }

    @Test
    void createGoal_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        user.setRunnerProfile(null);

        assertThrows(IllegalStateException.class,
                () -> goalService.createGoal(dto));

        verifyNoInteractions(goalRepository, calculator,
                goalAssessmentRepository, trainingPlanService);
    }

    @Test
    void createGoal_shouldThrowExceptionWhenTargetTimeIsZero() {
        dto.setTargetHours(0);
        dto.setTargetMinutes(0);
        dto.setTargetSeconds(0);

        when(goalRepository.existsByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(dto));

        verify(goalRepository, never()).save(any());
        verifyNoInteractions(calculator, goalAssessmentRepository,
                trainingPlanService);
    }

    @Test
    void createGoal_shouldThrowExceptionWhenTargetTimeIsNegative() {
        dto.setTargetHours(-1);
        dto.setTargetMinutes(0);
        dto.setTargetSeconds(0);

        when(goalRepository.existsByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(dto));

        verify(goalRepository, never()).save(any());
        verifyNoInteractions(calculator, goalAssessmentRepository,
                trainingPlanService);
    }

    @Test
    void createGoal_shouldThrowExceptionWhenLongestRecentRunIsInvalid() {
        profile.setLongestRecentRun(0.0);

        when(goalRepository.existsByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> goalService.createGoal(dto));

        verify(goalRepository, never()).save(any());
        verifyNoInteractions(calculator, goalAssessmentRepository,
                trainingPlanService);
    }

    @Test
    void createGoal_shouldThrowExceptionWhenAveragePaceIsInvalid() {
        profile.setAveragePaceSeconds(0.0);

        when(goalRepository.existsByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> goalService.createGoal(dto));

        verify(goalRepository, never()).save(any());
        verifyNoInteractions(calculator, goalAssessmentRepository,
                trainingPlanService);
    }


    @Test
    void replaceGoal_shouldReplaceOldGoalAndCreateNewOne() {
        Goal oldGoal = new Goal();

        when(goalRepository.findByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(Optional.of(oldGoal));

        when(calculator.predict(
                anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyInt(), any(LocalDate.class)
        )).thenReturn(assessmentResult);

        when(goalRepository.save(any(Goal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = goalService.replaceGoal(dto);

        assertTrue(result);
        assertEquals(Status.REPLACED, oldGoal.getStatus());

        ArgumentCaptor<Goal> goalCaptor =
                ArgumentCaptor.forClass(Goal.class);

        verify(goalRepository).save(goalCaptor.capture());

        Goal newGoal = goalCaptor.getValue();

        assertNotSame(oldGoal, newGoal);
        assertEquals(Status.ACTIVE, newGoal.getStatus());
        assertEquals(profile, newGoal.getRunnerProfile());

        verify(goalAssessmentRepository).save(any(GoalAssessment.class));
        verify(trainingPlanService).createTrainingPlan(
                eq(newGoal), any(GoalAssessment.class));
    }

    @Test
    void replaceGoal_shouldThrowExceptionWhenNoActiveGoalExists() {
        when(goalRepository.findByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> goalService.replaceGoal(dto));

        verify(goalRepository, never()).save(any());
        verifyNoInteractions(calculator, goalAssessmentRepository,
                trainingPlanService);
    }

    @Test
    void replaceGoal_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        user.setRunnerProfile(null);

        assertThrows(IllegalStateException.class,
                () -> goalService.replaceGoal(dto));

        verifyNoInteractions(goalRepository, calculator,
                goalAssessmentRepository, trainingPlanService);
    }


    @Test
    void completeActiveGoal_shouldCompleteGoalAndReturnTrue() {
        Goal activeGoal = new Goal();
        activeGoal.setStatus(Status.ACTIVE);

        when(goalRepository.findByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(Optional.of(activeGoal));

        boolean result = goalService.completeActiveGoal();

        assertTrue(result);
        assertEquals(Status.COMPLETED, activeGoal.getStatus());
    }

    @Test
    void completeActiveGoal_shouldReturnFalseWhenNoActiveGoalExists() {
        when(goalRepository.findByRunnerProfileAndStatus(
                profile, Status.ACTIVE)).thenReturn(Optional.empty());

        boolean result = goalService.completeActiveGoal();

        assertFalse(result);
    }

    @Test
    void completeActiveGoal_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        user.setRunnerProfile(null);

        assertThrows(IllegalStateException.class,
                () -> goalService.completeActiveGoal());

        verifyNoInteractions(goalRepository);
    }


    @Test
    void getAll_shouldReturnAllGoalsForCurrentRunner() {
        Goal firstGoal = new Goal();
        firstGoal.setStatus(Status.ACTIVE);

        Goal secondGoal = new Goal();
        secondGoal.setStatus(Status.COMPLETED);

        when(goalRepository.findAllByRunnerProfile(profile))
                .thenReturn(List.of(firstGoal, secondGoal));

        List<GoalResponseDto> result = goalService.getAll();

        assertEquals(2, result.size());
        assertEquals(Status.ACTIVE, result.get(0).getStatus());
        assertEquals(Status.COMPLETED, result.get(1).getStatus());

        verify(modelMapper, times(2))
                .map(any(Goal.class), any(GoalResponseDto.class));
        verify(goalRepository).findAllByRunnerProfile(profile);
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoGoalsExist() {
        when(goalRepository.findAllByRunnerProfile(profile))
                .thenReturn(List.of());

        List<GoalResponseDto> result = goalService.getAll();

        assertTrue(result.isEmpty());
        verify(modelMapper, never())
                .map(any(), any(GoalResponseDto.class));
    }

    @Test
    void getAll_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        user.setRunnerProfile(null);

        assertThrows(IllegalStateException.class,
                () -> goalService.getAll());

        verifyNoInteractions(goalRepository);
    }
}
