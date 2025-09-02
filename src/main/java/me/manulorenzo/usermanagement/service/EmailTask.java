package me.manulorenzo.usermanagement.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailTask {
    private String type; // "verification", "reset", "welcome"
    private String email;
    private String username;
    private String verificationToken;
    private String passwordResetToken;
    private String roles; // comma-separated if multiple

    public EmailTask() {
    }

    // Verification or welcome
    public EmailTask(String type, String email, String username, String tokenOrRoles) {
        this.type = type;
        this.email = email;
        this.username = username;
        if ("verification".equals(type)) {
            this.verificationToken = tokenOrRoles;
        } else if ("welcome".equals(type)) {
            this.roles = tokenOrRoles;
        }
    }

    // Password reset
    public EmailTask(String type, String email, String username, String passwordResetToken, boolean isReset) {
        this.type = type;
        this.email = email;
        this.username = username;
        this.passwordResetToken = passwordResetToken;
    }

    // Unify access for 'token' for compatibility in email templates
    public String getToken() {
        if ("verification".equals(type)) {
            return verificationToken;
        }
        if ("reset".equals(type)) {
            return passwordResetToken;
        }
        return null;
    }
}
