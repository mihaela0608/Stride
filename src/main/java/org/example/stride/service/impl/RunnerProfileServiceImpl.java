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

        runnerProfile.setUser(user);
        modelMapper.map(runnerProfileDto, runnerProfile);

        runnerProfileRepository.save(runnerProfile);
        return true;
    }
}
