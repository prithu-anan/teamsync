package com.teamsync.feedmanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RequiredPollOptionsForPollTypeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredPollOptionsForPollType {
    String message() default "Poll options are required when type is 'poll'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 