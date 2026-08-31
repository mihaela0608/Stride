package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;

@Entity
@Table(name = "personal_bests")
@Getter
@Setter
public class PersonalBest extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "runner_profile_id", nullable = false)
    private RunnerProfile runnerProfile;

    @Column(nullable = false)
    private double distance;


    @Column(nullable = false)
    private Duration time;

    @Column(nullable = false)
    private LocalDate date;

}
