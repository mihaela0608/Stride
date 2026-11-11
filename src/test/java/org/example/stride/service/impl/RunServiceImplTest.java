package org.example.stride.service.impl;

import org.example.stride.model.dto.RunCreationDto;
import org.example.stride.model.entity.*;
import org.example.stride.model.enums.Type;
import org.example.stride.repository.RunRepository;
import org.example.stride.repository.WorkoutRepository;
import org.example.stride.service.session.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RunServiceImplTest {

    @Mock
    private RunRepository runRepository;
    private ModelMapper modelMapper = new ModelMapper();
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private WorkoutRepository workoutRepository;
    @InjectMocks
    private RunServiceImpl runService;


    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();

        runService = new RunServiceImpl(
                runRepository,
                modelMapper,
                currentUserService,
                workoutRepository
        );
    }

    @Test
    void addRun_shouldSaveRun_whenValidDataProvided(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        RunCreationDto runCreationDto = new RunCreationDto(LocalDate.now(), null, 5.0, 3600, Type.EASY, 6, 150, 40, null);

        runService.addRun(runCreationDto);

        ArgumentCaptor<Run> captor = ArgumentCaptor.forClass(Run.class);
        verify(runRepository).save(captor.capture());

        Run savedRun = captor.getValue();

        assertEquals(5.0, savedRun.getDistance());
        assertEquals(Type.EASY, savedRun.getRunType());
        assertEquals(LocalDate.now(), savedRun.getDate());
        assertEquals(6, savedRun.getPerceivedEffort());
        assertEquals(40, savedRun.getElevationGain());
        assertEquals(720, savedRun.getAveragePace());
        assertSame(user.getRunnerProfile(), savedRun.getRunnerProfile());

    }

    @Test
    void addRun_throws_whenNoRunnerProfile(){
        User user = new User();

        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(IllegalStateException.class,() ->  runService.addRun(new RunCreationDto()));

    }

    @Test
    void addRun_throws_whenWorkoutNotValid(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        RunCreationDto runCreationDto = new RunCreationDto(LocalDate.now(), 1L, 5.0, 3600, Type.EASY, 6, 150, 40, null);
        assertThrows(IllegalArgumentException.class, () -> runService.addRun(runCreationDto));
    }

    @Test
    void addRun_throws_whenRunnerProfileDoesNotMatch(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        Workout workout = new Workout();
        Goal goal = new Goal();
        RunnerProfile runnerProfile = new RunnerProfile();
        runnerProfile.setId(2L);
        goal.setRunnerProfile(runnerProfile);
        TrainingPlan trainingPlan = new TrainingPlan();
        trainingPlan.setGoal(goal);
        TrainingWeek trainingWeek = new TrainingWeek();
        trainingWeek.setTrainingPlan(trainingPlan);
        workout.setTrainingWeek(trainingWeek);
        workout.setId(1L);
        Mockito.when(workoutRepository.findById(1L)).thenReturn(Optional.of(workout));

        RunCreationDto runCreationDto = new RunCreationDto(LocalDate.now(), 1L, 5.0, 3600, Type.EASY, 6, 150, 40, null);

        assertThrows(IllegalArgumentException.class,() ->  runService.addRun(runCreationDto));
    }

    private User createUserWithRunnerProfile(){
        User user = new User();
        RunnerProfile runnerProfile = new RunnerProfile();
        runnerProfile.setId(1L);

        runnerProfile.setUser(user);
        user.setRunnerProfile(runnerProfile);

        return user;
    }
}
