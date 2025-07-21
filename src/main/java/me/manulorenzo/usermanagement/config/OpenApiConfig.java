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
                                A comprehensive user management system with JWT authentication and role-based authorization.
                                
                                **Features:**
                                - User registration and authentication
                                - JWT token-based security
                                - Role-based access control (RBAC)
                                - Admin operations for user management
                                
                                **Getting Started:**
                                1. Register a user via `/api/auth/register` (first user gets ADMIN role)
                                2. Login via `/api/auth/login` to get JWT token
                                3. Use the JWT token in Authorization header for protected endpoints
                                4. Admin users can manage roles via `/api/admin` endpoints
                                
                                **Authentication:**
                                - Use the `Authorize` button below to enter your JWT token
                                - Format: `Bearer your-jwt-token-here`
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