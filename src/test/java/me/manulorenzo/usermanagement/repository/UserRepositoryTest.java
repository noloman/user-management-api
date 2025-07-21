package me.manulorenzo.usermanagement.repository;

import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        Role role = new Role("USER");
        entityManager.persistAndFlush(role);

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRoles(Set.of(role));
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("password", found.get().getPassword());
        assertEquals(1, found.get().getRoles().size());
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertTrue(found.isEmpty());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        Role role = new Role("USER");
        entityManager.persistAndFlush(role);

        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pass1");
        user1.setRoles(Set.of(role));

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass2");
        user2.setRoles(Set.of(role));

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        long count = userRepository.count();

        assertEquals(2, count);
    }
}