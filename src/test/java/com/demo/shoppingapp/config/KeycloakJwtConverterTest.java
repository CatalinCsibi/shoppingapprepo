package com.demo.shoppingapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtConverterTest {

    private final KeycloakJwtConverter converter = new KeycloakJwtConverter();

    @Test
    void convertCombinesScopesAndRealmRoles() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "alice")
                .claim("scope", "products:read products:write")
                .claim("realm_access", Map.of("roles", List.of("admin", "manager")))
                .build();

        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(jwt);

        assert authentication != null;
        assertThat(authentication.getName()).isEqualTo("alice");
        assertThat(authentication.getAuthorities()).contains(
                new SimpleGrantedAuthority("SCOPE_products:read"),
                new SimpleGrantedAuthority("SCOPE_products:write"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MANAGER")
        );
    }

    @Test
    void convertHandlesMissingRealmAccessGracefully() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "alice")
                .build();

        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(jwt);

        assert authentication != null;
        assertThat(authentication.getName()).isEqualTo("alice");
        assertThat(authentication.getAuthorities()).isEmpty();
    }
}
