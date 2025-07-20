package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.dto.RegisterRequest;
import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.entity.User;
import me.manulorenzo.usermanagement.repository.RoleRepository;
import me.manulorenzo.usermanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    public User register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        Role userRole = roleRepo.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            return roleRepo.save(role);
        });
        user.setRoles(Set.of(userRole));
        return userRepo.save(user);
    }
}

