
package org.example.stride.integration;

import org.example.stride.model.entity.*;
import org.example.stride.model.enums.*;
import org.example.stride.repository.*;

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
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GoalIntegrationTest {

    private static final String TEST_EMAIL = "test@stride.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RunnerProfileRepository runnerProfileRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private GoalAssessmentRepository goalAssessmentRepository;

    @Autowired
    private TrainingPlanRepository trainingPlanRepository;

    @Autowired
    private TrainingWeekRepository trainingWeekRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    private RunnerProfile runnerProfile;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Runner");
        user.setEmail(TEST_EMAIL);
        user.setPassword("test-password");
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        user.setGender(Gender.FEMALE);
        user.setCreatedAt(LocalDate.now());
        user.setRole(Role.USER);

        user = userRepository.save(user);

        runnerProfile = new RunnerProfile();
        runnerProfile.setUser(user);
        runnerProfile.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        runnerProfile.setRunsPerWeek(4);
        runnerProfile.setAverageWeeklyDistance(25);
        runnerProfile.setLongestRecentRun(10);
        runnerProfile.setAveragePaceSeconds(360);
        runnerProfile.setPreferredRunningDays(
                Set.of(
                        Day.MONDAY,
                        Day.WEDNESDAY,
                        Day.FRIDAY,
                        Day.SUNDAY
                )
        );

        runnerProfile = runnerProfileRepository.save(runnerProfile);

        user.setRunnerProfile(runnerProfile);
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createGoal_shouldSaveGoalAndGenerateTrainingPlan() throws Exception {

        String requestBody = """
                {
                    "targetDistance": 5.0,
                    "targetHours": 0,
                    "targetMinutes": 30,
                    "targetSeconds": 0,
                    "targetDate": "%s",
                    "event": "First 5K"
                }
                """.formatted(LocalDate.now().plusYears(3));

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                            "message": "Goal is created successfully."
                        }
                        """));

        List<Goal> goals = goalRepository.findAllByRunnerProfile(runnerProfile);

        assertEquals(1, goals.size());

        Goal goal = goals.get(0);

        assertEquals(5.0, goal.getTargetDistance());
        assertEquals(1800, goal.getTargetTimeSeconds());
        assertEquals(Status.ACTIVE, goal.getStatus());
        assertEquals(LocalDate.now().plusYears(3), goal.getTargetDate());

        List<GoalAssessment> assessments = goalAssessmentRepository.findAll();

        assertTrue(
                assessments.stream()
                        .anyMatch(assessment ->
                                assessment.getGoal().getId()==(goal.getId())
                        )
        );

        TrainingPlan trainingPlan = trainingPlanRepository
                .findByGoal(goal)
                .orElseThrow();

        assertEquals(Status.ACTIVE, trainingPlan.getStatus());
        assertTrue(trainingPlan.getWeeks() > 0);

        List<TrainingWeek> weeks = trainingWeekRepository
                .findAllByTrainingPlanOrderByWeekNumberAsc(trainingPlan);

        assertFalse(weeks.isEmpty());
        assertTrue(weeks.size() <= 2);

        List<Workout> workouts = workoutRepository.findAll().stream()
                .filter(workout ->
                        workout.getTrainingWeek() != null
                                && workout.getTrainingWeek()
                                .getTrainingPlan().getId()
                                ==(trainingPlan.getId())
                )
                .toList();

        assertFalse(workouts.isEmpty());
    }
}
