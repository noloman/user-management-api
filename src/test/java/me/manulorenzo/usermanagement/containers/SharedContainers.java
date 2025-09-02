package me.manulorenzo.usermanagement.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

public class SharedContainers {
    public static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.12-management")
            .withExposedPorts(5672, 15672);
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    static {
        RABBIT.start();
        POSTGRES.start();
    }
}
