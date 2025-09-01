package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Schema(description = "User profile object containing public and editable fields with validation constraints",
        example = "{\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\",\n  \"fullName\": \"John Doe\",\n  \"bio\": \"Software developer\",\n  \"imageUrl\": \"https://example.com/image.jpg\"\n}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Schema(description = "Username (3-50 characters, alphanumeric and underscores only)",
            example = "john_doe",
            pattern = "^[a-zA-Z0-9_]{3,50}$")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$",
            message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @Schema(description = "Valid email address", example = "john@example.com")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Schema(description = "Full name (optional, max 100 characters)", example = "John Doe")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @Schema(description = "Bio/description (optional, max 500 characters)", example = "Software developer")
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    @Schema(description = "Profile image URL (optional, must be valid URL)", example = "https://example.com/image.jpg")
    @URL(message = "Must be a valid URL")
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;
}
