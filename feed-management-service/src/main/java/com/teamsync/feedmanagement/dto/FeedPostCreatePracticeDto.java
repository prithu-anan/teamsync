package com.teamsync.feedmanagement.dto;

import java.time.LocalDate;

import com.teamsync.feedmanagement.entity.FeedPosts;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FeedPostCreatePracticeDto {
    @NotNull(message="type cannot be null")
    public FeedPosts.FeedPostType type ; 
    @NotNull(message="type cannot be null")
    public String content ;
    
    private String[] mediaUrls ;
    private LocalDate eventDate;
    private String[] pollOptions;

}
