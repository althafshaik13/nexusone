package dev.nexusone.ticket_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${nexusone.security.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${nexusone.security.expected-issuer}")
    private String expectedIssuer;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), new JwtIssuerValidator(expectedIssuer));
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());

        http
                // Stateless bearer-token API: CSRF protection defends against a browser
                // automatically attaching credentials (cookies/session) to a forged
                // cross-site request. A bearer token in an Authorization header is never
                // attached automatically, so there's nothing for CSRF to exploit here.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/assign").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/transition").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/tickets/*/events").hasAnyRole("AGENT", "ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }
}
