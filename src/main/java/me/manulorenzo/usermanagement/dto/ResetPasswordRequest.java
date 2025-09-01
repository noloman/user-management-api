package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Password reset request with validation constraints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @Schema(description = "Valid email address", example = "john@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Schema(description = "Password reset token received via email", example = "abc123-def456-ghi789")
    @NotBlank(message = "Reset token cannot be blank")
    @Size(max = 255, message = "Reset token is too long")
    private String token;

    @Schema(description = "New password (minimum 8 characters, must contain at least one letter and one number)",
            example = "newSecurePassword123",
            minLength = 8)
    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "New password must contain at least one letter and one number")
    private String newPassword;
}