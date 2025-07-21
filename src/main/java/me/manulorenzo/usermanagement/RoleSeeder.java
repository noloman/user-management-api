package me.manulorenzo.usermanagement;

import lombok.AllArgsConstructor;
import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class RoleSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RoleSeeder.class);

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        logger.info("Starting role seeding process");

        try {
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                roleRepository.save(new Role("ADMIN"));
                logger.info("ADMIN role created and saved to database");
            } else {
                logger.debug("ADMIN role already exists, skipping creation");
            }

            if (roleRepository.findByName("USER").isEmpty()) {
                roleRepository.save(new Role("USER"));
                logger.info("USER role created and saved to database");
            } else {
                logger.debug("USER role already exists, skipping creation");
            }

            logger.info("Role seeding process completed successfully");

        } catch (Exception e) {
            logger.error("Error during role seeding process", e);
            throw e;
        }
    }
}
