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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RunIntegrationTest {
    private static final String TEST_EMAIL = "test@stride.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RunnerProfileRepository runnerProfileRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private TrainingWeekRepository trainingWeekRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private TrainingPlanRepository trainingPlanRepository;

    private RunnerProfile runnerProfile;

    private Workout workout;





    @BeforeEach
    void setUp(){
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

        Goal goal = new Goal();
        goal.setRunnerProfile(runnerProfile);
        goal.setTargetDistance(10);
        goal.setTargetTimeSeconds(3600);
        goal.setTargetDate(LocalDate.now().plusWeeks(8));
        goal.setStatus(Status.ACTIVE);
        goal.setCreatedAt(LocalDate.now());
        goal = goalRepository.save(goal);

        TrainingPlan trainingPlan = new TrainingPlan();
        trainingPlan.setGoal(goal);
        trainingPlan.setStartDate(LocalDate.now());
        trainingPlan.setEndDate(LocalDate.now().plusWeeks(8));
        trainingPlan.setWeeks(8);
        trainingPlan.setStatus(Status.ACTIVE);
        trainingPlan = trainingPlanRepository.save(trainingPlan);

        TrainingWeek trainingWeek = new TrainingWeek();
        trainingWeek.setTrainingPlan(trainingPlan);
        trainingWeek.setWeekNumber(1);
        trainingWeek.setTargetWeeklyDistance(20);
        trainingWeek.setStartDate(LocalDate.now());
        trainingWeek.setEndDate(LocalDate.now().plusDays(6));
        trainingWeek.setFocus(Focus.BASE);
        trainingWeek = trainingWeekRepository.save(trainingWeek);

        workout = new Workout();
        workout.setDate(LocalDate.now());
        workout.setType(Type.EASY);
        workout.setDistance(5);
        workout.setDurationSeconds(3600);
        workout.setDescription("Test");
        workout.setStatus(Status.ACTIVE);
        workout.setTrainingWeek(trainingWeek);
        workout = workoutRepository.save(workout);
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createRun_shouldSaveRun() throws Exception {
        String requestBody = """
                {
                "date": "%s",
                "workoutId": %d,
                "distance": 5,
                "duration": 3600,
                "runType": "EASY",
                "perceivedEffort": 6,
                "averageHeartRate": 150,
                "elevationGain": 60,
                "notes": ""
                }
                """.formatted(LocalDate.now(), workout.getId() );

        mockMvc.perform(post("/api/runs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                            "message": "Run is created successfully!"
                        }
                        """));

        List<Run> runs = runRepository.findAll();
        assertEquals(1, runs.size());

        Run run = runs.get(0);
        assertEquals(LocalDate.now(), run.getDate());
        assertEquals(workout.getId(), run.getWorkout().getId());
        assertEquals(5, run.getDistance());
        assertEquals(3600, run.getDuration());
        assertEquals(Type.EASY, run.getRunType());
        assertEquals(6, run.getPerceivedEffort());
        assertEquals(150, run.getAverageHeartRate());
        assertEquals(60, run.getElevationGain());
        assertEquals("", run.getNotes());
    }



}
