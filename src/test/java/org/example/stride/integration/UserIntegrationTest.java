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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
public class UserIntegrationTest {
    private static final String EXISTING_EMAIL = "existing@stride.com";
    private static final String NEW_EMAIL = "new.runner@stride.com";
    private static final String RAW_PASSWORD = "Secret123!";

    // ВАЖНО: нагласи полетата според UserRegisterDto
    private static final String VALID_REGISTER_BODY = """
            {
            "firstName": "New",
            "lastName": "Runner",
            "email": "new.runner@stride.com",
            "password": "Secret123!",
            "dateOfBirth": "2000-01-01",
            "gender": "FEMALE"
            }
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RunnerProfileRepository runnerProfileRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setFirstName("Existing");
        existingUser.setLastName("Runner");
        existingUser.setEmail(EXISTING_EMAIL);
        existingUser.setPassword("encoded-password");
        existingUser.setDateOfBirth(LocalDate.of(1999, 5, 5));
        existingUser.setGender(Gender.FEMALE);
        existingUser.setCreatedAt(LocalDate.now());
        existingUser.setRole(Role.USER);
        existingUser = userRepository.save(existingUser);
    }

    private List<User> findUsersByEmail(String email) {
        return userRepository.findAll().stream()
                .filter(u -> email.equals(u.getEmail()))
                .toList();
    }


    @Test
    void register_shouldCreateUser_whenDataIsValid() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                            "message": "Registration is done."
                        }
                        """));

        List<User> users = findUsersByEmail(NEW_EMAIL);
        assertEquals(1, users.size());

        User saved = users.get(0);
        assertEquals("New", saved.getFirstName());
        assertEquals("Runner", saved.getLastName());
        assertEquals(LocalDate.of(2000, 1, 1), saved.getDateOfBirth());
        assertEquals(Gender.FEMALE, saved.getGender());
        assertEquals(Role.USER, saved.getRole());
    }

    @Test
    void register_shouldEncodePassword() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER_BODY))
                .andExpect(status().isCreated());

        User saved = findUsersByEmail(NEW_EMAIL).get(0);
        assertNotEquals(RAW_PASSWORD, saved.getPassword());
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, saved.getPassword()));
    }

    @Test
    void register_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        String body = VALID_REGISTER_BODY.replace(NEW_EMAIL, EXISTING_EMAIL);

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().json("""
                        {
                            "message": "User with this email already exists."
                        }
                        """));

        List<User> users = findUsersByEmail(EXISTING_EMAIL);
        assertEquals(1, users.size());
        assertEquals("Existing", users.get(0).getFirstName());
    }

    @Test
    void register_shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not a json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturnBadRequest_whenGenderIsInvalid() throws Exception {
        String body = VALID_REGISTER_BODY.replace("FEMALE", "ROBOT");

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertEquals(0, findUsersByEmail(NEW_EMAIL).size());
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        String body = VALID_REGISTER_BODY.replace(NEW_EMAIL, "not-an-email");

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertEquals(0, findUsersByEmail("not-an-email").size());
    }

    @Test
    void register_shouldReturnBadRequest_whenRequiredFieldsAreBlank() throws Exception {
        String body = """
                {
                "firstName": "",
                "lastName": "",
                "email": "",
                "password": "",
                "dateOfBirth": "2000-01-01",
                "gender": "FEMALE"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldBeForbidden_withoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER_BODY))
                .andExpect(status().isForbidden());

        assertEquals(0, findUsersByEmail(NEW_EMAIL).size());
    }


    @Test
    @WithMockUser(username = EXISTING_EMAIL)
    void getCurrentUser_shouldReturnUserData_withoutRunnerProfile() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EXISTING_EMAIL))
                .andExpect(jsonPath("$.firstName").value("Existing"))
                .andExpect(jsonPath("$.lastName").value("Runner"))
                .andExpect(jsonPath("$.hasRunnerProfile").value(false));
    }

    @Test
    @WithMockUser(username = EXISTING_EMAIL)
    void getCurrentUser_shouldReturnHasRunnerProfileTrue_whenProfileExists() throws Exception {
        RunnerProfile profile = new RunnerProfile();
        profile.setUser(existingUser);
        profile.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        profile.setRunsPerWeek(4);
        profile.setAverageWeeklyDistance(25);
        profile.setLongestRecentRun(10);
        profile.setAveragePaceSeconds(360);
        profile.setPreferredRunningDays(Set.of(Day.MONDAY, Day.FRIDAY));
        profile = runnerProfileRepository.save(profile);

        existingUser.setRunnerProfile(profile);
        userRepository.save(existingUser);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasRunnerProfile").value(true));
    }

    @Test
    @WithMockUser(username = EXISTING_EMAIL)
    void getCurrentUser_shouldNotExposePassword() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getCurrentUser_shouldNotWork_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assertTrue(s == 401 || s == 403 || s == 302,
                            "Expected 401/403/302 but was " + s);
                });
    }
}