package org.example.stride.integration;

import org.example.stride.model.entity.*;
import org.example.stride.model.enums.*;
import org.example.stride.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TrainingIntegrationTest {
    private static final String TEST_EMAIL = "test@stride.com";
    private static final String OTHER_EMAIL = "other@stride.com";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RunnerProfileRepository runnerProfileRepository;
    @Autowired
    private GoalRepository goalRepository;
    @Autowired
    private TrainingPlanRepository trainingPlanRepository;
    @Autowired
    private TrainingWeekRepository trainingWeekRepository;
    @Autowired
    private WorkoutRepository workoutRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = createUserWithProfile(TEST_EMAIL);
    }


    private User createUserWithProfile(String email) {
        User u = new User();
        u.setFirstName("Test");
        u.setLastName("Runner");
        u.setEmail(email);
        u.setPassword("test-password");
        u.setDateOfBirth(LocalDate.of(2000, 1, 1));
        u.setGender(Gender.FEMALE);
        u.setCreatedAt(LocalDate.now());
        u.setRole(Role.USER);
        u = userRepository.save(u);

        RunnerProfile profile = new RunnerProfile();
        profile.setUser(u);
        profile.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        profile.setRunsPerWeek(4);
        profile.setAverageWeeklyDistance(25);
        profile.setLongestRecentRun(10);
        profile.setAveragePaceSeconds(360);
        profile.setPreferredRunningDays(Set.of(Day.MONDAY, Day.WEDNESDAY, Day.FRIDAY));
        profile = runnerProfileRepository.save(profile);

        u.setRunnerProfile(profile);
        return userRepository.save(u);
    }


    private void createPlanWithWorkouts(User owner) {
        Goal goal = new Goal();
        goal.setRunnerProfile(owner.getRunnerProfile());
        goal.setTargetDistance(10);
        goal.setTargetTimeSeconds(3600);
        goal.setTargetDate(LocalDate.now().plusWeeks(8));
        goal.setStatus(Status.ACTIVE);
        goal.setCreatedAt(LocalDate.now());
        goal = goalRepository.save(goal);

        TrainingPlan plan = new TrainingPlan();
        plan.setGoal(goal);
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusWeeks(8));
        plan.setWeeks(8);
        plan.setStatus(Status.ACTIVE);
        plan = trainingPlanRepository.save(plan);

        TrainingWeek week1 = createWeek(plan, 1, LocalDate.now(), Focus.BASE);
        TrainingWeek week2 = createWeek(plan, 2, LocalDate.now().plusWeeks(1), Focus.BASE);

        createWorkout(week1, LocalDate.now(), 5);
        createWorkout(week1, LocalDate.now().plusDays(2), 7);
        createWorkout(week2, LocalDate.now().plusWeeks(1), 6);
    }

    private TrainingWeek createWeek(TrainingPlan plan, int number, LocalDate start, Focus focus) {
        TrainingWeek week = new TrainingWeek();
        week.setTrainingPlan(plan);
        week.setWeekNumber(number);
        week.setTargetWeeklyDistance(20);
        week.setStartDate(start);
        week.setEndDate(start.plusDays(6));
        week.setFocus(focus);
        return trainingWeekRepository.save(week);
    }

    private void createWorkout(TrainingWeek week, LocalDate date, double distance) {
        Workout w = new Workout();
        w.setDate(date);
        w.setType(Type.EASY);
        w.setDistance(distance);
        w.setDurationSeconds(1800);
        w.setDescription("Test workout");
        w.setStatus(Status.ACTIVE);
        w.setTrainingWeek(week);
        workoutRepository.save(w);
    }


    @Test
    @WithMockUser(username = TEST_EMAIL)
    void viewTrainingPlan_shouldReturnOk_whenUserHasPlan() throws Exception {
        createPlanWithWorkouts(user);

        mockMvc.perform(get("/api/training"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = TEST_EMAIL)
    void viewAllWorkouts_shouldReturnAllWorkoutsOfUser() throws Exception {
        createPlanWithWorkouts(user);

        mockMvc.perform(get("/api/training/workouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].type", containsInAnyOrder("EASY", "EASY", "EASY")))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder("ACTIVE", "ACTIVE", "ACTIVE")))
                .andExpect(jsonPath("$[*].distance", containsInAnyOrder(5.0, 7.0, 6.0)));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void viewAllWorkouts_shouldNotReturnWorkoutsOfOtherUsers() throws Exception {
        createPlanWithWorkouts(user);
        User other = createUserWithProfile(OTHER_EMAIL);
        createPlanWithWorkouts(other);

        mockMvc.perform(get("/api/training/workouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }


    @Test
    @WithMockUser(username = TEST_EMAIL)
    void viewTrainingWeeks_shouldReturnAllWeeksOfPlan() throws Exception {
        createPlanWithWorkouts(user);

        mockMvc.perform(get("/api/training/training-weeks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].weekNumber", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].focus", containsInAnyOrder("BASE", "BASE")));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void viewTrainingWeeks_shouldNotReturnWeeksOfOtherUsers() throws Exception {
        createPlanWithWorkouts(user);
        User other = createUserWithProfile(OTHER_EMAIL);
        createPlanWithWorkouts(other);

        mockMvc.perform(get("/api/training/training-weeks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }


    @Test
    @WithMockUser(username = TEST_EMAIL)
    void viewWeeklyWorkouts_shouldReturnTwoWorkouts_forWeekOne() throws Exception {
        createPlanWithWorkouts(user);

        mockMvc.perform(get("/api/training/week-workouts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].distance", containsInAnyOrder(5.0, 7.0)));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void viewWeeklyWorkouts_shouldReturnOneWorkout_forWeekTwo() throws Exception {
        createPlanWithWorkouts(user);

        mockMvc.perform(get("/api/training/week-workouts/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].distance").value(6.0));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void viewWeeklyWorkouts_shouldReturnBadRequest_whenNumberIsNotInteger() throws Exception {
        mockMvc.perform(get("/api/training/week-workouts/abc"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void trainingEndpoints_shouldNotWork_whenUserIsNotAuthenticated() throws Exception {
        String[] urls = {
                "/api/training",
                "/api/training/workouts",
                "/api/training/training-weeks",
                "/api/training/week-workouts/1"
        };

        for (String url : urls) {
            mockMvc.perform(get(url))
                    .andExpect(result -> {
                        int s = result.getResponse().getStatus();
                        assertTrue(s == 401 || s == 403 || s == 302,
                                url + ": expected 401/403/302 but was " + s);
                    });
        }
    }
}