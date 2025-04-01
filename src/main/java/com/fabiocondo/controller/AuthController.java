package com.fabiocondo.controller;

import com.fabiocondo.domain.User;
import com.fabiocondo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService; // Alterado para AuthService

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        return authService.authenticateWithUsernameAndPassword(user);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody String idTokenString) {
        return authService.authenticateWithGoogle(idTokenString);
    }

}
