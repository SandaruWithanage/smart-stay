package lk.smartstay.identity.service;

import java.util.UUID;
import lk.smartstay.identity.persistence.AccessRepository;
import lk.smartstay.identity.persistence.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Re-load current account state and roles/permissions for every JWT request.
 * Database policy changes and token-version invalidations take effect immediately.
 */
@Component
public class CurrentIdentityConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserRepository users;
    private final AccessRepository access;

    public CurrentIdentityConverter(UserRepository users, AccessRepository access) {
        this.users = users;
        this.access = access;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            UUID id = UUID.fromString(jwt.getSubject());
            var user = users.findById(id).orElseThrow(
                () -> new BadCredentialsException("Unknown user"));
            Number version = jwt.getClaim("ver");
            if (!user.isActive() || version == null
                || version.longValue() != user.getTokenVersion()) {
                throw new BadCredentialsException("Expired user authorization");
            }
            var authorities = access.permissions(id).stream()
                .map(permission -> new SimpleGrantedAuthority("PERM_" + permission))
                .toList();
            return new JwtAuthenticationToken(jwt, authorities, id.toString());
        } catch (IllegalArgumentException | ClassCastException e) {
            throw new BadCredentialsException("Invalid token identity");
        }
    }
}
