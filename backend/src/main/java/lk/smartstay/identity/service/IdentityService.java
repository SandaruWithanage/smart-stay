package lk.smartstay.identity.service;

import java.time.Clock;
import java.util.Locale;
import java.util.UUID;
import lk.smartstay.guest.persistence.GuestEntity;
import lk.smartstay.guest.persistence.GuestRepository;
import lk.smartstay.identity.api.LoginRequest;
import lk.smartstay.identity.api.RegistrationRequest;
import lk.smartstay.identity.api.TokenResponse;
import lk.smartstay.identity.persistence.AccessRepository;
import lk.smartstay.identity.persistence.UserEntity;
import lk.smartstay.identity.persistence.UserRepository;
import lk.smartstay.shared.ConflictException;
import lk.smartstay.shared.NotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityService {
    private final UserRepository users;
    private final GuestRepository guests;
    private final AccessRepository access;
    private final AuthorizationService authz;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final Clock clock;

    public IdentityService(UserRepository users, GuestRepository guests, AccessRepository access,
                           AuthorizationService authz, PasswordEncoder passwords,
                           TokenService tokens, Clock clock) {
        this.users = users;
        this.guests = guests;
        this.access = access;
        this.authz = authz;
        this.passwords = passwords;
        this.tokens = tokens;
        this.clock = clock;
    }

    @Transactional
    public TokenResponse register(RegistrationRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String document = request.documentNumber().strip().toUpperCase(Locale.ROOT);
        if (users.existsByEmail(email) || guests.existsByDocumentNumber(document)) {
            throw new ConflictException("An account or guest document is already registered");
        }
        var guest = guests.saveAndFlush(new GuestEntity(UUID.randomUUID(), document,
            request.fullName().strip(), request.phone().strip(), email));
        var user = users.saveAndFlush(new UserEntity(UUID.randomUUID(), email,
            passwords.encode(request.password()), guest.getFullName(),
            guest.getId(), clock.instant()));
        access.assignRole(user.getId(), "GUEST");
        return tokens.issue(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        var user = users.findByEmail(email).orElse(null);
        // Always run bcrypt even if email is unknown to reduce timing differences.
        String dummyHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.ogwHh8b1lK1/z0U2";
        if (user == null) {
            passwords.matches(request.password(), dummyHash);
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!passwords.matches(request.password(), user.getPasswordHash()) || !user.isActive()) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return tokens.issue(user);
    }

    @Transactional
    public void logout(Authentication authentication) {
        UUID id = authz.userId(authentication);
        var user = users.findById(id)
            .orElseThrow(() -> new NotFoundException("Account not found"));
        user.invalidateTokens(); // Invalidates every outstanding token for this account.
        users.flush();
    }
}
