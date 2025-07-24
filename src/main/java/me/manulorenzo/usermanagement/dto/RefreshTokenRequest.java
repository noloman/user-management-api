package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for refreshing access token")
public class RefreshTokenRequest {

    @Schema(description = "Refresh token", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String refreshToken;
}