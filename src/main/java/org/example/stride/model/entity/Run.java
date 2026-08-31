package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.RunType;

import java.time.Duration;
import java.time.LocalDate;

@Entity
@Table(name = "runs")
@Getter
@Setter
public class Run extends BaseEntity{

    @OneToOne
    @JoinColumn(name = "runner_profile_id", nullable = false)
    private RunnerProfile runnerProfile;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private double distance;

    @Column(nullable = false)
    private Duration time;

    @Column(name = "average_peace", nullable = false)
    private Duration averagePeace;

    @Column(name = "averageHeartRate")
    private int averageHeartRate;

    @Column(name = "elevation_gain")
    private int elevationGain;

    @Column(name = "run_type", nullable = false)
    private RunType runType;

    @Column(name = "perceivedEffort", nullable = false)
    private int perceivedEffort;

    private String notes;


}
