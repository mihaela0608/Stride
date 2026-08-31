package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Difficulty;
import org.example.stride.model.enums.GoalStatus;

import java.time.LocalDate;


@Entity
@Table(name = "goal_assessments")
@Getter
@Setter
public class GoalAssessment extends BaseEntity{
    //id
    //goalId
    //predictedTime
    //requiredImprovementPercentage
    //difficulty
    //status
    //explanation
    //createdAt
    @OneToOne
    @JoinColumn(name = "goal_id_id", nullable = false)
    private Goal goalId;

    @Column(name = "predicted_time", nullable = false)
    private int predictedTime;

    @Column(name = "required_improvement_percentage", nullable = false)
    private int requiredImprovementPercentage;

    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false)
    private GoalStatus status;

    private String explanation;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
}
