package com.teamsync.feedmanagement.repository;


import com.teamsync.feedmanagement.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Events, Long> {
}