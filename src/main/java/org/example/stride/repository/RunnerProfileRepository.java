package org.example.stride.repository;

import org.example.stride.model.entity.RunnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunnerProfileRepository extends JpaRepository<RunnerProfile, Long> {
}
