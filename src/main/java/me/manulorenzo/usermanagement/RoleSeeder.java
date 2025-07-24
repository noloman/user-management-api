package me.manulorenzo.usermanagement;

import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RoleSeeder.class);

    private final String adminRoleName;
    private final String userRoleName;
    private final RoleRepository roleRepository;

    public RoleSeeder(
            @Value("${app.roles.admin}") String adminRoleName,
            @Value("${app.roles.user}") String userRoleName,
            RoleRepository roleRepository) {
        this.adminRoleName = adminRoleName;
        this.userRoleName = userRoleName;
        this.roleRepository = roleRepository;

        logger.info("RoleSeeder initialized with admin role: '{}', user role: '{}'", adminRoleName, userRoleName);
    }

    @Override
    public void run(String... args) {
        logger.info("Starting role seeding process");

        try {
            if (roleRepository.findByName(adminRoleName).isEmpty()) {
                roleRepository.save(new Role(adminRoleName));
                logger.info("{} role created and saved to database", adminRoleName);
            } else {
                logger.debug("{} role already exists, skipping creation", adminRoleName);
            }

            if (roleRepository.findByName(userRoleName).isEmpty()) {
                roleRepository.save(new Role(userRoleName));
                logger.info("{} role created and saved to database", userRoleName);
            } else {
                logger.debug("{} role already exists, skipping creation", userRoleName);
            }

            logger.info("Role seeding process completed successfully");

        } catch (Exception e) {
            logger.error("Error during role seeding process", e);
            throw e;
        }
    }
}
