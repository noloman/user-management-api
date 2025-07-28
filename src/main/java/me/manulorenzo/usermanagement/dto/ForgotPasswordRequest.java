package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Forgot password request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;
}