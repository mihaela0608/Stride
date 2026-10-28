package org.example.stride.repository;

import org.example.stride.model.entity.Run;
import org.example.stride.model.entity.RunnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RunRepository extends JpaRepository<Run, Long> {
    List<Run> findAllByRunnerProfileAndDateAfter(RunnerProfile profile, LocalDate startDate);
}
