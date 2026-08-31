package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Status;

import java.time.LocalDate;

@Entity
@Table(name = "training_plans")
@Getter
@Setter
public class TrainingPlan extends BaseEntity{

    @OneToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int weeks;

    @Column(nullable = false)
    private Status status;
}
