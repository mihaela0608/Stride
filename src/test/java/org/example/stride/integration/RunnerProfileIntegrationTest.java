package org.example.stride.integration;

import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.entity.User;
import org.example.stride.model.enums.Day;
import org.example.stride.model.enums.ExperienceLevel;
import org.example.stride.model.enums.Gender;
import org.example.stride.model.enums.Role;
import org.example.stride.repository.RunnerProfileRepository;
import org.example.stride.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RunnerProfileIntegrationTest {
    private static final String TEST_EMAIL = "test@stride.com";

    private static final String VALID_BODY = """
            {
            "experienceLevel": "BEGINNER",
            "runsPerWeek": 3,
            "averageWeeklyDistance": 18,
            "longestRecentRun": 8,
            "averagePace": "06:30",
            "preferredRunningDays": ["MONDAY","WEDNESDAY","SUNDAY"]
            }
            """;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RunnerProfileRepository runnerProfileRepository;
    @Autowired
    private MockMvc mockMvc;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("Test");
        user.setLastName("Runner");
        user.setEmail(TEST_EMAIL);
        user.setPassword("test-password");
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        user.setGender(Gender.FEMALE);
        user.setCreatedAt(LocalDate.now());
        user.setRole(Role.USER);

        user = userRepository.save(user);
    }

    private void createExistingProfile() {
        RunnerProfile profile = new RunnerProfile();
        profile.setUser(user);
        profile.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        profile.setRunsPerWeek(4);
        profile.setAverageWeeklyDistance(25);
        profile.setLongestRecentRun(10);
        profile.setAveragePaceSeconds(360);
        profile.setPreferredRunningDays(Set.of(Day.MONDAY, Day.FRIDAY));

        profile = runnerProfileRepository.save(profile);

        user.setRunnerProfile(profile);
        userRepository.save(user);
    }


    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createProfile_shouldSaveRunnerProfile() throws Exception {
        String requestBody = """
                {
                "experienceLevel": "BEGINNER",
                "runsPerWeek": 3,
                "averageWeeklyDistance": 18,
                "longestRecentRun": 8,
                "averagePace": "06:30",
                "preferredRunningDays": ["MONDAY","WEDNESDAY","SUNDAY"]
                }
                """;

        mockMvc.perform(post("/api/runner-profile")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)).andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                            "message": "Runner profile is created successfully."
                        }
                        """));

        assertEquals(1, runnerProfileRepository.findAll().size());
        RunnerProfile runnerProfile = runnerProfileRepository.findAll().get(0);

        assertEquals(ExperienceLevel.BEGINNER, runnerProfile.getExperienceLevel());
        assertEquals(3, runnerProfile.getRunsPerWeek());
        assertEquals(18, runnerProfile.getAverageWeeklyDistance());
        assertEquals(390, runnerProfile.getAveragePaceSeconds());
        assertEquals(Set.of(Day.MONDAY, Day.WEDNESDAY, Day.SUNDAY), runnerProfile.getPreferredRunningDays());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void getStatus_shouldReturnFalse_whenUserHasNoProfile() throws Exception {
        mockMvc.perform(get("/api/runner-profile/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasProfile").value(false));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void getStatus_shouldReturnTrue_whenUserHasProfile() throws Exception {
        createExistingProfile();

        mockMvc.perform(get("/api/runner-profile/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasProfile").value(true));
    }


    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createProfile_shouldReturnConflict_whenProfileAlreadyExists() throws Exception {
        createExistingProfile();

        mockMvc.perform(post("/api/runner-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().json("""
                        {
                            "message": "You already have profile."
                        }
                        """));

        assertEquals(1, runnerProfileRepository.findAll().size());
        RunnerProfile profile = runnerProfileRepository.findAll().get(0);
        assertEquals(ExperienceLevel.INTERMEDIATE, profile.getExperienceLevel());
        assertEquals(4, profile.getRunsPerWeek());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createProfile_shouldBeLinkedToCurrentUser() throws Exception {
        mockMvc.perform(post("/api/runner-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        RunnerProfile profile = runnerProfileRepository.findAll().get(0);
        assertEquals(user.getId(), profile.getUser().getId());

        mockMvc.perform(get("/api/runner-profile/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasProfile").value(true));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createProfile_shouldConvertDifferentPaceToSeconds() throws Exception {
        String body = VALID_BODY.replace("06:30", "05:45");

        mockMvc.perform(post("/api/runner-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        RunnerProfile profile = runnerProfileRepository.findAll().get(0);
        assertEquals(345, profile.getAveragePaceSeconds());
    }


    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createProfile_shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/runner-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not a json"))
                .andExpect(status().isBadRequest());

        assertEquals(0, runnerProfileRepository.findAll().size());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createProfile_shouldReturnBadRequest_whenEnumValueIsInvalid() throws Exception {
        String body = VALID_BODY.replace("BEGINNER", "SUPERHUMAN");

        mockMvc.perform(post("/api/runner-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertEquals(0, runnerProfileRepository.findAll().size());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createProfile_shouldReturnBadRequest_whenValidationFails() throws Exception {
        String body = """
                {
                "experienceLevel": "BEGINNER",
                "runsPerWeek": -5,
                "averageWeeklyDistance": -1,
                "longestRecentRun": -1,
                "averagePace": "06:30",
                "preferredRunningDays": []
                }
                """;

        mockMvc.perform(post("/api/runner-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertEquals(0, runnerProfileRepository.findAll().size());
    }


    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createProfile_shouldBeForbidden_withoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/runner-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());

        assertEquals(0, runnerProfileRepository.findAll().size());
    }

    @Test
    void createProfile_shouldNotWork_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/runner-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assertTrue(s == 401 || s == 403 || s == 302,
                            "Expected 401/403/302 but was " + s);
                });

        assertEquals(0, runnerProfileRepository.findAll().size());
    }

    @Test
    void getStatus_shouldNotWork_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/runner-profile/status"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assertTrue(s == 401 || s == 403 || s == 302,
                            "Expected 401/403/302 but was " + s);
                });
    }
}