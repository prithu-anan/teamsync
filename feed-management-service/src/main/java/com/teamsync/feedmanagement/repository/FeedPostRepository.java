package com.teamsync.feedmanagement.repository;

import com.teamsync.feedmanagement.entity.FeedPosts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedPostRepository extends JpaRepository<FeedPosts, Long> {
    
    @Query("SELECT fp FROM FeedPosts fp WHERE (:type IS NULL OR fp.type = :type)")
    Page<FeedPosts> findAllWithPaginationAndFilter(
            @Param("type") FeedPosts.FeedPostType type,
            Pageable pageable);
}
