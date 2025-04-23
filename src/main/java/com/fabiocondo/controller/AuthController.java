package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.BookNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.AuthService;
import com.fabiocondo.service.impl.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService; // Alterado para AuthService
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        return authService.authenticateWithUsernameAndPassword(user);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody String idTokenString) {
        return authService.authenticateWithGoogle(idTokenString);
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<?> generate(@RequestBody String email) throws UserNotFoundException, MessagingException {
        String otp = otpService.generateOtp(email);
        return response(HttpStatus.OK, "OTP gerado e enviado com sucesso!");
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<?> otpLogin(@RequestParam String email, @RequestParam String otp) throws Exception {
        return otpService.validateOtp(email, otp);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }

}
