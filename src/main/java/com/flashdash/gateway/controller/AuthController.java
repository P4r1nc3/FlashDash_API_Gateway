package com.flashdash.gateway.controller;

import com.flashdash.gateway.dto.request.LoginRequest;
import com.flashdash.gateway.dto.request.RegisterRequest;
import com.flashdash.gateway.dto.response.AuthResponse;
import com.flashdash.gateway.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticate(loginRequest));
    }

    @PutMapping("/{userId}/resend-verification-email")
    public void resendVerificationEmail(@PathVariable String userId) {
        authService.emailVerification(userId);
    }
}
