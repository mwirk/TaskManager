package com.taskmanager.org.controller;


import com.taskmanager.org.dto.LoginResponse;
import com.taskmanager.org.dto.LoginUserDTO;
import com.taskmanager.org.exception.InvalidUserCredentialsException;
import com.taskmanager.org.model.CustomUserDetails;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.AuthenticationService;
import com.taskmanager.org.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthRestController(AuthenticationManager authenticationManager, JwtService jwtService, AuthenticationService authenticationService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDTO loginUserDto) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            UserDetails userDetails = new CustomUserDetails(
                    authenticatedUser.getId(),
                    authenticatedUser.getName(),
                    authenticatedUser.getEmail(),
                    authenticatedUser.getPassword(),
                    authenticatedUser.getRoles().stream()
                            .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r.getName()))
                            .toList()
            );

            String jwtToken = jwtService.generateToken(userDetails);

            LoginResponse loginResponse = new LoginResponse()
                    .setToken(jwtToken)
                    .setExpiresIn(jwtService.getExpirationTime());

            return ResponseEntity.ok(loginResponse);

        } catch (IllegalArgumentException e) {

            throw new InvalidUserCredentialsException("Invalid login credentials");

        }
    }
}

