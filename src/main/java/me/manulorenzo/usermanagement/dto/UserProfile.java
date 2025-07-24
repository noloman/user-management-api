package me.manulorenzo.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "User profile object containing public and editable fields", example = "{\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\",\n  \"fullName\": \"John Doe\",\n  \"bio\": \"Software developer\",\n  \"imageUrl\": \"https://example.com/image.jpg\"\n}")
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String fullName;
    @Getter
    @Setter
    private String bio;
    @Getter
    @Setter
    private String imageUrl;
}
