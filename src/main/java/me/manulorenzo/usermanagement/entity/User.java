package me.manulorenzo.usermanagement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    @Setter
    @Getter
    private String username;
    @Setter
    @Getter
    private String password;
    @Setter
    @Getter
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();
    @Setter
    @Getter
    private String email;
    @Setter
    @Getter
    private String fullName;
    @Setter
    @Getter
    private String bio;
    @Setter
    @Getter
    private String imageUrl;

    @Setter
    @Getter
    private boolean enabled = false; // Account disabled until email verified

    @Setter
    @Getter
    private boolean emailVerified = false;

    @Setter
    @Getter
    private String verificationToken;

    @Setter
    @Getter
    private Instant verificationTokenExpiry;

    @Setter
    @Getter
    private String passwordResetToken;

    @Setter
    @Getter
    private Instant passwordResetTokenExpiry;
}
