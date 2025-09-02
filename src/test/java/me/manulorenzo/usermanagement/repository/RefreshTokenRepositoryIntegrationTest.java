package me.manulorenzo.usermanagement.repository;

import me.manulorenzo.usermanagement.containers.SharedContainers;
import me.manulorenzo.usermanagement.entity.RefreshToken;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RefreshTokenRepositoryIntegrationTest {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SharedContainers.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", SharedContainers.POSTGRES::getUsername);
        registry.add("spring.datasource.password", SharedContainers.POSTGRES::getPassword);

        registry.add("spring.rabbitmq.host", SharedContainers.RABBIT::getHost);
        registry.add("spring.rabbitmq.port", SharedContainers.RABBIT::getAmqpPort);
    }

    @Test
    void canSaveAndFetchRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("abc123token");
        token.setExpiryDate(java.time.Instant.now().plusSeconds(86400));
        refreshTokenRepository.save(token);
        RefreshToken found = refreshTokenRepository.findByToken("abc123token").orElseThrow();
        Assertions.assertNotNull(found);
        Assertions.assertEquals("abc123token", found.getToken());
    }
}
