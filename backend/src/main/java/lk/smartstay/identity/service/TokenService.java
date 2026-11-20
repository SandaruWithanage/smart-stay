package lk.smartstay.identity.service;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lk.smartstay.identity.api.TokenResponse;
import lk.smartstay.identity.persistence.UserEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

/** JWT carries identity and token revocation version, never authorization grants. */
@Service
public class TokenService {
    public static final long ACCESS_TOKEN_SECONDS = Duration.ofMinutes(15).toSeconds();
    public static final String ISSUER = "smartstay";
    public static final String AUDIENCE = "smartstay-web";
    private final JwtEncoder encoder;
    private final Clock clock;

    public TokenService(JwtEncoder encoder, Clock clock) {
        this.encoder = encoder;
        this.clock = clock;
    }

    public TokenResponse issue(UserEntity user) {
        var now = clock.instant();
        var claims = JwtClaimsSet.builder()
            .issuer(ISSUER)
            .audience(List.of(AUDIENCE))
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ACCESS_TOKEN_SECONDS))
            .id(UUID.randomUUID().toString())
            .claim("ver", user.getTokenVersion())
            .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", ACCESS_TOKEN_SECONDS);
    }
}
