package com.airscope.util;

import com.airscope.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler - catches all exceptions and returns consistent error responses.
 *
 * @RestControllerAdvice means this class applies to ALL controllers.
 * Each @ExceptionHandler method handles a specific exception type.
 *
 * Without this, Spring would return its own ugly HTML error pages.
 * With this, we always return clean JSON like:
 * {
 *   "message": "Device not found with id: 5",
 *   "status": 404
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle 404 - resource not found.
     */
    @ExceptionHandler(AppExceptions.ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AppExceptions.ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), 404));
    }

    /**
     * Handle 403 - unauthorized access.
     */
    @ExceptionHandler(AppExceptions.UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AppExceptions.UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), 403));
    }

    /**
     * Handle 400 - bad request / business logic error.
     */
    @ExceptionHandler(AppExceptions.BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(AppExceptions.BadRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), 400));
    }

    /**
     * Handle 400 - validation errors (from @Valid annotations).
     * Collects all field errors into a single readable message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // Collect all field validation error messages
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message, 400));
    }

    /**
     * Handle 401 - wrong credentials during login.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid email or password", 401));
    }

    /**
     * Handle 403 - Spring Security access denied.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Access denied", 403));
    }

    /**
     * Handle 500 - catch-all for unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log the full stack trace for debugging
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred: " + ex.getMessage(), 500));
    }
}
