
package org.example.stride.service.impl;

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
import org.example.stride.service.session.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceImplTest {

    @Mock
    private TrainingPlanRepository trainingPlanRepository;

    @Mock
    private AdaptivePlanService adaptivePlanService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TrainingWeekRepository trainingWeekRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @InjectMocks
    private TrainingPlanServiceImpl trainingPlanService;

    private User user;
    private RunnerProfile profile;
    private Goal goal;
    private TrainingPlan plan;
    private GoalAssessment assessment;

    @BeforeEach
    void setUp() {
        profile = new RunnerProfile();

        user = new User();
        user.setRunnerProfile(profile);

        goal = new Goal();
        goal.setRunnerProfile(profile);
        goal.setStatus(Status.ACTIVE);
        goal.setTargetDate(LocalDate.now().plusMonths(6));

        plan = new TrainingPlan();
        plan.setGoal(goal);
        plan.setStatus(Status.ACTIVE);

        assessment = new GoalAssessment();
        assessment.setEstimatedWeeks(8);
    }


    @Test
    void createTrainingPlan_shouldCreatePlanAndGenerateWeeks() {
        trainingPlanService.createTrainingPlan(goal, assessment);

        verify(trainingPlanRepository).save(argThat(savedPlan ->
                savedPlan.getGoal().equals(goal)
                        && savedPlan.getStatus() == Status.ACTIVE
                        && savedPlan.getWeeks() == 8
                        && savedPlan.getStartDate().equals(LocalDate.now())
                        && savedPlan.getEndDate().equals(
                        LocalDate.now().plusWeeks(8))
        ));

        verify(adaptivePlanService)
                .generateOrUpdateNextWindow(any(TrainingPlan.class));
    }

    @Test
    void createTrainingPlan_shouldThrowExceptionWhenGoalDateIsTooSoon() {
        goal.setTargetDate(LocalDate.now().plusWeeks(4));

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.createTrainingPlan(goal, assessment));

        verifyNoInteractions(trainingPlanRepository, adaptivePlanService);
    }


    @Test
    void generateNewWeeks_shouldGenerateWeeksForActivePlan() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.of(plan));

        trainingPlanService.generateNewWeeks();

        verify(adaptivePlanService).generateOrUpdateNextWindow(plan);
    }

    @Test
    void generateNewWeeks_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        user.setRunnerProfile(null);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.generateNewWeeks());

        verifyNoInteractions(goalRepository, trainingPlanRepository,
                adaptivePlanService);
    }

    @Test
    void generateNewWeeks_shouldThrowExceptionWhenNoActiveGoalExists() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.generateNewWeeks());

        verifyNoInteractions(trainingPlanRepository, adaptivePlanService);
    }

    @Test
    void generateNewWeeks_shouldThrowExceptionWhenTrainingPlanDoesNotExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.generateNewWeeks());

        verifyNoInteractions(adaptivePlanService);
    }


    @Test
    void getCurrentTrainingPlan_shouldReturnTrainingPlanDto() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.of(plan));

        TrainingPlanViewDto result =
                trainingPlanService.getCurrentTrainingPlan();

        assertNotNull(result);

        verify(modelMapper).map(plan, result);
    }

    @Test
    void getCurrentTrainingPlan_shouldThrowExceptionWhenNoActiveGoalExists() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.getCurrentTrainingPlan());

        verifyNoInteractions(trainingPlanRepository, modelMapper);
    }

    @Test
    void getCurrentTrainingPlan_shouldThrowExceptionWhenPlanDoesNotExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> trainingPlanService.getCurrentTrainingPlan());
    }

    @Test
    void getCurrentTrainingPlan_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        user.setRunnerProfile(null);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.getCurrentTrainingPlan());

        verifyNoInteractions(goalRepository, trainingPlanRepository,
                modelMapper);
    }


    @Test
    void getAllWorkouts_shouldReturnWorkoutsFromAllWeeks() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        TrainingWeek firstWeek = new TrainingWeek();
        TrainingWeek secondWeek = new TrainingWeek();

        Workout firstWorkout = new Workout();
        Workout secondWorkout = new Workout();
        Workout thirdWorkout = new Workout();

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.of(plan));

        when(trainingWeekRepository
                .findAllByTrainingPlanOrderByWeekNumberAsc(plan))
                .thenReturn(List.of(firstWeek, secondWeek));

        when(workoutRepository.findAllByTrainingWeek(firstWeek))
                .thenReturn(List.of(firstWorkout, secondWorkout));

        when(workoutRepository.findAllByTrainingWeek(secondWeek))
                .thenReturn(List.of(thirdWorkout));

        List<WorkoutsViewDto> result =
                trainingPlanService.getAllWorkouts();

        assertEquals(3, result.size());

        verify(modelMapper, times(3))
                .map(any(Workout.class), any(WorkoutsViewDto.class));
    }

    @Test
    void getAllWorkouts_shouldReturnEmptyListWhenNoWeeksExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.of(plan));

        when(trainingWeekRepository
                .findAllByTrainingPlanOrderByWeekNumberAsc(plan))
                .thenReturn(List.of());

        List<WorkoutsViewDto> result =
                trainingPlanService.getAllWorkouts();

        assertTrue(result.isEmpty());

        verifyNoInteractions(modelMapper);
        verify(workoutRepository, never()).findAllByTrainingWeek(any());
    }

    @Test
    void getAllWorkouts_shouldThrowExceptionWhenNoActiveGoalExists() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.getAllWorkouts());

        verifyNoInteractions(trainingPlanRepository,
                trainingWeekRepository, workoutRepository, modelMapper);
    }

    @Test
    void getAllWorkouts_shouldThrowExceptionWhenPlanDoesNotExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> trainingPlanService.getAllWorkouts());
    }

    @Test
    void getAllWorkouts_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        user.setRunnerProfile(null);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.getAllWorkouts());

        verifyNoInteractions(goalRepository, trainingPlanRepository,
                trainingWeekRepository, workoutRepository);
    }

    // GET ALL TRAINING WEEKS

    @Test
    void getAllTrainingWeeks_shouldReturnAllWeeks() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        TrainingWeek firstWeek = new TrainingWeek();
        TrainingWeek secondWeek = new TrainingWeek();

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.of(plan));

        when(trainingWeekRepository
                .findAllByTrainingPlanOrderByWeekNumberAsc(plan))
                .thenReturn(List.of(firstWeek, secondWeek));

        List<TrainingWeekViewDto> result =
                trainingPlanService.getAllTrainingWeeks();

        assertEquals(2, result.size());

        verify(modelMapper, times(2))
                .map(any(TrainingWeek.class), any(TrainingWeekViewDto.class));
    }

    @Test
    void getAllTrainingWeeks_shouldReturnEmptyListWhenNoWeeksExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.of(plan));

        when(trainingWeekRepository
                .findAllByTrainingPlanOrderByWeekNumberAsc(plan))
                .thenReturn(List.of());

        List<TrainingWeekViewDto> result =
                trainingPlanService.getAllTrainingWeeks();

        assertTrue(result.isEmpty());
        verifyNoInteractions(modelMapper);
    }

    @Test
    void getAllTrainingWeeks_shouldThrowExceptionWhenNoActiveGoalExists() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.getAllTrainingWeeks());

        verifyNoInteractions(trainingPlanRepository,
                trainingWeekRepository, modelMapper);
    }

    @Test
    void getAllTrainingWeeks_shouldThrowExceptionWhenPlanDoesNotExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> trainingPlanService.getAllTrainingWeeks());
    }

    @Test
    void getAllTrainingWeeks_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        user.setRunnerProfile(null);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.getAllTrainingWeeks());

        verifyNoInteractions(goalRepository, trainingPlanRepository,
                trainingWeekRepository, modelMapper);
    }


    @Test
    void getWeeklyWorkouts_shouldReturnWorkoutsForRequestedWeek() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        int weekNumber = 2;

        TrainingWeek week = new TrainingWeek();
        Workout firstWorkout = new Workout();
        Workout secondWorkout = new Workout();

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.of(plan));

        when(trainingWeekRepository.findByTrainingPlanAndWeekNumber(
                plan, weekNumber)).thenReturn(Optional.of(week));

        when(workoutRepository.findAllByTrainingWeek(week))
                .thenReturn(List.of(firstWorkout, secondWorkout));

        List<WorkoutsViewDto> result =
                trainingPlanService.getWeeklyWorkouts(weekNumber);

        assertEquals(2, result.size());

        verify(modelMapper, times(2))
                .map(any(Workout.class), any(WorkoutsViewDto.class));
    }

    @Test
    void getWeeklyWorkouts_shouldReturnEmptyListWhenWeekHasNoWorkouts() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        int weekNumber = 2;
        TrainingWeek week = new TrainingWeek();

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.of(plan));

        when(trainingWeekRepository.findByTrainingPlanAndWeekNumber(
                plan, weekNumber)).thenReturn(Optional.of(week));

        when(workoutRepository.findAllByTrainingWeek(week))
                .thenReturn(List.of());

        List<WorkoutsViewDto> result =
                trainingPlanService.getWeeklyWorkouts(weekNumber);

        assertTrue(result.isEmpty());
        verifyNoInteractions(modelMapper);
    }

    @Test
    void getWeeklyWorkouts_shouldThrowExceptionWhenNoActiveGoalExists() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.getWeeklyWorkouts(1));

        verifyNoInteractions(trainingPlanRepository,
                trainingWeekRepository, workoutRepository, modelMapper);
    }

    @Test
    void getWeeklyWorkouts_shouldThrowExceptionWhenWeekDoesNotExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.of(plan));

        when(trainingWeekRepository.findByTrainingPlanAndWeekNumber(
                plan, 99)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.getWeeklyWorkouts(99));

        verifyNoInteractions(workoutRepository, modelMapper);
    }

    @Test
    void getWeeklyWorkouts_shouldThrowExceptionWhenRunnerProfileIsMissing() {
        user.setRunnerProfile(null);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(IllegalStateException.class,
                () -> trainingPlanService.getWeeklyWorkouts(1));

        verifyNoInteractions(goalRepository, trainingPlanRepository,
                trainingWeekRepository, workoutRepository);
    }

    @Test
    void getWeeklyWorkouts_shouldThrowExceptionWhenPlanDoesNotExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(goalRepository.findByRunnerProfileAndStatus(profile, Status.ACTIVE))
                .thenReturn(Optional.of(goal));

        when(trainingPlanRepository.findByGoal(goal))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> trainingPlanService.getWeeklyWorkouts(1));
    }
}
