package org.example.stride.service.impl;

import lombok.AllArgsConstructor;
import org.example.stride.model.dto.RunnerProfileDto;
import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.entity.User;
import org.example.stride.repository.RunnerProfileRepository;
import org.example.stride.service.RunnerProfileService;
import org.example.stride.service.session.CurrentUserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class RunnerProfileServiceImpl implements RunnerProfileService {

    private final RunnerProfileRepository runnerProfileRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;

    @Override
    public boolean createRunnerProfile(RunnerProfileDto runnerProfileDto) {

        User user = currentUserService.getCurrentUser();

        if (runnerProfileRepository.findByUser(user).isPresent()) {
            return false;
        }

        RunnerProfile runnerProfile = new RunnerProfile();

        if (runnerProfileDto.getPreferredRunningDays().size() != runnerProfileDto.getRunsPerWeek()){
            return false;
        }

        runnerProfile.setUser(user);
        runnerProfile.setAveragePaceSeconds(parsePaceToSeconds(runnerProfileDto.getAveragePace()));
        modelMapper.map(runnerProfileDto, runnerProfile);

        runnerProfileRepository.save(runnerProfile);
        return true;
    }

    @Override
    public boolean hasProfile(Long userId) {
        return runnerProfileRepository.existsByUserId(currentUserService.getCurrentUser().getId());
    }

    private static int parsePaceToSeconds(String pace){
        String[] parts = pace.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return (minutes * 60) + seconds;
    }
}
