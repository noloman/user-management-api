package me.manulorenzo.usermanagement.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI userManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Management API")
                        .description("""
                                A comprehensive user management system with JWT authentication, role-based authorization, and PostgreSQL database integration.
                                
                                **Key Features:**
                                - User registration & authentication
                                - JWT access and refresh token support (refresh = 7 days, access = 15 minutes)
                                - Role-based access control (RBAC) with ADMIN/USER roles
                                - Profile management (get/update)
                                - Secure logout and refresh token revocation
                                - **Docker-first setup**: PostgreSQL runs as a Docker container by default
                                
                                **Quick Start (Docker-first):**
                                1. Start with `./docker-scripts/start.sh` (requires Docker)
                                2. Open http://localhost:8082/swagger-ui.html
                                3. Register a user at `/api/auth/register` (first user gets ADMIN role)
                                4. Login at `/api/auth/login` to get access/refresh tokens
                                5. Use the *access token* in Authorization header for all protected endpoints
                                6. Use *refresh token* at `/api/auth/refresh` to renew your access token
                                7. Update your profile at `/api/profile` (GET/PUT)
                                8. Additional roles managed at `/api/admin/*`
                                
                                **Authentication Tip:**
                                Use the `Authorize` button below to enter your JWT access token.
                                - Format: `Bearer your-jwt-token-here`
                                - Refresh token is required for token renewal (see `/api/auth/refresh`)
                                
                                **Note:**
                                - This API runs with **PostgreSQL in Docker**. H2 database exists only for testing.
                                - Local dev (`application.yml`) also assumes Docker-based PostgreSQL on `localhost:5432`.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Manuel Lorenzo Parejo")
                                .email("manulorenzop@gmail.com")
                                .url("https://github.com/noloman/usermanagement"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}