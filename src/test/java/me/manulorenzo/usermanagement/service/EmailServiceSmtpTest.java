package me.manulorenzo.usermanagement.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import me.manulorenzo.usermanagement.containers.SharedContainers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public class EmailServiceSmtpTest {
    static GreenMail greenMail;

    @BeforeAll
    static void startMail() {
        greenMail = new GreenMail(new ServerSetup(0, null, "smtp"));
        greenMail.start();
    }

    @Autowired
    private EmailService emailService;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
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

    @Test
    void shouldSendVerificationEmail() throws Exception {
        EmailTask task = new EmailTask("verification", "to@localhost", "testuser", "AAA_TOKEN");
        emailService.sendVerificationEmail(task);
        greenMail.waitForIncomingEmail(1);
        MimeMessage[] messages = greenMail.getReceivedMessages();
        Assertions.assertEquals(1, messages.length);
        Assertions.assertTrue(messages[0].getSubject().contains("Verify Your Email"));
        Assertions.assertTrue(messages[0].getContent().toString().contains("AAA_TOKEN"));
    }
}
