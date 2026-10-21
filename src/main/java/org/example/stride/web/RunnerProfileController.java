package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.RunnerProfileDto;
import org.example.stride.service.RunnerProfileService;
import org.example.stride.service.session.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/runner-profile")
@RequiredArgsConstructor
public class RunnerProfileController {

    private final RunnerProfileService runnerProfileService;
    private final CurrentUserService currentUserService;

    @GetMapping("/status")
    public ResponseEntity<?> getProfileStatus() {
        Long userId = currentUserService.getCurrentUser().getId();

        boolean hasProfile = runnerProfileService.hasProfile(userId);

        return ResponseEntity.ok(
                Map.of("hasProfile", hasProfile)
        );
    }

    @PostMapping
    public ResponseEntity<?> createProfile(
            @Valid @RequestBody RunnerProfileDto dto) {

        Long userId = currentUserService.getCurrentUser().getId();

        if (runnerProfileService.hasProfile(userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "You already have profile."
                    ));
        }

        boolean created = runnerProfileService.createRunnerProfile(dto);

        if (!created) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Profile is not created. It is possible to already exist."
                    ));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "Runner profile is created successfully."
                ));
    }
}