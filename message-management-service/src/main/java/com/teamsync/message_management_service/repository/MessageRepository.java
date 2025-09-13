package com.teamsync.message_management_service.repository;

import com.teamsync.message_management_service.entity.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Messages, Long> {

    List<Messages> findByChannelIdOrderByTimestampAsc(Long channelId);

    Optional<Messages> findByIdAndChannelId(Long id, Long channelId);

    List<Messages> findByChannelIdAndIsPinnedTrueOrderByTimestampDesc(Long channelId);
}
