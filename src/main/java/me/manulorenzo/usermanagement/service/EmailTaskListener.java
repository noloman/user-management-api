package me.manulorenzo.usermanagement.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailTaskListener {
    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = "emailQueue")
    public void handleEmailTask(EmailTask task) {
        switch (task.getType()) {
            case "verification":
                emailService.sendVerificationEmail(task);
                break;
            case "reset":
                emailService.sendPasswordResetEmail(task);
                break;
            case "welcome":
                emailService.sendWelcomeEmail(task);
                break;
            default:
                // Unknown type
                break;
        }
    }
}
