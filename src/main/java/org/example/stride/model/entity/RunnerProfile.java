package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Day;
import org.example.stride.model.enums.ExperienceLevel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "runner_profiles")
@Getter
@Setter
public class RunnerProfile extends BaseEntity{

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "experience_level", nullable = false)
    private ExperienceLevel experienceLevel;

    @Column(name = "runs_per_week", nullable = false)
    private int runsPerWeek;

    @Column(name = "average_weekly_distance", nullable = false)
    private double averageWeeklyDistance;

    @Column(name = "longest_recent_run")
    private double longestRecentRun;

    @Column(name = "average_pace_seconds")
    private double averagePaceSeconds;

    @ElementCollection
    @CollectionTable(
            name = "runner_profile_running_days",
            joinColumns = @JoinColumn(name = "runner_profile_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "running_day", nullable = false, length = 20)
    private Set<Day> preferredRunningDays = new HashSet<>();
}
