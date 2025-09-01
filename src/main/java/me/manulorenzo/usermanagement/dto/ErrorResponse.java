package me.manulorenzo.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response format")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type/category", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Main error message", example = "Invalid input provided")
    private String message;

    @Schema(description = "Detailed error description", example = "The email field must be a valid email address")
    private String details;

    @Schema(description = "API endpoint that caused the error", example = "/api/auth/register")
    private String path;

    @Schema(description = "When the error occurred")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "List of validation errors (if applicable)")
    private List<ValidationError> validationErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual validation error")
    public static class ValidationError {
        @Schema(description = "Field name that failed validation", example = "email")
        private String field;

        @Schema(description = "Rejected value", example = "invalid-email")
        private Object rejectedValue;

        @Schema(description = "Validation error message", example = "must be a valid email address")
        private String message;
    }
}