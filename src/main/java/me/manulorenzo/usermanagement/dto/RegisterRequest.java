package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Request object for user registration")
public class RegisterRequest {

    @Schema(description = "Username for the new account", example = "john_doe", required = true)
    private String username;

    @Schema(description = "Password for the new account", example = "mySecurePassword123", required = true)
    private String password;
}
