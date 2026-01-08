package com.taskmanager.org.serviceTest;


import com.taskmanager.org.model.Role;
import com.taskmanager.org.service.JwtAuthenticationFilter;
import com.taskmanager.org.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private HandlerExceptionResolver exceptionResolver;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);


        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldPassRequestWhenNoAuthorizationHeader() throws IOException, ServletException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    void shouldPassRequestWhenAuthorizationHeaderIsInvalid() throws IOException, ServletException {
        when(request.getHeader("Authorization")).thenReturn("InvalidTokenFormat");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws IOException, ServletException {
        String jwt = "abc.def.ghi";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn("user@mail.com");

        UserDetails mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("user@mail.com");

        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(mockUser);
        when(jwtService.isTokenValid(jwt, mockUser)).thenReturn(true);


        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_USER"));

        when(jwtService.extractClaim(eq(jwt), any())).thenReturn(authorities);

        filter.doFilterInternal(request, response, filterChain);


        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@mail.com",
                SecurityContextHolder.getContext().getAuthentication().getName());

        verify(filterChain).doFilter(request, response);
    }




    @Test
    void shouldNotFilterSwaggerPaths() throws ServletException {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        assertTrue(filter.shouldNotFilter(request));
    }


    @Test
    void shouldFilterApiPaths() throws ServletException {
        when(request.getRequestURI()).thenReturn("/api/test");

        assertFalse(filter.shouldNotFilter(request));
    }
    @Test
    void shouldNotAuthenticateWhenTokenInvalid() throws IOException, ServletException {
        String jwt = "abc.def.ghi";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn("user@mail.com");

        UserDetails mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("user@mail.com");
        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(mockUser);


        when(jwtService.isTokenValid(jwt, mockUser)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    @Test
    void shouldAuthenticateUserWithNoRoles() throws IOException, ServletException {
        String jwt = "abc.def.ghi";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn("user@mail.com");

        UserDetails mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("user@mail.com");
        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(mockUser);

        when(jwtService.isTokenValid(jwt, mockUser)).thenReturn(true);

        // Token nie ma ról
        when(jwtService.extractClaim(eq(jwt), any())).thenReturn(List.of());

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@mail.com", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }
    @Test
    void shouldPassRequestWhenAuthorizationHeaderDoesNotStartWithBearer() throws IOException, ServletException {
        when(request.getHeader("Authorization")).thenReturn("Token abc.def.ghi");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    @Test
    void shouldNotFilterSwaggerAndWebjars() throws ServletException {
        String[] paths = {
                "/swagger-ui/index.html",
                "/v3/api-docs",
                "/swagger-ui/swagger-ui.css",
                "/swagger-ui/swagger-ui-bundle.js",
                "/webjars/something",
                "/swagger-ui.html"
        };

        for (String path : paths) {
            when(request.getRequestURI()).thenReturn(path);
            assertTrue(filter.shouldNotFilter(request), "Path should be skipped: " + path);
        }
    }
    @Test
    void shouldFilterApiRequest() throws ServletException {
        when(request.getRequestURI()).thenReturn("/api/test");
        assertFalse(filter.shouldNotFilter(request));
    }







}


