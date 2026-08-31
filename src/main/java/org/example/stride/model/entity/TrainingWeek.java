package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Focus;

import java.time.LocalDate;

@Entity
@Table(name = "training_weeks")
@Getter
@Setter
public class TrainingWeek extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "training_plan_id", nullable = false)
    private TrainingPlan trainingPlan;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate end_date;

    private Focus focus;

}
