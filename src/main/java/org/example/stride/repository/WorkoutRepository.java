package org.example.stride.repository;

import org.example.stride.model.entity.TrainingWeek;
import org.example.stride.model.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findAllByTrainingWeekOrderByDateAsc(TrainingWeek trainingWeek);
}
