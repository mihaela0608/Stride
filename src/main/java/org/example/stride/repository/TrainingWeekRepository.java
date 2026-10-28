package org.example.stride.repository;

import org.example.stride.model.entity.TrainingPlan;
import org.example.stride.model.entity.TrainingWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingWeekRepository extends JpaRepository<TrainingWeek, Long> {
    List<TrainingWeek> findAllByTrainingPlanOrderByWeekNumberAsc(TrainingPlan trainingPlan);

    Optional<TrainingWeek> findByTrainingPlanAndWeekNumber(TrainingPlan trainingPlan, int weekNumber);

}
