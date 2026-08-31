package org.example.stride.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.stride.model.enums.Day;
import org.example.stride.model.enums.ExperienceLevel;

import java.util.List;

@Entity
@Table(name = "runner_profiles")
@Getter
@Setter
public class RunnerProfile extends BaseEntity{

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "experience_level", nullable = false)
    private ExperienceLevel experienceLevel;

    @Column(name = "runs_per_week", nullable = false)
    private int runsPerWeek;

    @Column(name = "average_weekly_distance", nullable = false)
    private double averageWeeklyDistance;

    @Column(name = "longest_recent_run")
    private double longestRecentRun;

//    @Column(name = "preferred_running_days")
//    private List<Day> preferredRunningDays;
    //TODO Running days not covered
}
