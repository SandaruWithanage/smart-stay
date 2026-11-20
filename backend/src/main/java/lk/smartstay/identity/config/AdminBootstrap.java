package lk.smartstay.identity.config;

import java.time.Clock;
import java.util.Locale;
import java.util.UUID;
import lk.smartstay.identity.persistence.AccessRepository;
import lk.smartstay.identity.persistence.UserEntity;
import lk.smartstay.identity.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * One-time opt-in administrative account creation.
 * Never seeds credentials or assigns ADMIN to public registrations.
 * Remove bootstrap env vars after the initial account is created.
 */
@Configuration
public class AdminBootstrap {
    @Bean
    ApplicationRunner initialAdministrator(
        UserRepository users,
        AccessRepository access,
        PasswordEncoder encoder,
        Clock clock,
        TransactionTemplate tx,
        @Value("${smartstay.bootstrap.admin-email:}") String email,
        @Value("${smartstay.bootstrap.admin-password:}") String password
    ) {
        return args -> {
            if (email.isBlank() && password.isBlank()) return;
            if (email.isBlank() || password.length() < 16) {
                throw new IllegalStateException(
                    "Admin bootstrap requires an email and password of at least 16 characters");
            }
            tx.executeWithoutResult(status -> {
                String normalized = email.strip().toLowerCase(Locale.ROOT);
                if (users.existsByEmail(normalized)) return; // Never rotate on restart.
                if (!access.roleExists("ADMIN")) {
                    throw new IllegalStateException("ADMIN role not present after migration");
                }
                UserEntity admin = users.saveAndFlush(new UserEntity(
                    UUID.randomUUID(), normalized, encoder.encode(password),
                    "Hotel Administrator", null, clock.instant()));
                access.assignRole(admin.getId(), "ADMIN");
            });
        };
    }
}
