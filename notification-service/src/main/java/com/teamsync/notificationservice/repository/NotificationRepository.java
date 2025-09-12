package com.teamsync.notificationservice.repository;

import com.teamsync.notificationservice.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    @Query(value = "{'userId': ?0, 'isRead': false}", count = true)
    long countUnreadByUserId(Long userId);
    
    @Query("{'userId': ?0, 'createdAt': {$gte: ?1}}")
    List<Notification> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime after);
    
    void deleteByUserIdAndCreatedAtBefore(Long userId, LocalDateTime before);
    
    void deleteByUserId(Long userId);
}