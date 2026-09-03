package org.example.stride.repository;

import org.example.stride.model.entity.PersonalBest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalBestRepository extends JpaRepository<PersonalBest, Long> {
}
