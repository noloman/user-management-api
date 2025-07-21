package me.manulorenzo.usermanagement.repository;

import me.manulorenzo.usermanagement.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_ShouldReturnRole_WhenRoleExists() {
        Role role = new Role("ADMIN");
        entityManager.persistAndFlush(role);

        Optional<Role> found = roleRepository.findByName("ADMIN");

        assertTrue(found.isPresent());
        assertEquals("ADMIN", found.get().getName());
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenRoleDoesNotExist() {
        Optional<Role> found = roleRepository.findByName("NONEXISTENT");

        assertTrue(found.isEmpty());
    }
}