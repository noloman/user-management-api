package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Reset password request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "Password reset token from email", example = "abc123def456")
    private String token;

    @Schema(description = "New password", example = "newpassword123")
    private String newPassword;
}