package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.dto.RegisterRequest;
import me.manulorenzo.usermanagement.dto.UserProfile;
import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.entity.User;
import me.manulorenzo.usermanagement.repository.RoleRepository;
import me.manulorenzo.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private RoleRepository roleRepo;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_ShouldSaveUser_WhenValidRequest() {
        RegisterRequest request = new RegisterRequest("john", "password");
        when(userRepo.findByUsername("john")).thenReturn(Optional.empty());
        when(encoder.encode("password")).thenReturn("hashed");
        when(userRepo.count()).thenReturn(0L);
        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN")));

        userService.register(request);

        verify(userRepo).save(any(User.class));
    }

    @Test
    void register_ShouldSaveFirstUser_WithAdminRole() {
        RegisterRequest request = new RegisterRequest("alice", "pass");
        when(userRepo.findByUsername("alice")).thenReturn(Optional.empty());
        when(encoder.encode("pass")).thenReturn("hashed_pass");
        when(userRepo.count()).thenReturn(0L);
        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN")));

        userService.register(request);
        verify(userRepo).save(any(User.class));
    }

    @Test
    void register_ShouldSaveUser_WhenNotFirstUser_UserRole() {
        RegisterRequest request = new RegisterRequest("bob", "secret");
        when(userRepo.findByUsername("bob")).thenReturn(Optional.empty());
        when(encoder.encode("secret")).thenReturn("enc_secret");
        when(userRepo.count()).thenReturn(10L);
        when(roleRepo.findByName("USER")).thenReturn(Optional.of(new Role("USER")));

        userService.register(request);
        verify(userRepo).save(any(User.class));
    }

    @Test
    void register_ShouldThrow_WhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("existing", "pass");
        when(userRepo.findByUsername("existing")).thenReturn(Optional.of(new User()));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> userService.register(request));
    }

    @Test
    void register_ShouldThrow_WhenRoleMissing() {
        RegisterRequest request = new RegisterRequest("brad", "foo");
        when(userRepo.findByUsername("brad")).thenReturn(Optional.empty());
        when(encoder.encode("foo")).thenReturn("enc_foo");
        when(userRepo.count()).thenReturn(1L);
        when(roleRepo.findByName("USER")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> userService.register(request));
    }

    @Test
    void addRoleToUser_ShouldAddRole_WhenUserAndRoleExist() {
        User user = new User();
        user.setUsername("joan");
        Role role = new Role("MODERATOR");
        when(userRepo.findByUsername("joan")).thenReturn(Optional.of(user));
        when(roleRepo.findByName("MODERATOR")).thenReturn(Optional.of(role));

        userService.addRoleToUser("joan", "MODERATOR");
        verify(userRepo).save(user);
        org.junit.jupiter.api.Assertions.assertTrue(user.getRoles().contains(role));
    }

    @Test
    void addRoleToUser_ShouldThrow_WhenUserNotFound() {
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userService.addRoleToUser("ghost", "SOME_ROLE")
        );
    }

    @Test
    void addRoleToUser_ShouldThrow_WhenRoleNotFound() {
        User user = new User();
        user.setUsername("pete");
        when(userRepo.findByUsername("pete")).thenReturn(Optional.of(user));
        when(roleRepo.findByName("MISSING")).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> userService.addRoleToUser("pete", "MISSING")
        );
    }

    @Test
    void updateUserProfile_ShouldThrow_WhenChangingToExistingUsername() {
        User oldUser = new User();
        oldUser.setUsername("oldUser");
        oldUser.setEmail("old@example.com");
        when(userRepo.findByUsername("oldUser")).thenReturn(Optional.of(oldUser));
        when(userRepo.findByUsername("newName")).thenReturn(Optional.of(new User())); // already taken

        UserProfile changes = new UserProfile();
        changes.setUsername("newName"); // try changing to taken username
        changes.setEmail("other@example.com");

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                userService.updateUserProfile("oldUser", changes));
    }

    @Test
    void updateUserProfile_ShouldThrow_WhenChangingToExistingEmail() {
        User oldUser = new User();
        oldUser.setUsername("carl");
        oldUser.setEmail("carl@ex.com");
        when(userRepo.findByUsername("carl")).thenReturn(Optional.of(oldUser));
        when(userRepo.findByEmail("dupe@ex.com")).thenReturn(Optional.of(new User())); // already taken

        UserProfile changes = new UserProfile();
        changes.setUsername("carl");
        changes.setEmail("dupe@ex.com"); // try changing to taken email

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                userService.updateUserProfile("carl", changes));
    }

    @Test
    void updateUserProfile_ShouldUpdate_WhenUsernameAndEmailUnique() {
        User oldUser = new User();
        oldUser.setUsername("jdoe");
        oldUser.setEmail("jdoe@ex.com");
        when(userRepo.findByUsername("jdoe")).thenReturn(Optional.of(oldUser));
        when(userRepo.findByUsername("newjdoe")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("new@ex.com")).thenReturn(Optional.empty());

        UserProfile changes = new UserProfile();
        changes.setUsername("newjdoe");
        changes.setEmail("new@ex.com");
        changes.setFullName("Joe Doe");
        changes.setBio("Bio");
        changes.setImageUrl("A");

        userService.updateUserProfile("jdoe", changes);
        verify(userRepo).save(oldUser);
        org.junit.jupiter.api.Assertions.assertEquals("newjdoe", oldUser.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals("new@ex.com", oldUser.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals("Joe Doe", oldUser.getFullName());
    }

    @Test
    void updateUserProfile_ShouldUpdateFields_WhenNoUsernameOrEmailChange() {
        User oldUser = new User();
        oldUser.setUsername("emma");
        oldUser.setEmail("emma@ex.com");
        when(userRepo.findByUsername("emma")).thenReturn(Optional.of(oldUser));

        UserProfile changes = new UserProfile();
        changes.setUsername("emma");
        changes.setEmail("emma@ex.com");
        changes.setFullName("Emma Watson");
        changes.setBio("Actress");
        changes.setImageUrl("img");

        userService.updateUserProfile("emma", changes);
        verify(userRepo).save(oldUser);
        org.junit.jupiter.api.Assertions.assertEquals("emma", oldUser.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals("Emma Watson", oldUser.getFullName());
        org.junit.jupiter.api.Assertions.assertEquals("Actress", oldUser.getBio());
    }
}
