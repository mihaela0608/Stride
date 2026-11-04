package org.example.stride.web;

import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.TrainingPlanViewDto;
import org.example.stride.service.TrainingPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {
    private final TrainingPlanService trainingPlanService;

    @GetMapping
    public ResponseEntity<TrainingPlanViewDto> viewTrainingPlan(){
        return ResponseEntity.ok(trainingPlanService.getCurrentTrainingPlan());
        //TODO: Training plan for active goal only
        //TODO: Returns goal id(can be problem)
    }
}
