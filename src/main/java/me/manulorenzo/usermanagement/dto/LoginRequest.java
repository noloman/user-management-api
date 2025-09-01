package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for user login")
public class LoginRequest {

    @Schema(description = "Username of the account", example = "john_doe", required = true)
    @NotBlank(message = "Username cannot be blank")
    @Size(max = 255, message = "Username cannot exceed 255 characters")
    private String username;

    @Schema(description = "Password of the account", example = "mySecurePassword123", required = true)
    @NotBlank(message = "Password cannot be blank")
    @Size(max = 128, message = "Password cannot exceed 128 characters")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
