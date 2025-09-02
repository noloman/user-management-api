package me.manulorenzo.usermanagement.repository;

import me.manulorenzo.usermanagement.entity.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RoleRepositoryIntegrationTest {
    private static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16");
        postgres.start();
    }

    @Autowired
    private RoleRepository roleRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void canSaveAndFetchRole() {
        String uniqueName = "USER_" + System.currentTimeMillis();
        Role role = new Role();
        role.setName(uniqueName);
        roleRepository.save(role);
        Role found = roleRepository.findByName(uniqueName).orElseThrow();
        Assertions.assertNotNull(found);
        Assertions.assertEquals(uniqueName, found.getName());
    }
}
