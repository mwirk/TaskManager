package com.taskmanager.org.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private final String testSecretKey = "ZmFrZXNlY3JldGtleWZha2VwYWRkZGVkMTIzNDU2Nzg5MDEyMzQ1Ng==";

    @BeforeEach
    void setUp() {

        jwtService = new JwtService(
                "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b",
                3600000
        );

        userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn("testuser");

        GrantedAuthority authority = Mockito.mock(GrantedAuthority.class);
        Mockito.when(authority.getAuthority()).thenReturn("ROLE_USER");

        Collection<? extends GrantedAuthority> authorities = List.of(authority);
        Mockito.<Collection<? extends GrantedAuthority>>when(userDetails.getAuthorities())
                .thenReturn(authorities);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testGenerateTokenWithExtraClaims() {
        String token = jwtService.generateToken(
                java.util.Map.of("customClaim", "value"),
                userDetails
        );
        assertNotNull(token);

        String claim = jwtService.extractClaim(token, claims -> claims.get("customClaim", String.class));
        assertEquals("value", claim);
    }

    @Test
    void testTokenIsValid() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testTokenIsInvalidForWrongUser() {
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = Mockito.mock(UserDetails.class);
        Mockito.when(otherUser.getUsername()).thenReturn("otheruser");

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {

        jwtService.setJwtExpiration(-1);

        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testGetExpirationTime() {
        assertEquals(1000 * 60 * 60, jwtService.getExpirationTime());
    }
}
