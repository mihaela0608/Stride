
package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.model.dto.GoalResponseDto;
import org.example.stride.service.GoalService;
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

    @PostMapping
    public ResponseEntity<Map<String, String>> createGoal(
            @Valid @RequestBody GoalCreationDto dto) {

        goalService.createGoal(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "Goal is created successfully."
                ));
    }

    @PostMapping("/replace")
    public ResponseEntity<Map<String, String>> replaceGoal(
            @Valid @RequestBody GoalCreationDto dto) {

        goalService.replaceGoal(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "New active goal created."
                ));
    }

    @PatchMapping("/active/complete")
    public ResponseEntity<Map<String, String>> completeGoal() {

        boolean completed = goalService.completeActiveGoal();

        if (!completed) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "No active goal."));
        }

        return ResponseEntity.ok(
                Map.of("message", "Goal is completed.")
        );
    }

    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getAll() {
        return ResponseEntity.ok(goalService.getAll());
    }
}
