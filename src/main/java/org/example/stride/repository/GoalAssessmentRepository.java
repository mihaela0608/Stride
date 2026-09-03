package org.example.stride.repository;

import org.example.stride.model.entity.GoalAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalAssessmentRepository extends JpaRepository<GoalAssessment, Long> {
}
