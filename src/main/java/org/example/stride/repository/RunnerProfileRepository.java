package org.example.stride.repository;

import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RunnerProfileRepository extends JpaRepository<RunnerProfile, Long> {
    Optional<RunnerProfile> findByUser(User user);
}
