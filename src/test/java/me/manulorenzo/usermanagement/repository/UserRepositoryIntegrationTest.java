package me.manulorenzo.usermanagement.repository;

import me.manulorenzo.usermanagement.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRepositoryIntegrationTest {
    // Start container eagerly so dynamic property lambdas are safe
    private static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16");
        postgres.start();
    }

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void canSaveAndFetchUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@test.com");
        user.setPassword("pass123");
        userRepository.save(user);
        User found = userRepository.findByUsername("testuser").orElseThrow();
        Assertions.assertNotNull(found);
        Assertions.assertEquals("testuser@test.com", found.getEmail());
    }
}
