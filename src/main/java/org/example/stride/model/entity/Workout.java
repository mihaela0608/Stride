package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Status;
import org.example.stride.model.enums.Type;

import java.time.Duration;
import java.time.LocalDate;

@Entity
@Table(name = "workouts")
@Getter
@Setter
public class Workout extends BaseEntity{

    @OneToOne
    @JoinColumn(name = "training_week_id")
    private TrainingWeek trainingWeek;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private double distance;

    @Column(nullable = false)
    private Duration duration;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int intensity;

    @Column(nullable = false)
    private Status status;
}
