package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "User profile object containing public and editable fields", example = "{\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\",\n  \"fullName\": \"John Doe\",\n  \"bio\": \"Software developer\",\n  \"imageUrl\": \"https://example.com/image.jpg\"\n}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Schema(description = "Username", example = "john_doe")
    private String username;
    @Schema(description = "Email address", example = "john@example.com")
    private String email;
    @Schema(description = "Full name", example = "John Doe")
    private String fullName;
    @Schema(description = "Bio/description", example = "Software developer")
    private String bio;
    @Schema(description = "Profile image URL", example = "https://example.com/image.jpg")
    private String imageUrl;
}
