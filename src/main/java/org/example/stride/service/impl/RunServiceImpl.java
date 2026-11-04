package org.example.stride.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.RunCreationDto;
import org.example.stride.model.dto.RunResponseDto;
import org.example.stride.model.dto.RunUpdateDto;
import org.example.stride.model.entity.Run;
import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.entity.Workout;
import org.example.stride.repository.RunRepository;
import org.example.stride.repository.WorkoutRepository;
import org.example.stride.service.RunService;
import org.example.stride.service.session.CurrentUserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RunServiceImpl implements RunService {

    private final RunRepository runRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserService currentUserService;
    private final WorkoutRepository workoutRepository;

    @Override
    @Transactional
    public void addRun(RunCreationDto dto) {
        RunnerProfile runnerProfile = getRunnerProfile();

        Run run = new Run();
        modelMapper.map(dto, run);
        run.setRunnerProfile(runnerProfile);
        run.setAveragePace(dto.getDuration() / dto.getDistance());
        if (dto.getWorkoutId() != null){
            Optional<Workout> workout = workoutRepository.findById(dto.getWorkoutId());
            if (workout.isEmpty()){
                throw new IllegalArgumentException("No such workout!");
            }

            if (workout.get().getTrainingWeek().getTrainingPlan().getGoal().getRunnerProfile().getId() != runnerProfile.getId()){
                throw new IllegalArgumentException("Workout id is not valid!");
            }
            run.setWorkout(workout.get());
            //TODO: Logic for complateRatio or planAdjusment
        }
        runRepository.save(run);
    }

    @Override
    public List<RunResponseDto> getAll() {
        List<RunResponseDto> dtos = new ArrayList<>();
        dtos = runRepository.findAllByRunnerProfile(getRunnerProfile()).stream().map(e -> {
            RunResponseDto dto = new RunResponseDto();
            modelMapper.map(e, dto);
            return dto;
        }).toList();
        return dtos;
    }

    @Override
    public void updateRun(Long id, RunUpdateDto dto) {
        RunnerProfile runnerProfile = getRunnerProfile();
        Optional<Run> optionalRun = runRepository.findByIdAndRunnerProfile(id, runnerProfile);
        if (optionalRun.isEmpty()){
            throw new IllegalArgumentException("You can't change other users runs");
        }
        Run run = optionalRun.get();
        modelMapper.map(dto, run);

        if (dto.getWorkoutId() != null) {
            Optional<Workout> workout = workoutRepository.findById(dto.getWorkoutId());
            if (workout.isEmpty()) {
                throw new IllegalArgumentException("No such workout!");
            }

            if (workout.get().getTrainingWeek().getTrainingPlan().getGoal().getRunnerProfile().getId() != runnerProfile.getId()) {
                throw new IllegalArgumentException("Workout id is not valid!");
            }
            run.setWorkout(workout.get());
        }

        runRepository.save(run);
    }


    private RunnerProfile getRunnerProfile() {
        RunnerProfile profile =
                currentUserService.getCurrentUser().getRunnerProfile();

        if (profile == null) {
            throw new IllegalStateException(
                    "You need to have runner profile to work with runs."
            );
        }

        return profile;
    }


}
