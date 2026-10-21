package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.JSONParser;
import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.model.dto.GoalResponseDto;
import org.example.stride.model.entity.Goal;
import org.example.stride.service.GoalService;
import org.example.stride.service.RunnerProfileService;
import org.example.stride.service.session.CurrentUserService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

        ResponseEntity<Map<String, String>> CONFLICT = getMapResponseEntity();
        if (CONFLICT != null) return CONFLICT;

        goalService.createGoal(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "Goal is created successfully."
                ));
    }



    @PostMapping("/replace")
    public ResponseEntity<?> replaceGoal(@Valid @RequestBody GoalCreationDto dto){
        ResponseEntity<Map<String, String>> CONFLICT = getMapResponseEntity();
        if (CONFLICT != null) return CONFLICT;

        boolean result = goalService.replaceGoal(dto);

        if (!result){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Not active goal to be replaced"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "New active goal created"));
    }

    @PatchMapping("/active/complete")
    public ResponseEntity<?> completeGoal(){
        ResponseEntity<Map<String, String>> CONFLICT = getMapResponseEntity();
        if (CONFLICT != null) return CONFLICT;

        boolean result = goalService.completeActiveGoal();

        if (result){
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("message", "Goal is completed."));
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "No active goal."));
    }

    @GetMapping
    public ResponseEntity<?> getAll(){
        ResponseEntity<Map<String, String>> CONFLICT = getMapResponseEntity();
        if (CONFLICT != null) return CONFLICT;

        List<GoalResponseDto> goals = goalService.getAll();

        return ResponseEntity.ok(goals);
    }

    private @Nullable ResponseEntity<Map<String, String>> getMapResponseEntity() {
        Long userId = currentUserService.getCurrentUser().getId();

        if (!runnerProfileService.hasProfile(userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "First you need to create runner profile"
                    ));
        }
        return null;
    }
}