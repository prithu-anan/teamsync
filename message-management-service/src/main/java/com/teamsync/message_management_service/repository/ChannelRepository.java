package com.teamsync.message_management_service.repository;


import com.teamsync.message_management_service.entity.Channels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channels, Long> {
}