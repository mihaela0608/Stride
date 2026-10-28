package org.example.stride.repository;

import org.example.stride.model.entity.Goal;
import org.example.stride.model.entity.TrainingPlan;
import org.example.stride.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
    Optional<TrainingPlan> findByGoalAndStatus(Goal goal, Status status);
}

