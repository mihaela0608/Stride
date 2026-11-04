package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Type;

import java.time.Duration;
import java.time.LocalDate;

@Entity
@Table(name = "runs")
@Getter
@Setter
public class Run extends BaseEntity{

    @ManyToOne(optional = false)
    @JoinColumn(name = "runner_profile_id", nullable = false)
    private RunnerProfile runnerProfile;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private Workout workout;

    @Column(nullable = false)
    private double distance;

    @Column(nullable = false)
    private int duration;

    @Column(name = "average_pace", nullable = false)
    private double averagePace;

    @Column(name = "average_heart_rate")
    private Integer averageHeartRate;

    @Column(name = "elevation_gain")
    private Integer elevationGain;

    @Column(name = "run_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Type runType;

    @Column(name = "perceived_effort", nullable = false)
    private int perceivedEffort;

    private String notes;


}
