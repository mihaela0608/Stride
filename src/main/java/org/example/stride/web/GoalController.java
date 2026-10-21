package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.service.GoalService;
import org.example.stride.service.RunnerProfileService;
import org.example.stride.service.session.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final RunnerProfileService runnerProfileService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<?> createGoal(
            @Valid @RequestBody GoalCreationDto dto) {

        Long userId = currentUserService.getCurrentUser().getId();

        if (!runnerProfileService.hasProfile(userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "First you need to create runner profile"
                    ));
        }

        goalService.createGoal(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "Goal is created successfully."
                ));
    }

    @PostMapping("/replace")
    public ResponseEntity<?> replaceGoal(@Valid @RequestBody GoalCreationDto dto){
        Long userId = currentUserService.getCurrentUser().getId();

        if (!runnerProfileService.hasProfile(userId)){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "First you need to create runner profile"
                    ));
        }

        boolean result = goalService.replaceGoal(dto);

        if (!result){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Not active goal to be replaced"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "New active goal created"));
    }
}