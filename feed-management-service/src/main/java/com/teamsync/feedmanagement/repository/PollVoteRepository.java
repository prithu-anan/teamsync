package com.teamsync.feedmanagement.repository;

import com.teamsync.feedmanagement.entity.PollVotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVotes, Long> {
}