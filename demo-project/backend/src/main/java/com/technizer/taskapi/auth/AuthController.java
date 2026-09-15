package com.technizer.taskapi.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public TokenResponse register(@RequestBody RegisterRequest request) {
        String token = authService.register(request.email(), request.password(), request.displayName());
        return new TokenResponse(token);
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return new TokenResponse(token);
    }

    public record RegisterRequest(@Email String email, @NotBlank String password, @NotBlank String displayName) {
    }

    public record LoginRequest(@Email String email, @NotBlank String password) {
    }

    public record TokenResponse(String token) {
    }
}
