
package org.example.stride.service.impl;

import org.example.stride.model.dto.RunnerProfileDto;
import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.entity.User;
import org.example.stride.model.enums.Day;
import org.example.stride.model.enums.ExperienceLevel;
import org.example.stride.repository.RunnerProfileRepository;
import org.example.stride.service.session.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunnerProfileServiceImplTest {

    @Mock
    private RunnerProfileRepository runnerProfileRepository;

    @Mock
    private CurrentUserService currentUserService;

    private ModelMapper modelMapper;
    private RunnerProfileServiceImpl runnerProfileService;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        runnerProfileService = new RunnerProfileServiceImpl(
                runnerProfileRepository,
                currentUserService,
                modelMapper
        );
    }

    private RunnerProfileDto createDto() {
        return new RunnerProfileDto(
                ExperienceLevel.BEGINNER,
                3,
                20.0,
                8.0,
                "6:00",
                Set.of(Day.MONDAY, Day.FRIDAY, Day.SUNDAY)
        );
    }

    @Test
    void createRunnerProfile_createsProfileAndReturnsTrue_whenCorrectData() {
        RunnerProfileDto dto = createDto();
        User user = new User();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(runnerProfileRepository.findByUser(user))
                .thenReturn(Optional.empty());

        boolean result = runnerProfileService.createRunnerProfile(dto);

        ArgumentCaptor<RunnerProfile> captor =
                ArgumentCaptor.forClass(RunnerProfile.class);

        verify(runnerProfileRepository).save(captor.capture());

        RunnerProfile saved = captor.getValue();

        assertTrue(result);
        assertSame(user, saved.getUser());
        assertEquals(dto.getExperienceLevel(), saved.getExperienceLevel());
        assertEquals(dto.getRunsPerWeek(), saved.getRunsPerWeek());
        assertEquals(dto.getAverageWeeklyDistance(), saved.getAverageWeeklyDistance());
        assertEquals(dto.getLongestRecentRun(), saved.getLongestRecentRun());
        assertEquals(360, saved.getAveragePaceSeconds());
        assertEquals(dto.getPreferredRunningDays(), saved.getPreferredRunningDays());
    }

    @Test
    void createRunnerProfile_returnsFalse_whenRunnerProfileAlreadyCreated() {
        RunnerProfileDto dto = createDto();
        User user = new User();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(runnerProfileRepository.findByUser(user))
                .thenReturn(Optional.of(new RunnerProfile()));

        boolean result = runnerProfileService.createRunnerProfile(dto);

        assertFalse(result);
        verify(runnerProfileRepository, never()).save(any());
    }

    @Test
    void createRunnerProfile_returnsFalse_whenRunningDaysNotEqual() {
        RunnerProfileDto dto = new RunnerProfileDto(
                ExperienceLevel.BEGINNER,
                2,
                20.0,
                8.0,
                "6:00",
                Set.of(Day.MONDAY, Day.FRIDAY, Day.SUNDAY)
        );

        User user = new User();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(runnerProfileRepository.findByUser(user))
                .thenReturn(Optional.empty());

        boolean result = runnerProfileService.createRunnerProfile(dto);

        assertFalse(result);
        verify(runnerProfileRepository, never()).save(any());
    }

    @Test
    void hasProfile_returnsTrue_whenProfileExists() {
        User user = new User();
        user.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(runnerProfileRepository.existsByUserId(1L)).thenReturn(true);

        assertTrue(runnerProfileService.hasProfile(1L));

        verify(runnerProfileRepository).existsByUserId(1L);
    }

    @Test
    void hasProfile_returnsFalse_whenProfileDoesNotExist() {
        User user = new User();
        user.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(runnerProfileRepository.existsByUserId(1L)).thenReturn(false);

        assertFalse(runnerProfileService.hasProfile(1L));

        verify(runnerProfileRepository).existsByUserId(1L);
    }
}
