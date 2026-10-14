package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Difficulty;
import org.example.stride.model.enums.Status;

import java.time.LocalDate;


@Entity
@Table(name = "goal_assessments")
@Getter
@Setter
public class GoalAssessment extends BaseEntity{

    @OneToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "predicted_time", nullable = false)
    private int predictedTime;

    @Column(name = "required_improvement_percentage", nullable = false)
    private double requiredImprovementPercentage;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Difficulty difficulty;



    private String explanation;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
}
