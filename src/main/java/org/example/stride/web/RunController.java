package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.RunCreationDto;
import org.example.stride.model.dto.RunResponseDto;
import org.example.stride.model.dto.RunUpdateDto;
import org.example.stride.service.RunService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class RunController {
    private final RunService runService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createRun(@Valid @RequestBody RunCreationDto dto){
        runService.addRun(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Run is created successfully!"));
    }

    @GetMapping
    public ResponseEntity<List<RunResponseDto>> getAll(){
        return ResponseEntity.ok(runService.getAll());
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateRun(@PathVariable(name = "id") Long runId, @Valid @RequestBody RunUpdateDto dto){
        runService.updateRun(runId, dto);
        return ResponseEntity.ok()
                .body(Map.of("message", "Run is updated!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRun(@PathVariable(name = "id") Long runId){
        runService.deleteRun(runId);
        return ResponseEntity.ok()
                .body(Map.of("message", "Run deleted successfuly!"));
    }
}
