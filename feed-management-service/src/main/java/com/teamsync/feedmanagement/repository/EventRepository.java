package com.teamsync.feedmanagement.repository;


import com.teamsync.feedmanagement.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Events, Long> {
    List<Events> findAllByOrderByDateDesc();
    
    @Query("SELECT e FROM Events e WHERE e.date >= :startDate AND e.date <= :endDate ORDER BY e.date ASC")
    List<Events> findEventsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM Events e WHERE e.date >= :selectedDate ORDER BY e.date ASC")
    List<Events> findUpcomingEventsFromDate(@Param("selectedDate") LocalDate selectedDate);
}