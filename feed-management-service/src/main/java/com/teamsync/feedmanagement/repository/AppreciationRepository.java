package com.teamsync.feedmanagement.repository;

import com.teamsync.feedmanagement.entity.Appreciations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppreciationRepository extends JpaRepository<Appreciations, Long> {
}