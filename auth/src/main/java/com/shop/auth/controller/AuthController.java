package com.shop.auth.controller;

import com.shop.auth.model.User;
import com.shop.auth.service.AuthService;
import com.shop.auth.service.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User saved = authService.register(user);
            return ResponseEntity.status(201).body(Map.of(
                    "id", saved.getId(),
                    "username", saved.getUsername(),
                    "email", saved.getEmail(),
                    "role", saved.getRole()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                )
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.status(401).build();
        }
        Long userId = jwtService.getClaim(token, claims -> claims.get("uid", Long.class));
        String username = jwtService.getClaim(token, claims -> claims.getSubject());
        String role = jwtService.getClaim(token, claims -> claims.get("role", String.class));
        return ResponseEntity.ok(Map.of(
                "id", userId,
                "username", username,
                "role", role
        ));
    }

    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
