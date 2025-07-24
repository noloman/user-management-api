package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response object containing JWT tokens after successful login")
public class LoginResponse {

    @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Refresh token for obtaining new access tokens", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;
}
