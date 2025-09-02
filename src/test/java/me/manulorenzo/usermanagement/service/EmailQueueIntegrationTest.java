package me.manulorenzo.usermanagement.service;

import com.icegreen.greenmail.util.GreenMail;
import me.manulorenzo.usermanagement.containers.SharedContainers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Import({EmailQueueIntegrationTest.TestQueueConfig.class, RabbitAutoConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailQueueIntegrationTest {
    static GreenMail greenMail;
    // Use a unique queue for this test run
    public static final String TEST_QUEUE = "emailQueue-test-" + UUID.randomUUID();
    static volatile EmailTask received;
    static final CountDownLatch latch = new CountDownLatch(1);

    @BeforeAll
    static void startMail() {
        greenMail = new GreenMail(new com.icegreen.greenmail.util.ServerSetup(0, null, "smtp"));
        greenMail.start();
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", SharedContainers.RABBIT::getHost);
        registry.add("spring.rabbitmq.port", SharedContainers.RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", SharedContainers.RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", SharedContainers.RABBIT::getAdminPassword);
        registry.add("spring.datasource.url", SharedContainers.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", SharedContainers.POSTGRES::getUsername);
        registry.add("spring.datasource.password", SharedContainers.POSTGRES::getPassword);
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
    }

    // Test-only listener for this queue
    @RabbitListener(queues = "#{T(me.manulorenzo.usermanagement.service.EmailQueueIntegrationTest).TEST_QUEUE}")
    public void handleTestQueueEmail(EmailTask task) {
        received = task;
        latch.countDown();
    }

    // Moved to top-level (imported via @Import).
    @Configuration
    public static class TestQueueConfig {
        @Bean
        public Queue testQueue() {
            return new Queue(TEST_QUEUE, false, true, true);
        }

        @Bean
        public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
            return new Jackson2JsonMessageConverter();
        }
    }

    @Test
    void shouldSendAndReceiveEmailTask() throws Exception {
        EmailTask task = new EmailTask("verification", "test-unique@example.com", "user123", "sometoken");
        rabbitTemplate.convertAndSend(TEST_QUEUE, task);
        boolean got = latch.await(2, TimeUnit.SECONDS);
        org.junit.jupiter.api.Assertions.assertTrue(got, "Test-only RabbitListener did not receive task");
        org.junit.jupiter.api.Assertions.assertNotNull(received);
        org.junit.jupiter.api.Assertions.assertEquals("test-unique@example.com", received.getEmail());
    }
}
