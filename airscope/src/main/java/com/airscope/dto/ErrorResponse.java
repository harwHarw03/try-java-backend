package com.airscope.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * Standard error response format.
 *
 * All API errors return this consistent shape:
 * {
 *   "message": "Device not found",
 *   "status": 404
 * }
 *
 * This makes it easy for frontend clients to handle errors predictably.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    private String message;
    private int status;
}
