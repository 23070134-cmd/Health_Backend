package com.health.auth.controller;

import com.health.auth.dto.LoginRequest;
import com.health.auth.dto.RegisterRequest;
import com.health.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")
public class AuthController {

    @Autowired
    AuthService authService;

    // ===================== Auth Endpoints =====================
    // Register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest user) {
        return authService.register(user);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}
