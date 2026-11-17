
package org.example.stride.service.impl;

import org.example.stride.model.entity.*;
import org.example.stride.model.enums.Day;
import org.example.stride.model.enums.ExperienceLevel;
import org.example.stride.model.enums.Focus;
import org.example.stride.model.enums.Status;
import org.example.stride.repository.RunRepository;
import org.example.stride.repository.TrainingWeekRepository;
import org.example.stride.repository.WorkoutRepository;
import org.example.stride.service.session.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdaptivePlanServiceImplTest {

    @Mock
    private TrainingWeekRepository trainingWeekRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private RunRepository runRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AdaptivePlanServiceImpl adaptivePlanService;

    private User user;
    private RunnerProfile profile;
    private Goal goal;
    private TrainingPlan plan;

    @BeforeEach
    void setUp() {
        profile = new RunnerProfile();
        profile.setAverageWeeklyDistance(20.0);
        profile.setAveragePaceSeconds(360.0);
        profile.setExperienceLevel(ExperienceLevel.BEGINNER);
        profile.setPreferredRunningDays(Set.of(
                Day.MONDAY,
                Day.TUESDAY,
                Day.WEDNESDAY,
                Day.THURSDAY,
                Day.FRIDAY,
                Day.SATURDAY,
                Day.SUNDAY
        ));

        user = new User();
        user.setRunnerProfile(profile);

        goal = new Goal();
        goal.setRunnerProfile(profile);
        goal.setTargetDistance(10.0);
        goal.setTargetDate(LocalDate.now().plusMonths(6));

        plan = new TrainingPlan();
        plan.setGoal(goal);
        plan.setWeeks(8);
        plan.setStartDate(LocalDate.now());
        plan.setStatus(Status.ACTIVE);
    }


    @Test
    void generateOrUpdateNextWindow_shouldGenerateTwoWeeks() {
        stubGeneration(List.of());

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        verify(trainingWeekRepository, times(2))
                .save(any(TrainingWeek.class));

        verify(workoutRepository, times(2))
                .deleteAllByTrainingWeekAndStatusAndDateGreaterThanEqual(
                        any(TrainingWeek.class),
                        eq(Status.ACTIVE),
                        any(LocalDate.class)
                );

        verify(workoutRepository, times(2)).saveAll(anyList());
    }

    @Test
    void generateOrUpdateNextWindow_shouldGenerateOnlyRemainingWeeks() {
        plan.setWeeks(1);
        stubGeneration(List.of());

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        verify(trainingWeekRepository, times(1))
                .save(any(TrainingWeek.class));

        verify(workoutRepository, times(1)).saveAll(anyList());
    }

    @Test
    void generateOrUpdateNextWindow_shouldSetCorrectWeekNumbers() {
        stubGeneration(List.of());

        ArgumentCaptor<TrainingWeek> captor =
                ArgumentCaptor.forClass(TrainingWeek.class);

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        verify(trainingWeekRepository, times(2)).save(captor.capture());

        List<TrainingWeek> savedWeeks = captor.getAllValues();

        assertEquals(1, savedWeeks.get(0).getWeekNumber());
        assertEquals(2, savedWeeks.get(1).getWeekNumber());
    }

    @Test
    void generateOrUpdateNextWindow_shouldSetWeekDates() {
        stubGeneration(List.of());

        ArgumentCaptor<TrainingWeek> captor =
                ArgumentCaptor.forClass(TrainingWeek.class);

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        verify(trainingWeekRepository, times(2)).save(captor.capture());

        TrainingWeek firstWeek = captor.getAllValues().get(0);
        TrainingWeek secondWeek = captor.getAllValues().get(1);

        LocalDate expectedMonday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        assertEquals(expectedMonday, firstWeek.getStartDate());
        assertEquals(expectedMonday.plusDays(6), firstWeek.getEndDate());

        assertEquals(expectedMonday.plusWeeks(1), secondWeek.getStartDate());
        assertEquals(expectedMonday.plusWeeks(1).plusDays(6),
                secondWeek.getEndDate());
    }

    @Test
    void generateOrUpdateNextWindow_shouldSetTrainingPlanForWeeks() {
        stubGeneration(List.of());

        ArgumentCaptor<TrainingWeek> captor =
                ArgumentCaptor.forClass(TrainingWeek.class);

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        verify(trainingWeekRepository, times(2)).save(captor.capture());

        for (TrainingWeek week : captor.getAllValues()) {
            assertEquals(plan, week.getTrainingPlan());
        }
    }

    @Test
    void generateOrUpdateNextWindow_shouldSetBaseFocusForFirstWeeks() {
        stubGeneration(List.of());

        ArgumentCaptor<TrainingWeek> captor =
                ArgumentCaptor.forClass(TrainingWeek.class);

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        verify(trainingWeekRepository, times(2)).save(captor.capture());

        assertEquals(Focus.BASE, captor.getAllValues().get(0).getFocus());
        assertEquals(Focus.BASE, captor.getAllValues().get(1).getFocus());
    }

    @Test
    void generateOrUpdateNextWindow_shouldGenerateWorkoutsForPreferredDays() {
        stubGeneration(List.of());

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        ArgumentCaptor<List<Workout>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(workoutRepository, times(2)).saveAll(captor.capture());

        List<Workout> workouts = captor.getAllValues().get(0);

        assertFalse(workouts.isEmpty());

        for (Workout workout : workouts) {
            assertNotNull(workout.getDate());
            assertNotNull(workout.getType());
            assertTrue(workout.getDistance() > 0);
            assertTrue(workout.getDurationSeconds() > 0);
            assertEquals(Status.ACTIVE, workout.getStatus());
        }
    }

    @Test
    void generateOrUpdateNextWindow_shouldNotGeneratePastWorkouts() {
        stubGeneration(List.of());

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        ArgumentCaptor<List<Workout>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(workoutRepository, times(2)).saveAll(captor.capture());

        LocalDate today = LocalDate.now();

        for (List<Workout> workouts : captor.getAllValues()) {
            for (Workout workout : workouts) {
                assertFalse(workout.getDate().isBefore(today));
            }
        }
    }


    @Test
    void generateOrUpdateNextWindow_shouldUseNormalVolumeWhenNotEnoughRunsExist() {
        stubGeneration(List.of(
                new Run(),
                new Run()
        ));

        ArgumentCaptor<TrainingWeek> captor =
                ArgumentCaptor.forClass(TrainingWeek.class);

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        verify(trainingWeekRepository, times(2)).save(captor.capture());

        assertEquals(
                20.0,
                captor.getAllValues().get(0).getTargetWeeklyDistance(),
                0.001
        );
    }

    @Test
    void generateOrUpdateNextWindow_shouldReduceVolumeWhenFatigueIsHigh() {
        Run firstRun = new Run();
        firstRun.setPerceivedEffort(9);

        Run secondRun = new Run();
        secondRun.setPerceivedEffort(9);

        Run thirdRun = new Run();
        thirdRun.setPerceivedEffort(9);

        stubGeneration(List.of(firstRun, secondRun, thirdRun));

        ArgumentCaptor<TrainingWeek> captor =
                ArgumentCaptor.forClass(TrainingWeek.class);

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        verify(trainingWeekRepository, times(2)).save(captor.capture());

        assertEquals(
                17.0,
                captor.getAllValues().get(0).getTargetWeeklyDistance(),
                0.001
        );
    }

    @Test
    void generateOrUpdateNextWindow_shouldReduceVolumeWhenEffortIsElevated() {
        Run firstRun = new Run();
        firstRun.setPerceivedEffort(7);

        Run secondRun = new Run();
        secondRun.setPerceivedEffort(7);

        Run thirdRun = new Run();
        thirdRun.setPerceivedEffort(7);

        stubGeneration(List.of(firstRun, secondRun, thirdRun));

        ArgumentCaptor<TrainingWeek> captor =
                ArgumentCaptor.forClass(TrainingWeek.class);

        adaptivePlanService.generateOrUpdateNextWindow(plan);

        verify(trainingWeekRepository, times(2)).save(captor.capture());

        assertEquals(
                18.4,
                captor.getAllValues().get(0).getTargetWeeklyDistance(),
                0.001
        );
    }


    @Test
    void generateOrUpdateNextWindow_shouldThrowExceptionWhenPlanIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adaptivePlanService.generateOrUpdateNextWindow(null)
        );

        verifyNoInteractions(
                trainingWeekRepository,
                workoutRepository,
                runRepository
        );
    }

    @Test
    void generateOrUpdateNextWindow_shouldThrowExceptionWhenGoalIsNull() {
        plan.setGoal(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> adaptivePlanService.generateOrUpdateNextWindow(plan)
        );

        verifyNoInteractions(
                trainingWeekRepository,
                workoutRepository,
                runRepository
        );
    }

    @Test
    void generateOrUpdateNextWindow_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        stubCurrentUser();
        user.setRunnerProfile(null);

        assertThrows(
                IllegalStateException.class,
                () -> adaptivePlanService.generateOrUpdateNextWindow(plan)
        );

        verifyNoInteractions(
                trainingWeekRepository,
                workoutRepository,
                runRepository
        );
    }

    @Test
    void generateOrUpdateNextWindow_shouldThrowExceptionWhenPlanHasNoWeeks() {
        stubCurrentUser();
        plan.setWeeks(0);

        assertThrows(
                IllegalStateException.class,
                () -> adaptivePlanService.generateOrUpdateNextWindow(plan)
        );

        verifyNoInteractions(
                trainingWeekRepository,
                workoutRepository,
                runRepository
        );
    }

    @Test
    void generateOrUpdateNextWindow_shouldThrowExceptionWhenStartDateIsMissing() {
        stubCurrentUser();
        plan.setStartDate(null);

        assertThrows(
                IllegalStateException.class,
                () -> adaptivePlanService.generateOrUpdateNextWindow(plan)
        );

        verifyNoInteractions(
                trainingWeekRepository,
                workoutRepository,
                runRepository
        );
    }

    @Test
    void generateOrUpdateNextWindow_shouldThrowExceptionWhenAverageWeeklyDistanceIsZero() {
        stubCurrentUser();
        profile.setAverageWeeklyDistance(0);

        assertThrows(
                IllegalStateException.class,
                () -> adaptivePlanService.generateOrUpdateNextWindow(plan)
        );

        verifyNoInteractions(
                trainingWeekRepository,
                workoutRepository,
                runRepository
        );
    }

    @Test
    void generateOrUpdateNextWindow_shouldThrowExceptionWhenAveragePaceIsZero() {
        stubCurrentUser();
        profile.setAveragePaceSeconds(0);

        assertThrows(
                IllegalStateException.class,
                () -> adaptivePlanService.generateOrUpdateNextWindow(plan)
        );

        verifyNoInteractions(
                trainingWeekRepository,
                workoutRepository,
                runRepository
        );
    }

    @Test
    void generateOrUpdateNextWindow_shouldThrowExceptionWhenPreferredDaysAreMissing() {
        stubCurrentUser();
        profile.setPreferredRunningDays(Set.of());

        assertThrows(
                IllegalStateException.class,
                () -> adaptivePlanService.generateOrUpdateNextWindow(plan)
        );

        verifyNoInteractions(
                trainingWeekRepository,
                workoutRepository,
                runRepository
        );
    }


    private void stubCurrentUser() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    private void stubGeneration(List<Run> runs) {
        stubCurrentUser();

        when(runRepository.findAllByRunnerProfileAndDateAfter(
                eq(profile),
                any(LocalDate.class)
        )).thenReturn(runs);

        when(trainingWeekRepository.findByTrainingPlanAndWeekNumber(
                eq(plan),
                anyInt()
        )).thenReturn(Optional.empty());

        when(trainingWeekRepository.save(any(TrainingWeek.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
