package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Refresh token request with validation constraints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @Schema(description = "Valid refresh token obtained during login",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}