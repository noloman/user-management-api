package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "User registration request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Schema(description = "Desired username", example = "johndoe")
    private String username;

    @Schema(description = "Email address", example = "john@example.com")
    private String email;

    @Schema(description = "Password", example = "securePassword123")
    private String password;
}
