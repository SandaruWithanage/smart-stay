package lk.smartstay.config;

import lk.smartstay.identity.service.CurrentIdentityConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Explicit endpoint allowlist; any newly added route stays denied by default.
 * All authenticated guest, booking and admin routes additionally enforce permission
 * and resource ownership in the controller, based on current DB grants.
 */
@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            CurrentIdentityConverter converter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // Backend accepts Bearer headers, not cookies.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/me", "/api/me/ui-config").authenticated()
                .requestMatchers("/api/guests", "/api/guests/**").authenticated()
                .requestMatchers("/api/admin/rooms", "/api/admin/rooms/**").authenticated()
                .requestMatchers("/api/admin/access/**").authenticated()
                .requestMatchers("/api/admin/reservations").authenticated()
                .requestMatchers("/api/booking-requests", "/api/booking-requests/**").authenticated()
                .requestMatchers("/api/admin/booking-requests", "/api/admin/booking-requests/**").authenticated()
                .requestMatchers("/api/me/reservations").authenticated()
                .requestMatchers("/api/reservations", "/api/reservations/**").authenticated()
                .anyRequest().denyAll())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
            .build();
    }
}
