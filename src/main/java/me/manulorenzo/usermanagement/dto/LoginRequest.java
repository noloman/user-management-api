package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for user login")
public class LoginRequest {

    @Schema(description = "Username of the account", example = "john_doe", required = true)
    private String username;

    @Schema(description = "Password of the account", example = "mySecurePassword123", required = true)
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
