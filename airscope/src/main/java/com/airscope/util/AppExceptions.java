package com.airscope.util;

/**
 * Custom exceptions for AirScope.
 *
 * Using custom exceptions makes error handling cleaner and more readable.
 * Instead of returning error codes manually everywhere, we throw exceptions
 * and the GlobalExceptionHandler catches them and formats the response.
 *
 * Example usage:
 *   throw new ResourceNotFoundException("Device not found with id: " + id);
 */
public class AppExceptions {

    /**
     * Thrown when a requested resource doesn't exist.
     * Maps to HTTP 404 Not Found.
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when a user tries to do something they're not allowed to.
     * Maps to HTTP 403 Forbidden.
     */
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when input data is invalid or a conflict exists.
     * Maps to HTTP 400 Bad Request.
     */
    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }
}
