package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Email verification request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailRequest {

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "Email verification token received via email", example = "abc123def456")
    private String token;
}