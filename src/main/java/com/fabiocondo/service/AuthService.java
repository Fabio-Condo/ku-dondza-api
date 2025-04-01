package com.fabiocondo.service;

import com.fabiocondo.domain.User;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<User> authenticateWithUsernameAndPassword(User user);

    ResponseEntity<?> authenticateWithGoogle(String idTokenString);
}
