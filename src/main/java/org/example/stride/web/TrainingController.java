package org.example.stride.web;

import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.TrainingPlanViewDto;
import org.example.stride.model.dto.TrainingWeekViewDto;
import org.example.stride.model.dto.WorkoutsViewDto;
import org.example.stride.service.TrainingPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


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

    @GetMapping("/workouts")
    public ResponseEntity<List<WorkoutsViewDto>> viewAllWorkouts(){
        return ResponseEntity.ok(trainingPlanService.getAllWorkouts());
    }

    @GetMapping("/training-weeks")
    public ResponseEntity<List<TrainingWeekViewDto>> viewTrainingWeeks(){
        return ResponseEntity.ok(trainingPlanService.getAllTrainingWeeks());
    }

    @GetMapping("/week-workouts/{number}")
    public ResponseEntity<List<WorkoutsViewDto>> viewWeeklyWorkouts(@PathVariable (name = "number") int number){
        return ResponseEntity.ok(trainingPlanService.getWeeklyWorkouts(number));
    }
}
