package com.shop.app.controller.api;

import com.shop.app.model.User;
import com.shop.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Authentication", description = "Endpoints for obtaining authentication tokens")
public class AuthApiController {

    private final UserService userService;

    public AuthApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/token")
    @Operation(
            summary = "Obtain Basic authentication token",
            description = "Validates the supplied credentials and returns a token that can be used in the Authorization header.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token issued", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    public ResponseEntity<?> getToken(@Valid @RequestBody TokenRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String rawCredentials = request.getUsername() + ":" + request.getPassword();
        String token = "Basic " + Base64.getEncoder().encodeToString(rawCredentials.getBytes(StandardCharsets.UTF_8));
        TokenResponse response = new TokenResponse(token, user.getRole());
        return ResponseEntity.ok(response);
    }

    public static class TokenRequest {
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

    public static class TokenResponse {
        private final String token;
        private final String role;

        public TokenResponse(String token, String role) {
            this.token = token;
            this.role = role;
        }

        public String getToken() {
            return token;
        }

        public String getRole() {
            return role;
        }
    }
}
