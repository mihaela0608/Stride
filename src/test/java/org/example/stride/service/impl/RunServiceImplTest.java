package org.example.stride.service.impl;

import org.example.stride.model.dto.RunCreationDto;
import org.example.stride.model.dto.RunResponseDto;
import org.example.stride.model.dto.RunUpdateDto;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RunServiceImplTest {

    @Mock
    private RunRepository runRepository;
    private ModelMapper modelMapper;
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

    @Test
    void getAll_returnsList_whenRunnerProfileAvailable(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);
        Workout workout = new Workout();
        Run run = new Run(user.getRunnerProfile(), LocalDate.now(), workout, 5.0, 3600, 720, 140, 40, Type.EASY, 6, "");

        Mockito.when(runRepository.findAllByRunnerProfile(user.getRunnerProfile())).thenReturn(List.of(run));

        RunResponseDto dto = runService.getAll().get(0);
        assertEquals(run.getId(), dto.getId());
        assertEquals(run.getDate(), dto.getDate());
        assertEquals(run.getDistance(), dto.getDistance());
        assertEquals(run.getDuration(), dto.getDuration());
        assertEquals(run.getAverageHeartRate(), dto.getAverageHeartRate());
        assertEquals(run.getElevationGain(), dto.getElevationGain());
        assertEquals(run.getPerceivedEffort(), dto.getPerceivedEffort());
    }

    @Test
    void getAll_throws_whenNoRunnerProfile(){
        User user = new User();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(IllegalStateException.class, () -> runService.getAll());
    }

    @Test
    void updateRun_updatesAndSaves_correctData(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);
        Run run = new Run(user.getRunnerProfile(), LocalDate.now(), null, 5.0, 3600, 720, 140, 40, Type.EASY, 6, "");
        run.setId(1L);

        Mockito.when(runRepository.findByIdAndRunnerProfile(1L, user.getRunnerProfile())).thenReturn(Optional.of(run));
        RunUpdateDto runUpdateDto = new RunUpdateDto(LocalDate.now(), null, 6.0, 4200, Type.EASY, 7, 140, 70, "");

        runService.updateRun(1L, runUpdateDto);

        ArgumentCaptor<Run> capture = ArgumentCaptor.forClass(Run.class);
        verify(runRepository).save(capture.capture());

        Run savedRun = capture.getValue();

        assertEquals(runUpdateDto.getDate(), savedRun.getDate());
        assertEquals(runUpdateDto.getDistance(), savedRun.getDistance());
        assertEquals(runUpdateDto.getDuration(), savedRun.getDuration());
        assertEquals(runUpdateDto.getRunType(), savedRun.getRunType());
        assertEquals(runUpdateDto.getPerceivedEffort(), savedRun.getPerceivedEffort());
        assertEquals(runUpdateDto.getAverageHeartRate(), savedRun.getAverageHeartRate());
        assertEquals(runUpdateDto.getNotes(), savedRun.getNotes());
    }

    @Test
    void updateRun_throws_whenNoRunnerProfile(){
        User user = new User();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(IllegalStateException.class, () -> runService.updateRun(1L, new RunUpdateDto()));
    }

    @Test
    void updateRun_throws_whenIdNotForCurrentUser(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        Mockito.when(runRepository.findByIdAndRunnerProfile(1L, user.getRunnerProfile())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> runService.updateRun(1L, new RunUpdateDto()));
    }

    @Test
    void updateRun_throws_whenNoSuchWorkout(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);
        Mockito.when(runRepository.findByIdAndRunnerProfile(1L, user.getRunnerProfile())).thenReturn(Optional.of(new Run()));

        RunUpdateDto runUpdateDto = new RunUpdateDto(LocalDate.now(), 1L, 6.0, 4200, Type.EASY, 7, 140, 70, "");
        Mockito.when(workoutRepository.findById(runUpdateDto.getWorkoutId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> runService.updateRun(1L, runUpdateDto));
    }

    @Test
    void updateRun_throws_whenRunNotToWorkout(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);
        Mockito.when(runRepository.findByIdAndRunnerProfile(1L, user.getRunnerProfile())).thenReturn(Optional.of(new Run()));

        RunUpdateDto runUpdateDto = new RunUpdateDto(LocalDate.now(), 1L, 6.0, 4200, Type.EASY, 7, 140, 70, "");
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

        Mockito.when(workoutRepository.findById(runUpdateDto.getWorkoutId())).thenReturn(Optional.of(workout));

        assertThrows(IllegalArgumentException.class, () -> runService.updateRun(1L, runUpdateDto));
    }

    @Test
    void deleteRun_deletes_whenCorrectData(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        Run run = new Run(user.getRunnerProfile(), LocalDate.now(), new Workout(), 5.0, 3600, 720, 140, 40, Type.EASY, 6, "");
        Mockito.when(runRepository.findByIdAndRunnerProfile(1L, user.getRunnerProfile())).thenReturn(Optional.of(run));

        runService.deleteRun(1L);
        ArgumentCaptor<Run> captor = ArgumentCaptor.forClass(Run.class);
        verify(runRepository).delete(captor.capture());

        Run deleted = captor.getValue();

        assertSame(run, deleted);
    }

    @Test
    void deleteRun_throws_whenNoRunnerProfile(){
        User user = new User();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(IllegalStateException.class, () -> runService.deleteRun(1L));
    }

    @Test
    void deleteRun_throws_whenRunNotToCurrentUser(){
        User user = createUserWithRunnerProfile();
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

        Mockito.when(runRepository.findByIdAndRunnerProfile(1L, user.getRunnerProfile())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> runService.deleteRun(1L));
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
