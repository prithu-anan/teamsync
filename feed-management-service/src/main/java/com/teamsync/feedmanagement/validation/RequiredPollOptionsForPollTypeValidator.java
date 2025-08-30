package com.teamsync.feedmanagement.validation;

import com.teamsync.feedmanagement.dto.FeedPostCreateRequest;
import com.teamsync.feedmanagement.entity.FeedPosts;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequiredPollOptionsForPollTypeValidator implements ConstraintValidator<RequiredPollOptionsForPollType, FeedPostCreateRequest> {

    @Override
    public void initialize(RequiredPollOptionsForPollType constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(FeedPostCreateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let @NotNull handle null validation
        }

        // If type is not poll, this validation doesn't apply
        if (request.getType() != FeedPosts.FeedPostType.poll){
            if(request.getPollOptions()!=null) return false;
            return true;
        }

        // If type is poll, pollOptions must be non-null and non-empty
        return request.getPollOptions() != null && request.getPollOptions().length > 0;
    }
} 