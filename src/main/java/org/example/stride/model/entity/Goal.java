package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Status;

import java.time.Duration;
import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Getter
@Setter
public class Goal extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "runner_profile_id", nullable = false)
    private RunnerProfile runnerProfile;

    @Column(name = "target_distance", nullable = false)
    private double targetDistance;

    @Column(name = "target_time")
    private Duration targetTime;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;


}
