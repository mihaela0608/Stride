package org.example.stride.repository;

import org.example.stride.model.entity.Goal;
import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    boolean existsByRunnerProfileAndStatus(RunnerProfile profile, Status status);

    Optional<Goal> findAllByRunnerProfileAndStatus(RunnerProfile profile, Status status);

    Optional<Goal> findByRunnerProfileAndStatus(RunnerProfile runnerProfile, Status status);

    List<Goal> findAllByRunnerProfile(RunnerProfile runnerProfile);
}
