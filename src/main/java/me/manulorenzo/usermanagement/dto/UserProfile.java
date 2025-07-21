package me.manulorenzo.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
