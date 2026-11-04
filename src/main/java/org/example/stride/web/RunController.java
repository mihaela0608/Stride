package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.RunCreationDto;
import org.example.stride.service.RunService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/runs")
@RequiredArgsConstructor
public class RunController {
    private final RunService runService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createRun(@Valid @RequestBody RunCreationDto dto){
        runService.addRun(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Run is created successfully!"));
    }
}
