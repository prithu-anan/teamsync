package com.teamsync.usermanagement.exception;

/**
 * This class is used to handle unauthorized exception
 * HTTP Status Code: 401
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}